package com.eeit219.work_order_system.modules.c.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

@Service
public class WorkOrderStateMachineService {

    @Transactional
    public void changeState(
            Long workOrderId,
            WorkOrderEvent event) {

        // 1. 從資料庫讀取工單
        // 2. 取得工單目前狀態
        // 3. 將狀態機重設為目前狀態
        // 4. 發送 event
        // 5. 確認事件是否成功
        // 6. 把新狀態寫回資料庫
        // 7. 同時寫入異動紀錄或其他資料
    }
}