package com.eeit219.work_order_system.modules.e.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.dto.DailyReportDto;
import com.eeit219.work_order_system.modules.e.dto.EngineerKpiReportDto;
import com.eeit219.work_order_system.modules.e.dto.MonthlyReportDto;
import com.eeit219.work_order_system.modules.e.repository.ReportWorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportWorkOrderRepository workOrderRepository;
    private final RepairTicketHistoryRepository historyRepository;

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    // 1. 取得大分類統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getCategoryReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersByCategory(toStartOfDay(startDate), toEndOfDay(endDate));
    }

    // 2. 取得細項分類統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getSubCategoryReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersBySubCategory(toStartOfDay(startDate), toEndOfDay(endDate));
    }

    // 3. 依狀態統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getStatusReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersByStatus(toStartOfDay(startDate), toEndOfDay(endDate));
    }

    // 4. 依工單建立者統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getCreatorReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersByCreator(toStartOfDay(startDate), toEndOfDay(endDate));
    }

    // 5. 依優先級統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getPriorityReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersByPriority(toStartOfDay(startDate), toEndOfDay(endDate));
    }

    // 6. 依月份群組統計報表 (折線圖用)
    public List<MonthlyReportDto> getMonthlyReport(Integer year) {
        if (year != null) {
            return workOrderRepository.countWorkOrdersByMonthAndYear(year);
        }
        return workOrderRepository.countWorkOrdersByMonth();
    }

    // 7. 依每日群組統計報表 (折線圖用，支援年份與月份過濾)
    public List<DailyReportDto> getDailyReport(Integer year, Integer month) {
        return workOrderRepository.countWorkOrdersByDaily(year, month);
    }

    // 列出目前資料庫內的所有工單
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    // 8. 取得工程師處理 KPI 統計報表 (支援日期過濾)
    public List<EngineerKpiReportDto> getEngineerKpiReport(LocalDate startDate, LocalDate endDate) {
        List<RepairTicketHistory> histories = historyRepository.findHistoryForKpiReport(null, toEndOfDay(endDate));

        LocalDateTime filterStart = toStartOfDay(startDate);

        Map<Integer, LocalDateTime> lastStartTimes = new HashMap<>();
        Map<Integer, String> engineerNames = new HashMap<>();
        Map<Integer, Long> countMap = new HashMap<>();
        Map<Integer, Long> totalMinutesMap = new HashMap<>();

        for (RepairTicketHistory h : histories) {
            if (h.getWorkOrder() == null || h.getEditor() == null) continue;
            Integer ticketId = h.getWorkOrder().getWorkOrderId();
            Integer editorId = h.getEditor().getUserId();

            if (h.getStatus() == WorkOrderState.IN_PROGRESS) {
                // 記錄或更新該工單最新一次開始維修時間
                lastStartTimes.put(ticketId, h.getEditedTime());
            } else if (h.getStatus() == WorkOrderState.COMPLETED || h.getStatus() == WorkOrderState.PENDING_USER_ACCEPTANCE) {
                LocalDateTime endTime = h.getEditedTime();

                // 若有設定開始日期，且完工時間早於開始日期則跳過
                if (filterStart != null && endTime.isBefore(filterStart)) {
                    continue;
                }

                // 取得開始時間 (若無 IN_PROGRESS 歷程則預設用工單建立時間)
                LocalDateTime startTime = lastStartTimes.get(ticketId);
                if (startTime == null) {
                    startTime = h.getWorkOrder().getCreatedTime();
                }

                if (startTime != null && endTime.isAfter(startTime)) {
                    long minutes = Duration.between(startTime, endTime).toMinutes();
                    engineerNames.put(editorId, h.getEditor().getName() != null ? h.getEditor().getName() : "工程師 " + editorId);
                    countMap.put(editorId, countMap.getOrDefault(editorId, 0L) + 1);
                    totalMinutesMap.put(editorId, totalMinutesMap.getOrDefault(editorId, 0L) + minutes);
                }
            }
        }

        List<EngineerKpiReportDto> result = new ArrayList<>();
        for (Integer editorId : engineerNames.keySet()) {
            long count = countMap.getOrDefault(editorId, 0L);
            long totalMin = totalMinutesMap.getOrDefault(editorId, 0L);
            double avgHours = count > 0 ? Math.round((totalMin / 60.0 / count) * 100.0) / 100.0 : 0.0;
            long avgMin = count > 0 ? totalMin / count : 0L;

            result.add(new EngineerKpiReportDto(editorId, engineerNames.get(editorId), count, avgHours, avgMin));
        }

        result.sort((a, b) -> Long.compare(b.getCompletedCount(), a.getCompletedCount()));
        return result;
    }
}