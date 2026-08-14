package com.eeit219.work_order_system.modules.b.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.b.service.WorkOrderAttachmentService;

@RestController
public class WorkOrderAttachmentController {

    private final WorkOrderAttachmentService workOrderAttachmentService;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public WorkOrderAttachmentController(WorkOrderAttachmentService workOrderAttachmentService,
            WorkOrderRepository workOrderRepository,
            UserRepository userRepository) {
        this.workOrderAttachmentService = workOrderAttachmentService;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
    }

    // 事後補上傳附件（建單流程本身不夾帶檔案）。前端建單頁面若同時選了圖片，UX 上看起來是同一次送出，
    // 但實際是建單成功拿到 workOrderId 後才呼叫這支，兩者非同一交易，這支失敗不會讓工單本體跟著回滾
    @PostMapping(value = "/api/work-orders/{workOrderId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<WorkOrderAttachmentResponse>>> upload(@PathVariable Integer workOrderId,
            @RequestPart("files") List<MultipartFile> files) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到工單：" + workOrderId));

        List<WorkOrderAttachmentResponse> response = workOrderAttachmentService.uploadAll(workOrder, files,
                currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "附件上傳成功",
                        response));
    }

    // 查詢某工單的附件列表
    @GetMapping("/api/work-orders/{workOrderId}/attachments")
    public ResponseEntity<ApiResponse<List<WorkOrderAttachmentResponse>>> list(@PathVariable Integer workOrderId) {
        List<WorkOrderAttachmentResponse> response = workOrderAttachmentService.listByWorkOrder(workOrderId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "成功", response));
    }

    // 預覽附件：inline 回傳給前端直接渲染
    @GetMapping("/api/work-orders/attachments/{attachmentId}/view")
    public ResponseEntity<byte[]> view(@PathVariable Integer attachmentId) {
        WorkOrderAttachment attachment = workOrderAttachmentService.view(attachmentId);

        String encodedFileName = java.net.URLEncoder
                .encode(attachment.getOriginalFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(attachment.getContentType() != null
                        ? MediaType.parseMediaType(attachment.getContentType())
                        : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName)
                .body(attachment.getFileData());
    }

    // 刪除附件：僅限上傳者本人（見 WorkOrderAttachmentService.delete）
    @DeleteMapping("/api/work-orders/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer attachmentId) {
        workOrderAttachmentService.delete(attachmentId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "附件刪除成功", null));
    }

    // 從 JWT 解析出的登入帳號（account），查出對應的 userId
    private Integer currentUserId() {
        String account = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalStateException("找不到登入使用者：" + account))
                .getUserId();
    }
}
