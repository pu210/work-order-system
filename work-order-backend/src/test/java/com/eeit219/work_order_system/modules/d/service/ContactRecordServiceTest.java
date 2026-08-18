package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.entity.ContactRecord;
import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import com.eeit219.work_order_system.modules.d.repository.ContactRecordRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactRecordServiceTest {

    @Mock
    private ContactRecordRepository contactRecordRepository;

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Mock
    private WorkOrderAuthorizationService workOrderAuthorizationService;

    @InjectMocks
    private ContactRecordService contactRecordService;

    @Test
    void createComment_trimsAndSavesComment() {
        User currentUser = user(7, "王小明");
        WorkOrder workOrder = workOrder(12);
        ContactRecordCreateRequest request = new ContactRecordCreateRequest();
        request.setContent("  已與報修人聯絡  ");

        when(workOrderDetailRepository.findDetailById(12)).thenReturn(Optional.of(workOrder));
        when(contactRecordRepository.save(org.mockito.ArgumentMatchers.any(ContactRecord.class)))
                .thenAnswer(invocation -> {
                    ContactRecord record = invocation.getArgument(0);
                    record.setRecordId(30);
                    record.setCreatedTime(LocalDateTime.of(2026, 8, 18, 10, 0));
                    return record;
                });

        ContactRecordResponse response = contactRecordService.createComment(12, request, currentUser);

        ArgumentCaptor<ContactRecord> captor = ArgumentCaptor.forClass(ContactRecord.class);
        verify(contactRecordRepository).save(captor.capture());
        assertEquals("已與報修人聯絡", captor.getValue().getContent());
        assertEquals(ContactRecordType.COMMENT, captor.getValue().getRecordType());
        assertEquals(30, response.getRecordId());
        assertEquals(7, response.getAuthorUserId());
        verify(workOrderAuthorizationService).validateCommentPermission(workOrder, currentUser);
    }

    @Test
    void getRecords_returnsRecordsInRepositoryOrder() {
        User currentUser = user(7, "王小明");
        WorkOrder workOrder = workOrder(12);
        ContactRecord first = record(1, "第一筆", workOrder, currentUser);
        ContactRecord second = record(2, "第二筆", workOrder, currentUser);

        when(workOrderDetailRepository.findDetailById(12)).thenReturn(Optional.of(workOrder));
        when(contactRecordRepository.findByWorkOrder_WorkOrderIdOrderByCreatedTimeAscRecordIdAsc(12))
                .thenReturn(List.of(first, second));

        List<ContactRecordResponse> result = contactRecordService.getRecords(12, currentUser);

        assertEquals(List.of(1, 2), result.stream().map(ContactRecordResponse::getRecordId).toList());
        verify(workOrderAuthorizationService).validateViewPermission(workOrder, currentUser);
    }

    private User user(Integer userId, String name) {
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        return user;
    }

    private WorkOrder workOrder(Integer workOrderId) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderId(workOrderId);
        return workOrder;
    }

    private ContactRecord record(Integer recordId, String content, WorkOrder workOrder, User author) {
        ContactRecord record = new ContactRecord();
        record.setRecordId(recordId);
        record.setContent(content);
        record.setRecordType(ContactRecordType.COMMENT);
        record.setCreatedTime(LocalDateTime.of(2026, 8, 18, 10, recordId));
        record.setWorkOrder(workOrder);
        record.setAuthor(author);
        return record;
    }
}
