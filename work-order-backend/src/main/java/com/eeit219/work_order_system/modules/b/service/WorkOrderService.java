package com.eeit219.work_order_system.modules.b.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eeit219.work_order_system.common.exception.BusinessRuleViolationException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.Role;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final WorkOrderAttachmentService workOrderAttachmentService;
    private final NotificationService notificationService;
    private final UserRoleRepository userRoleRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
            SubCategoryRepository subCategoryRepository,
            WorkOrderAttachmentService workOrderAttachmentService,
            NotificationService notificationService,
            UserRoleRepository userRoleRepository) {
        this.workOrderRepository = workOrderRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.workOrderAttachmentService = workOrderAttachmentService;
        this.notificationService = notificationService;
        this.userRoleRepository = userRoleRepository;
    }

    // 建立工單：解析優先級、寫入工單本體，附件在同一個交易內一併驗證與寫入——
    // 只要其中一張附件驗證失敗（格式/大小/損毀），整個方法會拋出例外，工單本體連同已寫入的附件一起 rollback，
    // 不會留下「工單建立成功但附件缺漏」的髒資料。
    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request, User creator, List<MultipartFile> files) {
        SubCategory subCategory = subCategoryRepository.findByIdWithPriorityDetails(request.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("找不到子類別：" + request.getSubCategoryId()));

        Priority priority = resolvePriority(subCategory);

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo(generateWorkOrderNo());
        workOrder.setTitle(request.getTitle());
        workOrder.setSubCategory(subCategory);
        workOrder.setPriority(priority);
        workOrder.setLocationDetail(request.getLocationDetail());
        workOrder.setContactPhone(request.getContactPhone());
        workOrder.setDescription(request.getDescription());
        workOrder.setCreator(creator);
        workOrder.setDueTime(null);
        workOrder.setCreatedTime(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);

        // ===== 觸發發送通知給所有管理員 =====
        // 1. 查詢所有 role_id = 1 (管理員) 的使用者角色關聯紀錄
        List<UserRole> adminRoles = userRoleRepository.findByIdRoleId(1);

        // 2. 透過 for 迴圈，發送通知給每一位管理員
        for (UserRole adminRole : adminRoles) {
            Integer adminUserId = adminRole.getId().getUserId(); // 取得管理員的 userId

            notificationService.sendNotification(
                    adminUserId, // 接收通知的人（管理員 ID）
                    creator.getUserId(), // 發送通知的人（報修建單者 ID）
                    saved.getWorkOrderId(), // 工單 ID
                    "有新工單待審核！", // 通知標題
                    "使用者 " + creator.getName() + " 建立了一筆新工單：" + saved.getWorkOrderNo() + "，等待審核。", // 通知詳細內容
                    saved.getStatus() // 當時工單狀態 (PENDING_REVIEW)
            );
        }

        List<WorkOrderAttachmentResponse> attachments = List.of();
        if (files != null && !files.isEmpty()) {
            attachments = files.stream()
                    .map(file -> workOrderAttachmentService.upload(saved, file, creator))
                    .collect(Collectors.toList());
        }

        return toResponse(saved, subCategory, priority, creator, attachments);
    }

    // 查詢工單詳情
    public WorkOrderResponse getById(Integer workOrderId) {
        WorkOrder workOrder = workOrderRepository.findByIdWithDetails(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單：" + workOrderId));

        List<WorkOrderAttachmentResponse> attachments = workOrderAttachmentService.listByWorkOrder(workOrderId);

        return toResponse(workOrder, workOrder.getSubCategory(), workOrder.getPriority(), workOrder.getCreator(),
                attachments);
    }

    // 取得工單實體參照（不含附件明細的 join 查詢），供其他端點（如附件上傳）確認工單存在
    public WorkOrder getWorkOrderEntity(Integer workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單：" + workOrderId));
    }

    // 依呼叫者角色縮限範圍：ADMIN 看全部（可用 assignedHandlerId 篩特定工程師）；
    // HANDLER 只看「自己建立」或「被指派」的；EMPLOYEE 只看自己建立的。
    // 這支同時餵給 TicketList.vue（管理員頁面）跟 Dashboard.vue（三個角色都會呼叫），
    // 範圍限縮直接做在後端，不是只靠前端過濾——避免非管理員繞過前端直接打 API 拿到全公司工單
    public Page<WorkOrderListItemResponse> list(String keyword, WorkOrderState status, Integer priorityId,
            Integer categoryId, Integer assignedHandlerId, Integer currentUserId, Pageable pageable) {
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(currentUserId).stream()
                .map(code -> code.trim().toUpperCase())
                .toList();

        if (roleCodes.contains(Role.ADMIN)) {
            return workOrderRepository.search(keyword, status, priorityId, categoryId, assignedHandlerId, pageable)
                    .map(this::toListItem);
        }
        if (roleCodes.contains(Role.HANDLER)) {
            return workOrderRepository.findRelevantToUser(keyword, status, priorityId, categoryId, currentUserId,
                    pageable).map(this::toListItem);
        }
        return workOrderRepository.findMySubmissions(keyword, status, currentUserId, pageable)
                .map(this::toListItem);
    }

    // 一般使用者視角列表：只查自己建立的工單，只開放 keyword/status 篩選
    public Page<WorkOrderListItemResponse> listMySubmissions(String keyword, WorkOrderState status,
            Integer creatorId, Pageable pageable) {
        return workOrderRepository.findMySubmissions(keyword, status, creatorId, pageable).map(this::toListItem);
    }

    // 優先級判斷：子類別自己有 override 就用它，否則往上抓大類別的預設優先級
    private Priority resolvePriority(SubCategory subCategory) {
        if (subCategory.getOverridePriority() != null) {
            return subCategory.getOverridePriority();
        }
        // override_priority 為 null 時，往上抓大類別的預設優先級
        Priority defaultPriority = subCategory.getRepairCategory().getDefaultPriority();
        if (defaultPriority == null) {
            throw new BusinessRuleViolationException("子類別與所屬大類別皆未設定優先級：" + subCategory.getSubCategoriesId());
        }
        return defaultPriority;
    }

    // 工單編號產生：WO-年度-4碼流水號，取當年度目前最大號碼 +1
    private String generateWorkOrderNo() {
        String prefix = "WO-" + Year.now().getValue() + "-";

        int nextSequence = workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(prefix)
                .map(latest -> {
                    String sequencePart = latest.getWorkOrderNo().substring(latest.getWorkOrderNo().length() - 4);
                    return Integer.parseInt(sequencePart) + 1;
                })
                .orElse(1);

        return prefix + String.format("%04d", nextSequence);
    }

    // 工單詳情 DTO 組裝
    private WorkOrderResponse toResponse(WorkOrder workOrder, SubCategory subCategory,
            Priority priority, User creator, List<WorkOrderAttachmentResponse> attachments) {
        return WorkOrderResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(subCategory.getRepairCategory().getName())
                .subCategoryName(subCategory.getName())
                .priorityName(priority.getName())
                .locationDetail(workOrder.getLocationDetail())
                .contactPhone(workOrder.getContactPhone())
                .description(workOrder.getDescription())
                .dueTime(workOrder.getDueTime())
                .status(workOrder.getStatus().name())
                .createdTime(workOrder.getCreatedTime())
                .creatorName(creator.getName())
                .attachments(attachments)
                .build();
    }

    // 工單列表項目 DTO 組裝
    private WorkOrderListItemResponse toListItem(WorkOrder workOrder) {
        return WorkOrderListItemResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(workOrder.getSubCategory().getRepairCategory().getName())
                .priorityName(workOrder.getPriority().getName())
                .status(workOrder.getStatus().name())
                .creatorId(workOrder.getCreator() != null ? workOrder.getCreator().getUserId() : null)
                .creatorName(workOrder.getCreator() != null ? workOrder.getCreator().getName() : null)
                .assignedHandlerId(workOrder.getAssignedHandler() != null ? workOrder.getAssignedHandler().getUserId() : null)
                .assignedHandlerName(workOrder.getAssignedHandler() != null
                        ? workOrder.getAssignedHandler().getName()
                        : null)
                .createdTime(workOrder.getCreatedTime())
                .build();
    }
}
