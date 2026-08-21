package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.exception.EditSessionLockedException;
import com.eeit219.work_order_system.common.exception.InvalidEditSessionException;
import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.EditSessionResponse;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketEditSession;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketEditSessionRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditSessionService {

    private static final long IDLE_TIMEOUT_MINUTES = 5;
    private static final long MAX_SESSION_MINUTES = 20;

    private final RepairTicketEditSessionRepository editSessionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    /**
     * 管理員點擊「編輯」時呼叫。
     */
    @Transactional
    public EditSessionResponse startEditSession(
            Integer workOrderId,
            Integer userId) {

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單：" + workOrderId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者：" + userId));
                
        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new InvalidWorkOrderStateException(
                    "目前不是待審查狀態");
        }

        if (workOrder.getAdmin() != null) {
            throw new InvalidEditSessionException(
                    "此工單已有負責管理員，不需要取得編輯權");
        }
        LocalDateTime now = LocalDateTime.now();

        RepairTicketEditSession session = editSessionRepository
                .findById(workOrderId)
                .orElse(null);

        // 這張工單目前沒有人編輯，建立新的 Session。
        if (session == null) {
            session = new RepairTicketEditSession();

            // @MapsId 會自動使用 workOrder 的 ID，
            // 不需要另外 setTicketId()。
            session.setWorkOrder(workOrder);
            session.setUser(user);
            session.setSessionToken(generateToken());
            session.setStartTime(now);
            session.setLastActiveTime(now);

            editSessionRepository.save(session);

            return toResponse(session);
        }

        boolean sameUser = Objects.equals(
                session.getUser().getUserId(),
                userId);

        boolean expired = isExpired(session, now);

        // Session 還有效。
        if (!expired) {
            // 目前是其他管理員持有編輯權。
            if (!sameUser) {
                throw new EditSessionLockedException(
                        session.getUser().getName());
            }

            // 同一位管理員再次點擊編輯，視為繼續編輯。
            session.setLastActiveTime(now);

            return toResponse(session);
        }

        /*
         * Session 已經過期，不論原本是不是同一位管理員，
         * 都重新取得編輯權並產生新的 Token。
         */
        session.setUser(user);
        session.setSessionToken(generateToken());
        session.setStartTime(now);
        session.setLastActiveTime(now);

        return toResponse(session);
    }

    /**
     * 前端每兩分鐘呼叫一次。
     */
    @Transactional
    public void heartbeat(
            Integer workOrderId,
            Integer userId,
            String token) {

        LocalDateTime now = LocalDateTime.now();

        RepairTicketEditSession session = requireValidSession(
                workOrderId,
                userId,
                token,
                now);

        session.setLastActiveTime(now);

        /*
         * 不需要再呼叫 save()。
         * 因為 session 是在 Transaction 中查出的 managed entity，
         * Hibernate 會在交易提交時自動 UPDATE。
         */
    }

    /**
     * 管理員送出初審接受或拒絕前呼叫。
     */
    @Transactional(readOnly = true)
    public void validate(
            Integer workOrderId,
            Integer userId,
            String token) {

        requireValidSession(
                workOrderId,
                userId,
                token,
                LocalDateTime.now());
    }

    /**
     * 儲存成功或取消編輯時呼叫。
     */
    @Transactional
    public void release(
            Integer workOrderId,
            Integer userId,
            String token) {

        RepairTicketEditSession session = editSessionRepository
                .findById(workOrderId)
                .orElse(null);

        // 已經被刪除時，重複 release 不需要報錯。
        if (session == null) {
            return;
        }

        boolean sameUser = Objects.equals(
                session.getUser().getUserId(),
                userId);

        boolean sameToken = token != null
                && !token.isBlank()
                && Objects.equals(session.getSessionToken(), token);

        /*
         * 一定要同時檢查 userId 和 token，
         * 避免舊的管理員刪除新管理員的 Session。
         */
        if (!sameUser || !sameToken) {
            throw new InvalidEditSessionException(
                    "無法釋放編輯權：編輯憑證不正確");
        }

        editSessionRepository.delete(session);
    }

    /**
     * 驗證 Session 是否存在、使用者相同、Token 相同且未過期。
     */
    private RepairTicketEditSession requireValidSession(
            Integer workOrderId,
            Integer userId,
            String token,
            LocalDateTime now) {

        RepairTicketEditSession session = editSessionRepository
                .findById(workOrderId)
                .orElseThrow(() -> new InvalidEditSessionException(
                        "找不到編輯階段，請重新取得編輯權"));

        if (!Objects.equals(
                session.getUser().getUserId(),
                userId)) {

            throw new InvalidEditSessionException(
                    "你不是目前擁有此工單編輯權的管理員");
        }

        if (token == null
                || token.isBlank()
                || !Objects.equals(
                        session.getSessionToken(),
                        token)) {

            throw new InvalidEditSessionException(
                    "編輯憑證無效，請重新取得編輯權");
        }

        if (isExpired(session, now)) {
            throw new InvalidEditSessionException(
                    "編輯階段已經過期，請重新取得編輯權");
        }

        return session;
    }

    /**
     * 閒置超過 5 分鐘，或總編輯時間超過 20 分鐘，即為過期。
     */
    private boolean isExpired(
            RepairTicketEditSession session,
            LocalDateTime now) {

        LocalDateTime idleExpiresAt = session
                .getLastActiveTime()
                .plusMinutes(IDLE_TIMEOUT_MINUTES);

        LocalDateTime maxExpiresAt = session
                .getStartTime()
                .plusMinutes(MAX_SESSION_MINUTES);

        return !now.isBefore(idleExpiresAt)
                || !now.isBefore(maxExpiresAt);
    }

    /**
     * 回傳給前端的實際到期時間，
     * 取「閒置到期」與「最大時間到期」中較早的一個。
     */
    private EditSessionResponse toResponse(
            RepairTicketEditSession session) {

        LocalDateTime idleExpiresAt = session
                .getLastActiveTime()
                .plusMinutes(IDLE_TIMEOUT_MINUTES);

        LocalDateTime maxExpiresAt = session
                .getStartTime()
                .plusMinutes(MAX_SESSION_MINUTES);

        LocalDateTime expiresAt = idleExpiresAt.isBefore(maxExpiresAt)
                ? idleExpiresAt
                : maxExpiresAt;

        return new EditSessionResponse(
                session.getSessionToken(),
                session.getUser().getUserId(),
                session.getUser().getName(),
                session.getStartTime(),
                session.getLastActiveTime(),
                expiresAt);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}