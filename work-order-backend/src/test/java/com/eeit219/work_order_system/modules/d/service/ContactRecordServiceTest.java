package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.service.WorkOrderAttachmentService;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordMultipartCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.entity.ContactRecord;
import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import com.eeit219.work_order_system.modules.d.repository.ContactRecordRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactRecordServiceTest {

    @Mock
    private ContactRecordRepository contactRecordRepository;

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Mock
    private WorkOrderAuthorizationService workOrderAuthorizationService;

    // 模擬角色查詢，避免單元測試連線到真實資料庫。
    @Mock
    private UserRoleRepository userRoleRepository;

    // 模擬留言附件查詢與上傳服務。
    @Mock
    private WorkOrderAttachmentService workOrderAttachmentService;

    // 模擬 E 模組通知服務，單元測試不會真的寫入通知或發送 WebSocket。
    @Mock
    private NotificationService notificationService;

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

    // 驗證工程師留言後，報修人與負責管理員都會收到通知，
    // 而留言的工程師本人不會收到自己的留言通知。
    @Test
    void createComment_notifiesCreatorAndAdmin_whenHandlerComments() {
        User handler = user(7, "陳志明");
        User creator = user(6, "王小明");
        User admin = user(1, "系統管理員");

        WorkOrder workOrder = workOrder(12);
        workOrder.setWorkOrderNo("WO-2025-0001");
        workOrder.setStatus(WorkOrderState.IN_PROGRESS);
        workOrder.setCreator(creator);
        workOrder.setAdmin(admin);
        workOrder.setAssignedHandler(handler);

        ContactRecordCreateRequest request = new ContactRecordCreateRequest();
        request.setContent("已完成現場檢查");

        when(workOrderDetailRepository.findDetailById(12))
                .thenReturn(Optional.of(workOrder));

        when(contactRecordRepository.save(
                org.mockito.ArgumentMatchers.any(ContactRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        contactRecordService.createComment(12, request, handler);

        verify(notificationService).sendNotification(
                6,
                7,
                12,
                "工單有新留言",
                "陳志明在工單 WO-2025-0001 留下新訊息",
                WorkOrderState.IN_PROGRESS
        );

        verify(notificationService).sendNotification(
                1,
                7,
                12,
                "工單有新留言",
                "陳志明在工單 WO-2025-0001 留下新訊息",
                WorkOrderState.IN_PROGRESS
        );

        // 前面只允許兩次通知，因此也能確認工程師本人沒有收到通知。
        verifyNoMoreInteractions(notificationService);
    }


    // 驗證待審核工單尚未指定負責管理員時，
    // 報修人留言會通知所有啟用中的管理員，但不通知報修人自己。
    @Test
    void createComment_notifiesActiveAdmins_whenPendingReviewHasNoAdmin() {
        User creator = user(6, "王小明");

        WorkOrder workOrder = workOrder(12);
        workOrder.setWorkOrderNo("WO-2025-0001");
        workOrder.setStatus(WorkOrderState.PENDING_REVIEW);
        workOrder.setCreator(creator);
        workOrder.setAdmin(null);
        workOrder.setAssignedHandler(null);

        ContactRecordCreateRequest request = new ContactRecordCreateRequest();
        request.setContent("補充報修現場資訊");

        when(workOrderDetailRepository.findDetailById(12))
                .thenReturn(Optional.of(workOrder));

        // 模擬 A 模組查到兩位啟用中的管理員。
        when(userRoleRepository.findUserIdsByRoleCodeAndStatus(
                "ADMIN",
                User.UserStatus.ACTIVE))
                .thenReturn(List.of(1, 2));

        when(contactRecordRepository.save(
                org.mockito.ArgumentMatchers.any(ContactRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        contactRecordService.createComment(12, request, creator);

        verify(userRoleRepository).findUserIdsByRoleCodeAndStatus(
                "ADMIN",
                User.UserStatus.ACTIVE
        );

        verify(notificationService).sendNotification(
                1,
                6,
                12,
                "工單有新留言",
                "王小明在工單 WO-2025-0001 留下新訊息",
                WorkOrderState.PENDING_REVIEW
        );

        verify(notificationService).sendNotification(
                2,
                6,
                12,
                "工單有新留言",
                "王小明在工單 WO-2025-0001 留下新訊息",
                WorkOrderState.PENDING_REVIEW
        );

        // 只允許上述兩位管理員收到通知，避免通知報修人自己或其他無關人員。
        verifyNoMoreInteractions(notificationService);


    }

    // 驗證純圖片留言會在圖片成功上傳後才發送通知。
    // 同時確認圖片留言即使沒有文字內容，仍可正常建立。
    @Test
    void createCommentWithImages_notifiesAfterImageUpload() {
        User handler = user(7, "陳志明");
        User creator = user(6, "王小明");

        WorkOrder workOrder = workOrder(12);
        workOrder.setWorkOrderNo("WO-2025-0001");
        workOrder.setStatus(WorkOrderState.IN_PROGRESS);
        workOrder.setCreator(creator);
        workOrder.setAssignedHandler(handler);

        ContactRecordMultipartCreateRequest request =
                new ContactRecordMultipartCreateRequest();
        request.setContent(null);

        MultipartFile image = new MockMultipartFile(
                "files",
                "repair-result.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(workOrderDetailRepository.findDetailById(12))
                .thenReturn(Optional.of(workOrder));

        when(contactRecordRepository.save(
                org.mockito.ArgumentMatchers.any(ContactRecord.class)))
                .thenAnswer(invocation -> {
                    ContactRecord record = invocation.getArgument(0);
                    record.setRecordId(30);
                    return record;
                });

        contactRecordService.createCommentWithImages(
                12,
                request,
                List.of(image),
                handler
        );

        // 同時監看附件服務與通知服務，確認兩者的實際呼叫順序。
        InOrder callOrder = inOrder(
                workOrderAttachmentService,
                notificationService
        );

        callOrder.verify(workOrderAttachmentService).upload(
                workOrder,
                image,
                handler,
                30
        );

        callOrder.verify(notificationService).sendNotification(
                6,
                7,
                12,
                "工單有新留言",
                "陳志明在工單 WO-2025-0001 留下新訊息",
                WorkOrderState.IN_PROGRESS
        );

        // 此情境只有報修人需要收到通知，工程師本人不會收到自己的通知。
        verifyNoMoreInteractions(notificationService);
    }


    // 驗證圖片上傳失敗時不會發送留言通知。
    // 正式環境中 createCommentWithImages() 具有交易控制，例外也會使留言與附件一起回滾。
    @Test
    void createCommentWithImages_doesNotNotify_whenImageUploadFails() {
        User handler = user(7, "陳志明");
        User creator = user(6, "王小明");

        WorkOrder workOrder = workOrder(12);
        workOrder.setWorkOrderNo("WO-2025-0001");
        workOrder.setStatus(WorkOrderState.IN_PROGRESS);
        workOrder.setCreator(creator);
        workOrder.setAssignedHandler(handler);

        ContactRecordMultipartCreateRequest request =
                new ContactRecordMultipartCreateRequest();
        request.setContent(null);

        MultipartFile image = new MockMultipartFile(
                "files",
                "invalid-image.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(workOrderDetailRepository.findDetailById(12))
                .thenReturn(Optional.of(workOrder));

        when(contactRecordRepository.save(
                org.mockito.ArgumentMatchers.any(ContactRecord.class)))
                .thenAnswer(invocation -> {
                    ContactRecord record = invocation.getArgument(0);
                    record.setRecordId(30);
                    return record;
                });

        // 模擬 B 模組在驗證或儲存圖片時發生失敗。
        doThrow(new IllegalArgumentException("圖片上傳失敗"))
                .when(workOrderAttachmentService)
                .upload(workOrder, image, handler, 30);

        assertThrows(
                IllegalArgumentException.class,
                () -> contactRecordService.createCommentWithImages(
                        12,
                        request,
                        List.of(image),
                        handler
                )
        );

        // 圖片失敗時流程不應執行到通知階段。
        verifyNoInteractions(notificationService);
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
