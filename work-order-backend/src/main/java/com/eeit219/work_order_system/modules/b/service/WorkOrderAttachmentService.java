package com.eeit219.work_order_system.modules.b.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderAttachmentRepository;

@Service
public class WorkOrderAttachmentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final WorkOrderAttachmentRepository workOrderAttachmentRepository;
    private final UserRepository userRepository;

    public WorkOrderAttachmentService(WorkOrderAttachmentRepository workOrderAttachmentRepository,
            UserRepository userRepository) {
        this.workOrderAttachmentRepository = workOrderAttachmentRepository;
        this.userRepository = userRepository;
    }

    // 上傳單一附件：限圖片、10MB 上限，通過驗證才寫入 DB
    @Transactional
    public WorkOrderAttachmentResponse upload(WorkOrder workOrder, MultipartFile file, User uploadedUser) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上傳檔案不可為空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("檔案大小超過上限（10MB）：" + file.getOriginalFilename());
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("只允許上傳圖片檔案：" + file.getOriginalFilename());
        }

        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setWorkOrder(workOrder);
        attachment.setOriginalFileName(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setFileSize((int) file.getSize());
        attachment.setUploadedUser(uploadedUser);
        attachment.setCreatedTime(LocalDateTime.now());
        try {
            attachment.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("讀取上傳檔案失敗：" + file.getOriginalFilename(), e);
        }

        WorkOrderAttachment saved = workOrderAttachmentRepository.save(attachment);
        return toResponse(saved);
    }

    // 批次上傳：任何一張驗證失敗就整批中斷，不會部分成功。uploadedUserId 只查一次
    @Transactional
    public List<WorkOrderAttachmentResponse> uploadAll(WorkOrder workOrder, List<MultipartFile> files,
            Integer uploadedUserId) {
        if (files == null) {
            return List.of();
        }
        User uploadedUser = userRepository.findById(uploadedUserId)
                .orElseThrow(() -> new IllegalArgumentException("找不到使用者：" + uploadedUserId));

        return files.stream()
                .map(file -> upload(workOrder, file, uploadedUser))
                .collect(Collectors.toList());
    }

    // 查詢某工單的附件中繼資料列表（不含二進位檔案內容）
    public List<WorkOrderAttachmentResponse> listByWorkOrder(Integer workOrderId) {
        return workOrderAttachmentRepository.findByWorkOrder_WorkOrderId(workOrderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 取得單筆附件完整資料（含 fileData），給 controller 組 inline 預覽回應
    public WorkOrderAttachment view(Integer attachmentId) {
        return workOrderAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("找不到附件：" + attachmentId));
    }

    // 刪除附件：僅限上傳者本人
    @Transactional
    public void delete(Integer attachmentId, Integer requestUserId) {
        WorkOrderAttachment attachment = workOrderAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("找不到附件：" + attachmentId));

        if (!attachment.getUploadedUser().getUserId().equals(requestUserId)) {
            throw new IllegalStateException("只有上傳者本人可以刪除此附件");
        }

        workOrderAttachmentRepository.delete(attachment);
    }

    // 附件 DTO 組裝
    private WorkOrderAttachmentResponse toResponse(WorkOrderAttachment attachment) {
        return WorkOrderAttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .originalFileName(attachment.getOriginalFileName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .createdTime(attachment.getCreatedTime())
                .uploadedUserName(attachment.getUploadedUser().getName())
                .build();
    }
}
