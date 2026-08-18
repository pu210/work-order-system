package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.entity.ContactRecord;
import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import com.eeit219.work_order_system.modules.d.repository.ContactRecordRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactRecordService {

    // 查詢、儲存留言
    private final ContactRecordRepository contactRecordRepository;

    //取得留言建立者、被指派的工程師工單
    private final WorkOrderDetailRepository workOrderDetailRepository;

    //共用權限判斷
    private final WorkOrderAuthorizationService workOrderAuthorizationService;

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

    private ContactRecordResponse toResponse(ContactRecord contactRecord) {
        return ContactRecordResponse.builder()
                .recordId(contactRecord.getRecordId())
                .content(contactRecord.getContent())
                .createdTime(contactRecord.getCreatedTime())
                .recordType(contactRecord.getRecordType())
                .workOrderId(contactRecord.getWorkOrder().getWorkOrderId())
                .authorUserName(contactRecord.getAuthor().getName())
                .authorUserId(contactRecord.getAuthor().getUserId())
                .build();
    }

}
