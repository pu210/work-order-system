package com.eeit219.work_order_system.modules.d.controller;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrderAttachment;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordMultipartCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.service.ContactRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/contact-records")
@RequiredArgsConstructor
public class ContactRecordController {

    private final ContactRecordService contactRecordService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactRecordResponse>>> getRecords(
            @PathVariable Integer workOrderId,
            Authentication authentication) {

        List<ContactRecordResponse> records = contactRecordService.getRecords(
                workOrderId,
                getCurrentUser(authentication)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "取得聯絡紀錄成功",
                records
        ));
    }

    // 純文字留言
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ContactRecordResponse>> createComment(
            @PathVariable Integer workOrderId,
            @Valid
            @RequestBody ContactRecordCreateRequest request,
            Authentication authentication) {

        ContactRecordResponse record = contactRecordService.createComment(
                workOrderId,
                request,
                getCurrentUser(authentication)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                "新增留言成功",
                record
        ));
    }

    private User getCurrentUser(Authentication authentication) {
        String account = authentication.getName();
        return userRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalStateException("找不到使用者：" + account));
    }

    // 文字or圖片留言，兩者至少提供一項
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ContactRecordResponse>> createCommentWithImages(
            @PathVariable Integer workOrderId,
            @Valid
            @ModelAttribute ContactRecordMultipartCreateRequest request,
            @RequestPart(value = "files", required = false)
            List<MultipartFile> files,
            Authentication authentication) {

        ContactRecordResponse record =
                contactRecordService.createCommentWithImages(
                        workOrderId,
                        request,
                        files,
                        getCurrentUser(authentication)
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                "新增留言成功",
                record
        ));
    }

    @GetMapping("/{recordId}/attachments/{attachmentId}/view")
    public ResponseEntity<byte[]> viewCommentAttachment(
            @PathVariable Integer workOrderId,
            @PathVariable Integer recordId,
            @PathVariable Integer attachmentId,
            Authentication authentication){

        WorkOrderAttachment attachment =
                contactRecordService.getCommentAttachmentForView(
                        workOrderId,
                        recordId,
                        attachmentId,
                        getCurrentUser(authentication)
                );

        // 只有通過工單查看權限，且附件確實屬於指定工單與聯繫紀錄時，才會以圖片二進位內容回傳
        // 原始圖片類型和inline檔名回傳圖片二進位內容。
        MediaType responseContentType;
        if (attachment.getContentType() != null){
            responseContentType =
                    MediaType.parseMediaType(attachment.getContentType());
        } else {
            responseContentType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        String encodedFileName =
                java.net.URLEncoder
                        .encode(
                                attachment.getOriginalFileName(),
                                StandardCharsets.UTF_8
                        )
                        .replace("+", "%20");

        // 可讓瀏覽器直接預覽的圖片回應
        return ResponseEntity.ok()
                .contentType(responseContentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedFileName
                )
                .body(attachment.getFileData());

    }
}
