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

    // 5.1 依設備型號統計報表 (支援日期區間過濾)
    public List<CategoryReportDto> getEquipmentModelReport(LocalDate startDate, LocalDate endDate) {
        return workOrderRepository.countWorkOrdersByEquipmentModel(toStartOfDay(startDate), toEndOfDay(endDate));
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
        // 從歷史資料庫撈取全量歷程 (按時間由舊到新 editedTime ASC 排序，確保精準抓到最正確的 IN_PROGRESS 時間點)
        List<RepairTicketHistory> histories = historyRepository.findHistoryForKpiReport(null, toEndOfDay(endDate));

        // 轉換前端傳入的開始日期為當天 00:00:00 的 LocalDateTime
        LocalDateTime filterStart = toStartOfDay(startDate);

        // ---- 準備 4 個 Map 資料結構紀錄累積數據 ----
        Map<Integer, LocalDateTime> lastStartTimes = new HashMap<>(); // Key:工單ID ➡️ Value:最新一次接單開始時間
        Map<Integer, String> engineerNames = new HashMap<>();         // Key:工程師ID ➡️ Value:工程師姓名
        Map<Integer, Long> countMap = new HashMap<>();                 // Key:工程師ID ➡️ Value:完工總筆數
        Map<Integer, Long> totalMinutesMap = new HashMap<>();          // Key:工程師ID ➡️ Value:累積維修總分鐘數

        // 逐筆巡迴歷程 (editedTime ASC 由舊到新)
        for (RepairTicketHistory h : histories) {
            // 防呆判斷：若歷史紀錄缺少工單或操作者資料，直接跳過 (避免 NullPointerException)
            if (h.getWorkOrder() == null || h.getEditor() == null) continue;
            Integer ticketId = h.getWorkOrder().getWorkOrderId();
            Integer editorId = h.getEditor().getUserId();

            // 【動作一】捕捉「開始維修時間」
            if (h.getStatus() == WorkOrderState.IN_PROGRESS) {
                // 每當發現狀態變成 IN_PROGRESS，紀錄或更新該工單最新一次開始維修時間
                lastStartTimes.put(ticketId, h.getEditedTime());
            } 
            // 【動作二】捕捉「完工結算時間」並計算耗時
            else if (h.getStatus() == WorkOrderState.COMPLETED || h.getStatus() == WorkOrderState.PENDING_USER_ACCEPTANCE) {
                LocalDateTime endTime = h.getEditedTime();

                // 日期過濾：若有設定開始日期，且完工時間早於開始日期則跳過不採計
                if (filterStart != null && endTime.isBefore(filterStart)) {
                    continue;
                }

                // 取得這張工單的開始時間 (若無 IN_PROGRESS 歷程則預設使用工單建立時間作為起點)
                LocalDateTime startTime = lastStartTimes.get(ticketId);
                if (startTime == null) {
                    startTime = h.getWorkOrder().getCreatedTime();
                }

                // 確保完工時間晚於開始時間，計算時間差並進行數據累加
                if (startTime != null && endTime.isAfter(startTime)) {
                    // 使用 Duration.between 精準計算相差的分鐘數
                    long minutes = Duration.between(startTime, endTime).toMinutes();

                    // 1. 登記工程師姓名
                    engineerNames.put(editorId, h.getEditor().getName() != null ? h.getEditor().getName() : "工程師 " + editorId);
                    // 2. 完工筆數 +1 (getOrDefault 預設為 0)
                    countMap.put(editorId, countMap.getOrDefault(editorId, 0L) + 1);
                    // 3. 累積維修總分鐘數
                    totalMinutesMap.put(editorId, totalMinutesMap.getOrDefault(editorId, 0L) + minutes);
                }
            }
        }

        // ---- 組裝產出 DTO 結果列表 ----
        List<EngineerKpiReportDto> result = new ArrayList<>();
        for (Integer editorId : engineerNames.keySet()) {
            long count = countMap.getOrDefault(editorId, 0L);
            long totalMin = totalMinutesMap.getOrDefault(editorId, 0L);
            
            // 計算個人平均小時數 (保留兩位小數) 與個人平均分鐘數
            double avgHours = count > 0 ? Math.round((totalMin / 60.0 / count) * 100.0) / 100.0 : 0.0;
            long avgMin = count > 0 ? totalMin / count : 0L;

            result.add(new EngineerKpiReportDto(editorId, engineerNames.get(editorId), count, avgHours, avgMin));
        }

        // 依完工筆數高到低進行排序 (高到低)
        result.sort((a, b) -> Long.compare(b.getCompletedCount(), a.getCompletedCount()));
        return result;
    }
}