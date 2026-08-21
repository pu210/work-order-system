package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.AuthenticatedUser;
import com.eeit219.work_order_system.modules.c.dto.EditSessionResponse;
import com.eeit219.work_order_system.modules.c.service.EditSessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/review/edit-session")
@RequiredArgsConstructor
public class EditSessionController {

    private static final String EDIT_SESSION_TOKEN_HEADER =
            "X-Edit-Session-Token";

    private final EditSessionService editSessionService;

    /**
     * 管理員點擊「編輯」時呼叫。
     *
     * 這時還沒有 sessionToken，
     * 成功後由後端產生並回傳給前端。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EditSessionResponse>>
            startEditSession(
                    @PathVariable Integer workOrderId,
                    @AuthenticationPrincipal
                    AuthenticatedUser loginUser) {

        EditSessionResponse response =
                editSessionService.startEditSession(
                        workOrderId,
                        loginUser.userId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "取得工單編輯權成功",
                        response));
    }

    /**
     * 前端每兩分鐘呼叫一次，更新 lastActiveTime。
     */
    @PatchMapping("/heartbeat")
    public ResponseEntity<ApiResponse<Void>> heartbeat(
            @PathVariable Integer workOrderId,
            @RequestHeader(EDIT_SESSION_TOKEN_HEADER)
            String sessionToken,
            @AuthenticationPrincipal
            AuthenticatedUser loginUser) {

        editSessionService.heartbeat(
                workOrderId,
                loginUser.userId(),
                sessionToken);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        HttpStatus.OK.value(),
                        "編輯階段續期成功",
                        null));
    }

    /**
     * 管理員取消編輯時呼叫。
     *
     * 審核成功時也會由 ReviewService 呼叫 release，
     * 所以前端不用在成功後重複呼叫。
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> release(
            @PathVariable Integer workOrderId,
            @RequestHeader(EDIT_SESSION_TOKEN_HEADER)
            String sessionToken,
            @AuthenticationPrincipal
            AuthenticatedUser loginUser) {

        editSessionService.release(
                workOrderId,
                loginUser.userId(),
                sessionToken);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        HttpStatus.OK.value(),
                        "已釋放工單編輯權",
                        null));
    }
}