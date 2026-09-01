package com.eeit219.work_order_system.modules.c.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.exception.AiSuggestionUnavailableException;
import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.AdminArchiveContext;
import com.eeit219.work_order_system.modules.c.dto.AdminArchiveContext.HistorySource;
import com.eeit219.work_order_system.modules.c.dto.AdminArchiveSuggestionResponse;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.c.service.SpringAiGeminiArchiveClient.Evidence;
import com.eeit219.work_order_system.modules.c.service.SpringAiGeminiArchiveClient.RawArchiveSuggestion;

@Service
public class AdminArchiveSuggestionService {

    private static final String WORK_ORDER_SOURCE_ID = "WORK_ORDER_DESCRIPTION";
    private static final List<String> FIELD_NAMES = List.of(
            "failureCause", "repairAction", "replacedParts", "testResult");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+?886[- ]?)?0?9\\d{2}[- ]?\\d{3}[- ]?\\d{3}(?!\\d)");

    private final WorkOrderRepository workOrderRepository;
    private final RepairTicketHistoryRepository historyRepository;
    private final SpringAiGeminiArchiveClient geminiClient;

    public AdminArchiveSuggestionService(
            WorkOrderRepository workOrderRepository,
            RepairTicketHistoryRepository historyRepository,
            SpringAiGeminiArchiveClient geminiClient) {
        this.workOrderRepository = workOrderRepository;
        this.historyRepository = historyRepository;
        this.geminiClient = geminiClient;
    }

    @Transactional(readOnly = true)
    public AdminArchiveSuggestionResponse generate(Integer workOrderId, Integer userId) {
        WorkOrder workOrder = workOrderRepository.findByIdWithDetails(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到此工單"));

        if (workOrder.getStatus() != WorkOrderState.PENDING_ADMIN_ACCEPTANCE) {
            throw new InvalidWorkOrderStateException("只有待管理員驗收的工單可以產生 AI 歸檔建議");
        }
        if (workOrder.getAdmin() == null || !workOrder.getAdmin().getUserId().equals(userId)) {
            throw new AccessDeniedException("只有此工單的負責管理員可以產生 AI 歸檔建議");
        }
        // 用功單id查詢歷史記錄
        List<RepairTicketHistory> histories = historyRepository
                .findByWorkOrderWorkOrderIdOrderByEditedTimeAscHistoryIdAsc(workOrderId);

        // 過濾掉沒有反饋內容的歷史紀錄，並轉換成提供給 AI 使用的時間線格式
        List<HistorySource> timeline = histories.stream()
                .filter(history -> history.getFeedback() != null && !history.getFeedback().isBlank())
                .map(this::toHistorySource)
                .toList();

        // 取得最新的歷史紀錄
        Integer latestHistoryId = histories.isEmpty()
                ? null
                : histories.get(histories.size() - 1).getHistoryId();
        // 整理工單基本資料與歷史反饋，建立提供給 Gemini 分析的上下文
        AdminArchiveContext context = new AdminArchiveContext(
                WORK_ORDER_SOURCE_ID,
                workOrder.getWorkOrderId(),
                sanitize(workOrder.getWorkOrderNo()),
                sanitize(workOrder.getTitle()),
                sanitize(workOrder.getDescription()),
                workOrder.getVersion(),
                latestHistoryId,
                timeline);

        RawArchiveSuggestion raw = geminiClient.suggest(context);
        return validate(raw, context);
    }

    /**
     * 將資料庫中的單筆工單歷史紀錄，
     * 轉換成提供給 Gemini 分析的 HistorySource 格式。
     *
     * @param history 資料庫中的工單歷史紀錄
     * @return 包含來源 ID、流程階段、狀態、事件、時間及反饋的 AI 資料來源
     */
    private HistorySource toHistorySource(RepairTicketHistory history) {
        return new HistorySource(
                // 建立證據來源 ID，例如歷史紀錄 ID 25 會轉成 H25
                "H" + history.getHistoryId(),

                // 根據工單狀態與事件，轉換成容易理解的中文流程階段
                stageOf(history.getStatus(), history.getEvent()),

                // 取得歷史紀錄的工單狀態，並將 Enum 轉成字串
                history.getStatus().name(),

                // 取得這筆歷史紀錄的操作事件，例如 ACCEPT 或 REJECT
                history.getEvent().name(),

                // 取得這筆歷史紀錄發生的時間
                history.getEditedTime(),

                // 清理反饋內容並遮蔽 Email、手機號碼等個人資料
                sanitize(history.getFeedback()));
    }

    private AdminArchiveSuggestionResponse validate(
            RawArchiveSuggestion raw,
            AdminArchiveContext context) {
        Set<String> validSourceIds = new LinkedHashSet<>();
        validSourceIds.add(WORK_ORDER_SOURCE_ID);
        context.timeline().forEach(item -> validSourceIds.add(item.sourceId()));

        Map<String, List<String>> evidence = new LinkedHashMap<>();
        evidence.put("failureCause", validEvidence(raw.evidence(), "failureCause", validSourceIds));
        evidence.put("repairAction", validEvidence(raw.evidence(), "repairAction", validSourceIds));
        evidence.put("replacedParts", validEvidence(raw.evidence(), "replacedParts", validSourceIds));
        evidence.put("testResult", validEvidence(raw.evidence(), "testResult", validSourceIds));

        String failureCause = validatedValue(raw.failureCause(), 100, evidence.get("failureCause"));
        String repairAction = validatedValue(raw.repairAction(), 150, evidence.get("repairAction"));
        String replacedParts = validatedValue(raw.replacedParts(), 80, evidence.get("replacedParts"));
        String testResult = validatedValue(raw.testResult(), 100, evidence.get("testResult"));

        List<String> insufficientFields = new ArrayList<>();
        addIfMissing(insufficientFields, "failureCause", failureCause);
        addIfMissing(insufficientFields, "repairAction", repairAction);
        addIfMissing(insufficientFields, "replacedParts", replacedParts);
        addIfMissing(insufficientFields, "testResult", testResult);

        return new AdminArchiveSuggestionResponse(
                failureCause,
                repairAction,
                replacedParts,
                testResult,
                List.copyOf(insufficientFields),
                Map.copyOf(evidence),
                context.timeline().size() + 1,
                context.workOrderVersion(),
                context.latestHistoryId());
    }

    private List<String> validEvidence(Evidence evidence, String field, Set<String> validSourceIds) {
        if (evidence == null) {
            return List.of();
        }
        List<String> sourceIds = switch (field) {
            case "failureCause" -> evidence.failureCause();
            case "repairAction" -> evidence.repairAction();
            case "replacedParts" -> evidence.replacedParts();
            case "testResult" -> evidence.testResult();
            default -> List.of();
        };
        if (sourceIds == null) {
            return List.of();
        }
        return sourceIds.stream()
                .filter(validSourceIds::contains)
                .distinct()
                .toList();
    }

    private String validatedValue(String value, int maxLength, List<String> evidence) {
        String normalized = normalize(value);
        if (normalized == null || evidence.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new AiSuggestionUnavailableException("AI 回傳內容超過欄位字數限制，請重試或手動填寫");
        }
        return normalized;
    }

    private void addIfMissing(List<String> fields, String field, String value) {
        if (value == null && FIELD_NAMES.contains(field)) {
            fields.add(field);
        }
    }

    private String stageOf(WorkOrderState status, WorkOrderEvent event) {
        if (status == WorkOrderState.IN_PROGRESS && event == WorkOrderEvent.ACCEPT) {
            return "管理員審核與派工";
        }
        if (status == WorkOrderState.PENDING_REVIEW && event == WorkOrderEvent.REJECT) {
            return "工程師退回";
        }
        if (status == WorkOrderState.PENDING_USER_ACCEPTANCE && event == WorkOrderEvent.ACCEPT) {
            return "工程師回報完成";
        }
        if (status == WorkOrderState.PENDING_ADMIN_ACCEPTANCE && event == WorkOrderEvent.ACCEPT) {
            return "報修人驗收";
        }
        if (status == WorkOrderState.IN_PROGRESS && event == WorkOrderEvent.REJECT) {
            return "管理員退回重修";
        }
        if (status == WorkOrderState.CANCELLED && event == WorkOrderEvent.REJECT) {
            return "管理員拒絕工單";
        }
        return status.name() + " / " + event.name();
    }

    private String sanitize(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String withoutEmail = EMAIL_PATTERN.matcher(normalized).replaceAll("[EMAIL]");
        return MOBILE_PATTERN.matcher(withoutEmail).replaceAll("[PHONE]");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("[\\p{Cc}&&[^\\r\\n\\t]]", "").strip();
    }
}
