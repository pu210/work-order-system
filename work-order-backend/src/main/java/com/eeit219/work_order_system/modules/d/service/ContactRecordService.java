package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;
import com.eeit219.work_order_system.modules.b.service.WorkOrderAttachmentService;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordMultipartCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.entity.ContactRecord;
import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import com.eeit219.work_order_system.modules.d.repository.ContactRecordRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactRecordService {

    // 查詢、儲存留言
    private final ContactRecordRepository contactRecordRepository;

    // 取得留言建立者、被指派的工程師工單
    private final WorkOrderDetailRepository workOrderDetailRepository;

    // 共用權限判斷
    private final WorkOrderAuthorizationService workOrderAuthorizationService;

    // 共用B模組圖片驗證、儲存、查詢
    private final WorkOrderAttachmentService workOrderAttachmentService;

    // 查詢留言人角色代號
    private final UserRoleRepository userRoleRepository;

    // 每條留言最多5張圖片
    private static final int MAX_COMMENT_IMAGE_COUNT = 5;

    //建立留言
    @Transactional
    public ContactRecordResponse createComment(
            Integer workOrderId,
            ContactRecordCreateRequest request,
            User currentUser) {

        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單 ID:" + workOrderId));

        workOrderAuthorizationService.validateCommentPermission(workOrder, currentUser);


        ContactRecord contactRecord = new ContactRecord();
        contactRecord.setAuthor(currentUser);
        contactRecord.setWorkOrder(workOrder);
        contactRecord.setContent(request.getContent().strip());
        contactRecord.setRecordType(ContactRecordType.COMMENT);

        ContactRecord savedRecord = contactRecordRepository.save(contactRecord);
        return toResponse(savedRecord);
    }

    // 建立留言並上傳圖片；任一圖片失敗時，整筆交易一併回滾
    @Transactional
    public ContactRecordResponse createCommentWithImages(
            Integer workOrderId,
            ContactRecordMultipartCreateRequest request,
            List<MultipartFile> files,
            User currentUser) {

        validateCommentContentOrImages(request.getContent(), files);

        validateCommentImageCount(files);
        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單 ID:" + workOrderId));

        workOrderAuthorizationService.validateCommentPermission(workOrder, currentUser);


        ContactRecord contactRecord = new ContactRecord();
        contactRecord.setAuthor(currentUser);
        contactRecord.setWorkOrder(workOrder);
        contactRecord.setContent(normalizeCommentContent(request.getContent()));
        contactRecord.setRecordType(ContactRecordType.COMMENT);

        ContactRecord savedRecord = contactRecordRepository.save(contactRecord);

        if (files != null) {
            for (MultipartFile file : files) {
                workOrderAttachmentService.upload(
                        workOrder,
                        file,
                        currentUser,
                        savedRecord.getRecordId()
                );
            }
        }

        return toResponse(savedRecord);
    }

    // 取得留言圖片。
    // 依序驗證使用者的工單查看權限、留言與工單的關聯，以及附件與留言、工單的關聯，全部通過後才回傳附件。
    @Transactional(readOnly = true)
    public WorkOrderAttachment getCommentAttachmentForView(
            Integer workOrderId,
            Integer recordId,
            Integer attachmentId,
            User currentUser) {

        getViewableWorkOrder(workOrderId, currentUser);
        getContactRecordInWorkOrder(recordId, workOrderId);

        WorkOrderAttachment attachment =
                getExistingAttachment(attachmentId);

        validateAttachmentRelationship(
                attachment,
                workOrderId,
                recordId
        );
        return attachment;
    }

    //取得留言
    @Transactional(readOnly = true)
    public List<ContactRecordResponse> getRecords(Integer workOrderId, User currentUser) {

        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單 ID:" + workOrderId));

        workOrderAuthorizationService.validateViewPermission(workOrder, currentUser);

        return contactRecordRepository
                .findByWorkOrder_WorkOrderIdOrderByCreatedTimeAscRecordIdAsc(workOrderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 驗證留言至少包含有效文字或一張圖片。
    // 允許純文字、純圖片及文字加圖片；兩者皆空時拒絕建立留言。
    private void validateCommentContentOrImages(
            String content,
            List<MultipartFile> files) {

        boolean hasContent =
                content != null && !content.isBlank();
        boolean hasImages =
                files != null && !files.isEmpty();

        if (!hasContent && !hasImages) {
            throw new IllegalArgumentException("留言必須「輸入文字」或「上傳照片」。");
        }
    }
    // 正規化留言文字。null或只有空白的文字會轉成null；有效文字則移除頭尾空白後回傳。
    private String normalizeCommentContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        } else {
            return content.strip();
        }

    }

    // 驗證單則留言的圖片數量。
    // 未上傳圖片時允許通過；超過上限時拒絕建立留言。
    private void validateCommentImageCount(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        if (files.size() > MAX_COMMENT_IMAGE_COUNT) {
            throw new IllegalArgumentException("每次最多上傳5張照片");
        }

    }


    private ContactRecordResponse toResponse(ContactRecord contactRecord) {
        return ContactRecordResponse.builder()
                .recordId(contactRecord.getRecordId())
                .content(contactRecord.getContent())
                .createdTime(contactRecord.getCreatedTime())
                .recordType(contactRecord.getRecordType())
                .workOrderId(contactRecord.getWorkOrder().getWorkOrderId())
                .authorUserName(contactRecord.getAuthor().getName())
                .authorUserId(contactRecord.getAuthor().getUserId())
                .authorRoleCodes(userRoleRepository.findRoleCodesByUserId(
                        contactRecord.getAuthor().getUserId()))
                .attachments(workOrderAttachmentService.listByContactRecordId(
                        contactRecord.getRecordId()
                ))
                .build();
    }

    // 取得目前使用者有權查看的工單。
    // 工單不存在時回傳 404，沒有查看權限時回傳 403。
    private WorkOrder getViewableWorkOrder(Integer workOrderId, User currentUser) {

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單 ID:" + workOrderId));

        workOrderAuthorizationService.validateViewPermission(workOrder, currentUser);

        return workOrder;
    }

    // 取得指定工單內的聯繫紀錄。
    // 留言不存在或不屬於該工單時，統一視為找不到資源。
    private ContactRecord getContactRecordInWorkOrder(Integer recordId, Integer workOrderId) {

        return contactRecordRepository
                .findByRecordIdAndWorkOrder_WorkOrderId(recordId, workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到聯繫紀錄：" + recordId)
                );
    }

    // 透過 B 模組取得附件。將B模組找不到附件時產生的參數例外，轉換成D模組預覽API使用的404例外。
    private WorkOrderAttachment getExistingAttachment(Integer attachmentId) {
        try {
            return workOrderAttachmentService.view(attachmentId);
        } catch (IllegalArgumentException exception) {
            throw new EntityNotFoundException(
                    "找不到附件：" + attachmentId
            );
        }
    }

    // 驗證附件是否同時屬於指定工單與聯繫紀錄。
    // 任一關聯不符合時，統一回傳找不到附件，避免洩漏其他工單資料。
    private void validateAttachmentRelationship(
            WorkOrderAttachment attachment,
            Integer workOrderId,
            Integer recordId) {
        boolean belongsToWorkOrder =
                attachment.getWorkOrder() != null &&
                        workOrderId.equals(attachment.getWorkOrder().getWorkOrderId());

        boolean belongsToRecord =
                recordId.equals(
                        attachment.getContactRecordId()
                );

        if (!belongsToWorkOrder || !belongsToRecord) {
            throw new EntityNotFoundException(
                    "找不到附件：" + attachment.getAttachmentId()
            );
        }
    }

}
