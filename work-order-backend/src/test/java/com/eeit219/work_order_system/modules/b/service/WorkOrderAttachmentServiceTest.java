package com.eeit219.work_order_system.modules.b.service;

import com.eeit219.work_order_system.common.exception.ResourceConflictException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderAttachmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderAttachmentServiceTest {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Mock
    private WorkOrderAttachmentRepository workOrderAttachmentRepository;

    @InjectMocks
    private WorkOrderAttachmentService workOrderAttachmentService;

    @Test
    void upload_throwsIllegalArgument_whenFileExceedsMaxSize() {
        WorkOrder workOrder = workOrder(1);
        User uploader = user(1);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(MAX_FILE_SIZE + 1);

        assertThrows(IllegalArgumentException.class,
                () -> workOrderAttachmentService.upload(workOrder, file, uploader));
        verify(workOrderAttachmentRepository, never()).save(any());
    }

    @Test
    void upload_succeeds_whenFileIsExactlyMaxSize() throws Exception {
        WorkOrder workOrder = workOrder(1);
        User uploader = user(1);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(MAX_FILE_SIZE);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("photo.png");
        // getSize() 只影響大小上限判斷，跟 getBytes() 的實際內容互不相關（mock 裡本來就是分開設定）；
        // 但新版驗證會用 ImageIO 真的解碼一次，所以這裡要給一張解得出來的最小合法 PNG，不能隨便塞空 byte[]
        when(file.getBytes()).thenReturn(onePixelPng());
        when(workOrderAttachmentRepository.save(any(WorkOrderAttachment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrderAttachmentResponse response = workOrderAttachmentService.upload(workOrder, file, uploader);

        assertEquals("photo.png", response.getOriginalFileName());
        verify(workOrderAttachmentRepository).save(any(WorkOrderAttachment.class));
    }

    @Test
    void upload_throwsIllegalArgument_whenContentTypeIsNotImage() {
        WorkOrder workOrder = workOrder(1);
        User uploader = user(1);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(IllegalArgumentException.class,
                () -> workOrderAttachmentService.upload(workOrder, file, uploader));
        verify(workOrderAttachmentRepository, never()).save(any());
    }

    @Test
    void delete_throwsResourceConflict_whenRequesterIsNotUploader() {
        WorkOrderAttachment attachment = attachment(30, user(5));
        when(workOrderAttachmentRepository.findById(30)).thenReturn(Optional.of(attachment));

        assertThrows(ResourceConflictException.class,
                () -> workOrderAttachmentService.delete(30, 99));
        verify(workOrderAttachmentRepository, never()).delete(any());
    }

    @Test
    void delete_removesAttachment_whenRequesterIsUploader() {
        WorkOrderAttachment attachment = attachment(30, user(5));
        when(workOrderAttachmentRepository.findById(30)).thenReturn(Optional.of(attachment));

        workOrderAttachmentService.delete(30, 5);

        ArgumentCaptor<WorkOrderAttachment> captor = ArgumentCaptor.forClass(WorkOrderAttachment.class);
        verify(workOrderAttachmentRepository).delete(captor.capture());
        assertEquals(30, captor.getValue().getAttachmentId());
    }

    private User user(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setName("測試使用者" + userId);
        return user;
    }

    private WorkOrder workOrder(Integer workOrderId) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderId(workOrderId);
        return workOrder;
    }

    private WorkOrderAttachment attachment(Integer attachmentId, User uploadedUser) {
        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setAttachmentId(attachmentId);
        attachment.setUploadedUser(uploadedUser);
        return attachment;
    }

    // Service 現在會用 ImageIO 實際解碼確認內容是合法圖片，測試要餵真的解得出來的 PNG，不能隨便塞假 byte[]
    private byte[] onePixelPng() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
