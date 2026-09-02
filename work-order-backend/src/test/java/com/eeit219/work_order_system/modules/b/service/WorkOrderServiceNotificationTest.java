package com.eeit219.work_order_system.modules.b.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

// 整合測試：驗證交易 rollback 時通知真的沒發出去，這件事沒辦法用純 Mockito 單元測試驗證，
// 一定要讓 WorkOrderService 是真的被 Spring 容器管理、@Transactional 代理實際生效才測得到。
// 依賴本機 application.properties 指到的資料庫，跑之前要先確認 seed data 存在（sub_categories_id=1、user_id=6）。
@SpringBootTest
class WorkOrderServiceNotificationTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void create_doesNotNotifyAdmins_whenAttachmentValidationFailsAndTransactionRollsBack() {
        User creator = userRepository.findById(6)
                .orElseThrow(() -> new IllegalStateException("測試需要種子資料 user_id=6（emp01）"));

        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("測試工單");
        request.setSubCategoryId(1);
        request.setLocationDetail("測試地點");

        // 內容不是圖片但自報 content-type 是圖片：會通過前面欄位檢查，卡在 ImageIO.read() 那關
        MockMultipartFile invalidImage = new MockMultipartFile(
                "files", "fake.png", "image/png", "not a real image".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class,
                () -> workOrderService.create(request, creator, List.of(invalidImage)));

        // 交易 rollback，afterCommit 不會被觸發，通知（含 WebSocket 推播）不該被呼叫
        verifyNoInteractions(notificationService);
    }
}
