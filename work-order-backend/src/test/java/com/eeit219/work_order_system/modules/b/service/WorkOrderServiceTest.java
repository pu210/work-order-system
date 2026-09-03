package com.eeit219.work_order_system.modules.b.service;

import com.eeit219.work_order_system.common.exception.BusinessRuleViolationException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.Role;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.RepairCategory;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private WorkOrderAttachmentService workOrderAttachmentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

    @Test
    void create_usesSubCategoryOverridePriority_whenOverrideIsSet() {
        User creator = user(1, "王小明");
        Priority overridePriority = priority(9, "緊急");
        Priority categoryDefaultPriority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, overridePriority, categoryDefaultPriority);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        // 編號產生、管理員通知在 create() 成功路徑一定會被呼叫到，不需要 lenient
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(any()))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.create(request, creator, List.of());

        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(captor.capture());
        assertEquals(overridePriority, captor.getValue().getPriority());
        assertEquals("緊急", response.getPriorityName());
    }

    @Test
    void create_fallsBackToCategoryDefaultPriority_whenSubCategoryHasNoOverride() {
        User creator = user(1, "王小明");
        Priority categoryDefaultPriority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, null, categoryDefaultPriority);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(any()))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.create(request, creator, List.of());

        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(captor.capture());
        assertEquals(categoryDefaultPriority, captor.getValue().getPriority());
        assertEquals("一般", response.getPriorityName());
    }

    @Test
    void create_throwsBusinessRuleViolation_whenNeitherSubCategoryNorCategoryHasPriority() {
        User creator = user(1, "王小明");
        SubCategory subCategory = subCategory(5, null, null);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> workOrderService.create(request, creator, List.of()));
        assertTrue(exception.getMessage().contains("5"));
        verifyNoInteractions(workOrderRepository);
    }

    @Test
    void create_throwsIllegalArgument_whenSubCategoryNotFound() {
        User creator = user(1, "王小明");
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(999);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> workOrderService.create(request, creator, List.of()));
        assertTrue(exception.getMessage().contains("999"));
        verifyNoInteractions(workOrderRepository);
    }

    @Test
    void create_notifiesEveryAdmin_afterWorkOrderSaved() {
        User creator = user(1, "王小明");
        Priority priority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, priority, priority);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(any()))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of(10, 20));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setWorkOrderId(88);
            return workOrder;
        });

        workOrderService.create(request, creator, List.of());

        verify(notificationService).sendNotification(eq(10), eq(1), eq(88), anyString(), anyString(),
                eq(WorkOrderState.PENDING_REVIEW));
        verify(notificationService).sendNotification(eq(20), eq(1), eq(88), anyString(), anyString(),
                eq(WorkOrderState.PENDING_REVIEW));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void create_incrementsWorkOrderNo_fromLatestExistingNumber() {
        User creator = user(1, "王小明");
        Priority priority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, priority, priority);
        String prefix = "WO-" + Year.now().getValue() + "-";

        WorkOrder latest = new WorkOrder();
        latest.setWorkOrderNo(prefix + "0003");

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(prefix))
                .thenReturn(Optional.of(latest));
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.create(request, creator, List.of());

        assertEquals(prefix + "0004", response.getWorkOrderNo());
    }

    @Test
    void create_startsWorkOrderNoAtOne_whenNoExistingWorkOrderThisYear() {
        User creator = user(1, "王小明");
        Priority priority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, priority, priority);
        String prefix = "WO-" + Year.now().getValue() + "-";

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(prefix))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderResponse response = workOrderService.create(request, creator, List.of());

        assertEquals(prefix + "0001", response.getWorkOrderNo());
    }

    @Test
    void create_uploadsAttachments_inSameCallAsWorkOrder_whenFilesProvided() {
        User creator = user(1, "王小明");
        Priority priority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, priority, priority);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        MultipartFile file = mock(MultipartFile.class);
        WorkOrderAttachmentResponse attachmentResponse = WorkOrderAttachmentResponse.builder()
                .attachmentId(1)
                .originalFileName("photo.png")
                .build();

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(any()))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderAttachmentService.upload(any(WorkOrder.class), eq(file), eq(creator)))
                .thenReturn(attachmentResponse);

        WorkOrderResponse response = workOrderService.create(request, creator, List.of(file));

        verify(workOrderAttachmentService).upload(any(WorkOrder.class), eq(file), eq(creator));
        assertEquals(1, response.getAttachments().size());
        assertEquals("photo.png", response.getAttachments().get(0).getOriginalFileName());
    }

    @Test
    void create_propagatesAttachmentValidationFailure_withoutSwallowingIt() {
        // 驗證失敗（如格式/大小不符）不能被吞掉——要往外拋，讓 @Transactional 把工單本體一併 rollback，
        // 不然會留下「工單建立成功但附件缺漏」的髒資料
        User creator = user(1, "王小明");
        Priority priority = priority(2, "一般");
        SubCategory subCategory = subCategory(5, priority, priority);

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(5);
        request.setLocationDetail("A棟301教室");

        MultipartFile invalidFile = mock(MultipartFile.class);

        when(subCategoryRepository.findByIdWithPriorityDetails(5)).thenReturn(Optional.of(subCategory));
        when(workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(any()))
                .thenReturn(Optional.empty());
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(Role.ADMIN, User.UserStatus.ACTIVE))
                .thenReturn(List.of());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderAttachmentService.upload(any(WorkOrder.class), eq(invalidFile), eq(creator)))
                .thenThrow(new IllegalArgumentException("檔案大小超過上限（10MB）：huge.png"));

        assertThrows(IllegalArgumentException.class,
                () -> workOrderService.create(request, creator, List.of(invalidFile)));
    }

    @Test
    void getById_throwsResourceNotFound_whenWorkOrderDoesNotExist() {
        when(workOrderRepository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> workOrderService.getById(999, 1, List.of("EMPLOYEE")));
        assertTrue(exception.getMessage().contains("999"));
        verifyNoInteractions(workOrderAttachmentService);
    }

    @Test
    void list_forwardsAllFiltersToSearch_whenCallerIsAdmin() {
        WorkOrderState status = WorkOrderState.IN_PROGRESS;
        Pageable pageable = PageRequest.of(0, 20);
        when(workOrderRepository.search("冷氣", status, 2, 3, 4, 6, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        workOrderService.list("冷氣", status, 2, 3, 4, 6, 1, List.of("ADMIN"), pageable);

        verify(workOrderRepository).search("冷氣", status, 2, 3, 4, 6, null, pageable);
        verifyNoInteractions(workOrderAttachmentService);
    }

    @Test
    void list_restrictsToSelfViaSearch_whenCallerIsHandler() {
        WorkOrderState status = WorkOrderState.IN_PROGRESS;
        Pageable pageable = PageRequest.of(0, 20);
        when(workOrderRepository.search("冷氣", status, 2, 3, null, null, 9, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        workOrderService.list("冷氣", status, 2, 3, 77, 4, 9, List.of("HANDLER"), pageable);

        verify(workOrderRepository).search("冷氣", status, 2, 3, null, null, 9, pageable);
    }

    @Test
    void list_fallsBackToMySubmissions_whenCallerIsPlainEmployee() {
        WorkOrderState status = WorkOrderState.PENDING_REVIEW;
        Pageable pageable = PageRequest.of(0, 20);
        when(workOrderRepository.findMySubmissions("冷氣", status, 5, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        workOrderService.list("冷氣", status, 2, 3, 4, 6, 5, List.of("EMPLOYEE"), pageable);

        verify(workOrderRepository).findMySubmissions("冷氣", status, 5, pageable);
        verify(workOrderRepository, org.mockito.Mockito.never()).search(any(), any(), any(), any(), any(), any(),
                any(), any());
    }

    @Test
    void listMySubmissions_forwardsFiltersAndCreatorIdToRepository() {
        WorkOrderState status = WorkOrderState.COMPLETED;
        Pageable pageable = PageRequest.of(1, 10);
        when(workOrderRepository.findMySubmissions("冷氣", status, 7, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        workOrderService.listMySubmissions("冷氣", status, 7, pageable);

        verify(workOrderRepository).findMySubmissions("冷氣", status, 7, pageable);
    }

    private User user(Integer userId, String name) {
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        return user;
    }

    private Priority priority(Integer id, String name) {
        Priority priority = new Priority();
        priority.setPrioritiesId(id);
        priority.setName(name);
        return priority;
    }

    private SubCategory subCategory(Integer id, Priority overridePriority, Priority categoryDefaultPriority) {
        RepairCategory repairCategory = new RepairCategory();
        repairCategory.setRepairCategoriesId(1);
        repairCategory.setName("空調");
        repairCategory.setDefaultPriority(categoryDefaultPriority);

        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoriesId(id);
        subCategory.setName("冷氣故障");
        subCategory.setRepairCategory(repairCategory);
        subCategory.setOverridePriority(overridePriority);
        return subCategory;
    }
}
