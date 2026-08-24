-- ============================================================================
-- 假資料腳本 (Mock Data) - 依 db_2_5.sql schema 產生
-- 已依你提供的 users INSERT 資料對齊 user_id（帳號依 INSERT 順序自動編號 1~15）：
--   user_id = 1        -> admin      系統管理員
--   user_id = 2 ~ 5     -> handler01~04 維修人員（陳志明/林淑芬/黃建宏/張雅婷）
--   user_id = 6 ~ 15    -> emp01~10  一般員工（報修人）
--     其中 user_id = 13 (emp08) 帳號狀態 status=2 待審核
--          user_id = 14 (emp09) 帳號狀態 status=0 停用（仍保留其歷史工單資料）
-- 本腳本不包含 users / user_roles / password_reset_tokens /
-- user_oauth_accounts 這四張純帳號表，請先自行執行你提供的 users INSERT，
-- 並視需要補上 user_roles 對應（1->ADMIN, 2~5->HANDLER, 6~15->EMPLOYEE）。
-- 執行順序：db_2_5.sql 建表 -> users INSERT -> 本腳本。
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. priorities 優先級別
-- ----------------------------------------------------------------------------


INSERT INTO users (account, password_hash, must_change_password, name, email, phone, status) VALUES
('admin',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'系統管理員', 'admin@company.com',    '0900000000', 1),
('handler01', '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'陳志明',     'handler01@company.com', '0911111111', 1),
('handler02', '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'林淑芬',     'handler02@company.com', '0911111112', 1),
('handler03', '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'黃建宏',     'handler03@company.com', '0911111113', 1),
('handler04', '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'張雅婷',     'handler04@company.com', '0911111114', 1),
('emp01',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 1, N'王小明',     'emp01@company.com',     '0922222221', 1),
('emp02',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 1, N'李美玲',     'emp02@company.com',     '0922222222', 1),
('emp03',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'吳俊傑',     'emp03@company.com',     '0922222223', 1),
('emp04',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'蔡佳蓉',     'emp04@company.com',     '0922222224', 1),
('emp05',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 1, N'許家豪',     'emp05@company.com',     '0922222225', 1),
('emp06',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'鄭雅文',     'emp06@company.com',     '0922222226', 1),
('emp07',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'謝承翰',     'emp07@company.com',     '0922222227', 1),
('emp08',     NULL,                                                          1, N'周奕辰',     'emp08@company.com',     '0922222228', 2),
('emp09',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'徐郁婷',     'emp09@company.com',     '0922222229', 0),
('emp10',     '$2b$10$ijvnB3Dlv8pi3..wbmByJOfhWOwkcn6YlEHUDshVrbbPa2Yd5IXwG', 0, N'何柏翰',     'emp10@company.com',     '0922222230', 1);


SET IDENTITY_INSERT priorities ON;
INSERT INTO priorities (priorities_id, name, hours, status) VALUES
(1, N'緊急', 4, 1),
(2, N'高', 8, 1),
(3, N'中', 24, 1),
(4, N'低', 72, 1);
SET IDENTITY_INSERT priorities OFF;
GO

-- ----------------------------------------------------------------------------
-- 2. repair_categories 維修大類別
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT repair_categories ON;
INSERT INTO repair_categories (repair_categories_id, name, default_priority_id, status, created_time, updated_time, default_priority_name) VALUES
(1, N'電力設備', 2, 1, '2026-01-05 09:00:00', '2026-01-05 09:00:00', N'高'),
(2, N'空調系統', 2, 1, '2026-01-05 09:05:00', '2026-01-05 09:05:00', N'高'),
(3, N'網路資訊', 1, 1, '2026-01-05 09:10:00', '2026-01-05 09:10:00', N'緊急'),
(4, N'給排水設備', 3, 1, '2026-01-05 09:15:00', '2026-01-05 09:15:00', N'中'),
(5, N'建築設施', 4, 1, '2026-01-05 09:20:00', '2026-01-05 09:20:00', N'低'),
(6, N'消防安全', 1, 1, '2026-01-05 09:25:00', '2026-01-05 09:25:00', N'緊急');
SET IDENTITY_INSERT repair_categories OFF;
GO

-- ----------------------------------------------------------------------------
-- 3. sub_categories 子類別
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT sub_categories ON;
INSERT INTO sub_categories (sub_categories_id, category_id, name, override_priority_id, status, created_time, updated_time, override_priority_name) VALUES
(1, 1, N'電燈故障', NULL, 1, '2026-01-06 09:00:00', '2026-01-06 09:00:00', NULL),
(2, 1, N'插座異常', NULL, 1, '2026-01-06 09:01:00', '2026-01-06 09:01:00', NULL),
(3, 1, N'配電盤跳電', 1, 1, '2026-01-06 09:02:00', '2026-01-06 09:02:00', N'緊急'),
(4, 2, N'冷氣不冷', NULL, 1, '2026-01-06 09:03:00', '2026-01-06 09:03:00', NULL),
(5, 2, N'異音漏水', NULL, 1, '2026-01-06 09:04:00', '2026-01-06 09:04:00', NULL),
(6, 2, N'濾網清潔', 4, 1, '2026-01-06 09:05:00', '2026-01-06 09:05:00', N'低'),
(7, 3, N'網路斷線', NULL, 1, '2026-01-06 09:06:00', '2026-01-06 09:06:00', NULL),
(8, 3, N'WiFi訊號異常', NULL, 1, '2026-01-06 09:07:00', '2026-01-06 09:07:00', NULL),
(9, 3, N'電腦故障', 3, 1, '2026-01-06 09:08:00', '2026-01-06 09:08:00', N'中'),
(10, 4, N'水管漏水', NULL, 1, '2026-01-06 09:09:00', '2026-01-06 09:09:00', NULL),
(11, 4, N'馬桶阻塞', NULL, 1, '2026-01-06 09:10:00', '2026-01-06 09:10:00', NULL),
(12, 4, N'水龍頭故障', 4, 1, '2026-01-06 09:11:00', '2026-01-06 09:11:00', N'低'),
(13, 5, N'門窗損壞', NULL, 1, '2026-01-06 09:12:00', '2026-01-06 09:12:00', NULL),
(14, 5, N'天花板漏水', NULL, 1, '2026-01-06 09:13:00', '2026-01-06 09:13:00', NULL),
(15, 5, N'地板破損', 4, 1, '2026-01-06 09:14:00', '2026-01-06 09:14:00', N'低'),
(16, 6, N'消防設備異常', NULL, 1, '2026-01-06 09:15:00', '2026-01-06 09:15:00', NULL),
(17, 6, N'煙霧偵測器故障', NULL, 1, '2026-01-06 09:16:00', '2026-01-06 09:16:00', NULL);
SET IDENTITY_INSERT sub_categories OFF;
GO

-- ----------------------------------------------------------------------------
-- 4. roles 角色
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (role_id, role_code, role_name) VALUES
(1, 'ADMIN', N'系統管理員'),
(2, 'HANDLER', N'維修人員'),
(3, 'EMPLOYEE', N'一般員工');
SET IDENTITY_INSERT roles OFF;
GO

-- ----------------------------------------------------------------------------
-- 4b. user_roles 使用者與角色關聯（依你提供的 users 資料，依 INSERT 順序
--     對應 user_id 1~15；若實際 user_id 不同請自行調整）
-- ----------------------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id, created_time) VALUES
(1, 1, '2026-01-01 08:00:00'),   -- admin -> ADMIN
(2, 2, '2026-01-01 08:00:00'),   -- handler01 -> HANDLER
(3, 2, '2026-01-01 08:00:00'),   -- handler02 -> HANDLER
(4, 2, '2026-01-01 08:00:00'),   -- handler03 -> HANDLER
(5, 2, '2026-01-01 08:00:00'),   -- handler04 -> HANDLER
(6, 3, '2026-01-02 08:00:00'),   -- emp01 -> EMPLOYEE
(7, 3, '2026-01-02 08:00:00'),   -- emp02 -> EMPLOYEE
(8, 3, '2026-01-02 08:00:00'),   -- emp03 -> EMPLOYEE
(9, 3, '2026-01-02 08:00:00'),   -- emp04 -> EMPLOYEE
(10, 3, '2026-01-02 08:00:00'),  -- emp05 -> EMPLOYEE
(11, 3, '2026-01-02 08:00:00'),  -- emp06 -> EMPLOYEE
(12, 3, '2026-01-02 08:00:00'),  -- emp07 -> EMPLOYEE
(13, 3, '2026-01-02 08:00:00'),  -- emp08 -> EMPLOYEE (帳號待審核)
(14, 3, '2026-01-02 08:00:00'),  -- emp09 -> EMPLOYEE (帳號停用)
(15, 3, '2026-01-02 08:00:00');  -- emp10 -> EMPLOYEE
GO

-- ----------------------------------------------------------------------------
-- 5. repair_targets 報修標的
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT dbo.repair_targets ON;
INSERT INTO dbo.repair_targets (target_id, target_no, name, model) VALUES
(1, 'T-0001', N'分離式冷氣', N'Panasonic CS-A28FA2'),
(2, 'T-0002', N'中央空調主機', N'Daikin RXV140'),
(3, 'T-0003', N'A棟3F配電盤', N'ABB DB-300'),
(4, 'T-0004', N'消防灑水系統', N'Tyco TY-FRB'),
(5, 'T-0005', N'給水加壓馬達', N'Grundfos CR15'),
(6, 'T-0006', N'辦公室桌上型電腦', N'Dell OptiPlex 7090'),
(7, 'T-0007', N'網路交換器', N'Cisco Catalyst 2960'),
(8, 'T-0008', N'客用電梯', N'Otis Gen2'),
(9, 'T-0009', N'B棟煙霧偵測器', N'Honeywell SD-851'),
(10, 'T-0010', N'馬桶沖水系統', N'TOTO CW-K905');
SET IDENTITY_INSERT dbo.repair_targets OFF;
GO

-- ----------------------------------------------------------------------------
-- 6. work_orders 工單主表 (20筆，涵蓋 WorkOrderState 六種狀態)
-- creator_user_id: 6~15 (員工) / assigned_handler: 2~5 (維修人員) / admin_id: 1
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT work_orders ON;
INSERT INTO work_orders
(work_order_id, work_order_no, title, sub_category_id, priority_id, location_detail, contact_phone, description, due_time, status, created_time, creator_user_id, assigned_handler, is_overdue, version, target_id, admin_id) VALUES
(1, 'WO-2026-0001', N'辦公室日光燈不亮', 1, 2, N'A棟3樓辦公區', '0912345001', N'靠窗第二排燈管閃爍後熄滅', '2026-01-11 09:00:00', 'PENDING_REVIEW', '2026-01-10 09:00:00', 7, NULL, 0, 0, NULL, NULL),
(2, 'WO-2026-0002', N'會議室插座無法充電', 2, 2, N'A棟5樓大會議室', '0912345002', N'牆面插座三孔皆無反應', '2026-01-12 14:00:00', 'PENDING_REVIEW', '2026-01-12 06:00:00', 8, NULL, 0, 0, NULL, NULL),
(3, 'WO-2026-0003', N'配電盤異常跳電', 3, 1, N'A棟B1配電室', '0912345003', N'配電盤每日固定時間跳電一次', '2026-01-15 13:00:00', 'IN_PROGRESS', '2026-01-15 09:00:00', 9, 2, 0, 2, 3, 1),
(4, 'WO-2026-0004', N'冷氣完全不冷', 4, 2, N'B棟2樓業務部', '0912345004', N'冷氣運轉但吹出來的風不冷', '2026-01-20 18:00:00', 'IN_PROGRESS', '2026-01-20 10:00:00', 10, 3, 0, 2, 1, 1),
(5, 'WO-2026-0005', N'冷氣機異音並漏水', 5, 3, N'B棟3樓茶水間', '0912345005', N'開機後有滴答異音且底部漏水', '2026-01-25 09:00:00', 'PENDING_USER_ACCEPTANCE', '2026-01-24 09:00:00', 11, 3, 0, 4, 1, 1),
(6, 'WO-2026-0006', N'冷氣濾網清潔申請', 6, 4, N'C棟1樓大廳', '0912345006', N'例行濾網清潔，異味明顯', '2026-02-05 09:00:00', 'COMPLETED', '2026-02-02 09:00:00', 12, 3, 0, 5, 1, 1),
(7, 'WO-2026-0007', N'辦公室網路全區斷線', 7, 1, N'C棟2樓研發部', '0912345007', N'整層樓網路無法連線約半小時', '2026-02-10 10:00:00', 'COMPLETED', '2026-02-10 06:00:00', 13, 4, 0, 5, 7, 1),
(8, 'WO-2026-0008', N'WiFi訊號不穩', 8, 2, N'A棟4樓人資部', '0912345008', N'WiFi常斷線需重新連接', '2026-02-15 17:00:00', 'PENDING_ADMIN_ACCEPTANCE', '2026-02-15 09:00:00', 14, 4, 0, 3, 7, 1),
(9, 'WO-2026-0009', N'電腦無法開機', 9, 3, N'A棟4樓人資部', '0912345009', N'按下電源鍵完全沒反應', '2026-02-20 10:00:00', 'PENDING_REVIEW', '2026-02-19 15:00:00', 15, NULL, 1, 0, 6, NULL),
(10, 'WO-2026-0010', N'茶水間水管漏水', 10, 3, N'B棟1樓茶水間', '0912345010', N'水管接頭處持續滲水', '2026-03-01 09:00:00', 'IN_PROGRESS', '2026-02-28 09:00:00', 7, 5, 0, 2, NULL, 1),
(11, 'WO-2026-0011', N'廁所馬桶阻塞', 11, 3, N'C棟3樓男廁', '0912345011', N'沖水後排水緩慢並溢出', '2026-03-05 09:00:00', 'COMPLETED', '2026-03-04 09:00:00', 8, 5, 0, 5, 10, 1),
(12, 'WO-2026-0012', N'水龍頭滴水不止', 12, 4, N'A棟2樓茶水間', '0912345012', N'水龍頭關緊後仍持續滴水', '2026-03-10 09:00:00', 'PENDING_USER_ACCEPTANCE', '2026-03-08 09:00:00', 9, 5, 0, 4, NULL, 1),
(13, 'WO-2026-0013', N'會議室門損壞無法上鎖', 13, 4, N'B棟5樓小會議室', '0912345013', N'門鎖卡榫鬆脫', '2026-03-15 09:00:00', 'CANCELLED', '2026-03-12 09:00:00', 10, NULL, 0, 1, NULL, 1),
(14, 'WO-2026-0014', N'天花板滲水', 14, 4, N'C棟4樓走廊', '0912345014', N'雨天時天花板明顯滲水', '2026-03-20 09:00:00', 'IN_PROGRESS', '2026-03-18 09:00:00', 11, 4, 1, 2, NULL, 1),
(15, 'WO-2026-0015', N'地板磁磚破損', 15, 4, N'A棟1樓大廳', '0912345015', N'地磚裂開有安全疑慮', '2026-03-25 09:00:00', 'PENDING_ADMIN_ACCEPTANCE', '2026-03-22 09:00:00', 12, 4, 0, 3, NULL, 1),
(16, 'WO-2026-0016', N'消防灑水頭異常', 16, 1, N'B棟B1機房', '0912345016', N'灑水頭出現鏽蝕與滲水', '2026-04-01 09:00:00', 'PENDING_ADMIN_ACCEPTANCE', '2026-03-31 09:00:00', 13, 2, 0, 3, 4, 1),
(17, 'WO-2026-0017', N'煙霧偵測器誤報警報', 17, 1, N'B棟5樓辦公區', '0912345017', N'偵測器頻繁誤觸發警報聲', '2026-04-05 09:00:00', 'COMPLETED', '2026-04-04 09:00:00', 14, 2, 0, 5, 9, 1),
(18, 'WO-2026-0018', N'插座異味有燒焦味', 2, 1, N'C棟2樓研發部', '0912345018', N'插座使用時聞到燒焦味', '2026-04-10 09:00:00', 'IN_PROGRESS', '2026-04-09 09:00:00', 15, 2, 1, 2, NULL, 1),
(19, 'WO-2026-0019', N'電梯異音', 13, 4, N'A棟客用電梯', '0912345019', N'電梯運行時有明顯異音', '2026-04-15 09:00:00', 'PENDING_REVIEW', '2026-04-14 16:00:00', 7, NULL, 0, 0, 8, NULL),
(20, 'WO-2026-0020', N'網路交換器故障', 7, 1, N'C棟機房', '0912345020', N'交換器指示燈全滅，樓層斷網', '2026-04-20 09:00:00', 'CANCELLED', '2026-04-19 09:00:00', 8, NULL, 0, 1, 7, 1);
SET IDENTITY_INSERT work_orders OFF;
GO

-- ----------------------------------------------------------------------------
-- 7. work_order_attachments 工單附件
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT work_order_attachments ON;
INSERT INTO work_order_attachments (attachment_id, work_order_id, original_file_name, file_data, content_type, file_size, created_time, uploaded_user_id) VALUES
(1, 1, N'燈管閃爍.jpg', NULL, 'image/jpeg', 204800, '2026-01-10 09:02:00', 7),
(2, 3, N'配電盤跳電紀錄.jpg', NULL, 'image/jpeg', 512000, '2026-01-15 09:05:00', 9),
(3, 4, N'冷氣溫度截圖.png', NULL, 'image/png', 153600, '2026-01-20 10:03:00', 10),
(4, 5, N'冷氣底部漏水.jpg', NULL, 'image/jpeg', 307200, '2026-01-24 09:05:00', 11),
(5, 9, N'電腦無法開機.jpg', NULL, 'image/jpeg', 256000, '2026-02-19 15:05:00', 15),
(6, 10, N'水管接頭漏水.jpg', NULL, 'image/jpeg', 409600, '2026-02-28 09:05:00', 7),
(7, 14, N'天花板滲水痕跡.jpg', NULL, 'image/jpeg', 358400, '2026-03-18 09:05:00', 11),
(8, 16, N'灑水頭鏽蝕.jpg', NULL, 'image/jpeg', 302080, '2026-03-31 09:05:00', 13);
SET IDENTITY_INSERT work_order_attachments OFF;
GO

-- ----------------------------------------------------------------------------
-- 8. repair_ticket_edit_session 線上編輯 Session（僅目前有人正在編輯的工單）
-- PK 為 ticket_id，一張工單同時僅一個編輯中 session
-- ----------------------------------------------------------------------------
INSERT INTO repair_ticket_edit_session (ticket_id, user_id, session_token, start_time, last_active_time) VALUES
(3, 2, N'sess-tok-a1b2c3d4', '2026-08-20 10:00:00', '2026-08-20 10:12:00'),
(14, 4, N'sess-tok-e5f6g7h8', '2026-08-20 10:05:00', '2026-08-20 10:14:00'),
(18, 2, N'sess-tok-i9j0k1l2', '2026-08-20 10:08:00', '2026-08-20 10:15:00');
GO

-- ----------------------------------------------------------------------------
-- 9. repair_ticket_history 工單異動歷史紀錄
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT repair_ticket_history ON;
INSERT INTO repair_ticket_history (history_id, ticket_id, status, edited_time, editor_id, event, feedback) VALUES
(1, 3, 'PENDING_REVIEW', '2026-01-15 09:00:00', 9, 'CREATE', NULL),
(2, 3, 'IN_PROGRESS', '2026-01-15 11:00:00', 1, 'ASSIGN', N'已指派維修人員處理'),
(3, 4, 'PENDING_REVIEW', '2026-01-20 10:00:00', 10, 'CREATE', NULL),
(4, 4, 'IN_PROGRESS', '2026-01-20 13:00:00', 1, 'ASSIGN', N'已指派維修人員處理'),
(5, 5, 'PENDING_REVIEW', '2026-01-24 09:00:00', 11, 'CREATE', NULL),
(6, 5, 'IN_PROGRESS', '2026-01-24 11:00:00', 1, 'ASSIGN', NULL),
(7, 5, 'PENDING_USER_ACCEPTANCE', '2026-01-26 15:00:00', 3, 'REPAIR_DONE', N'已更換壓縮機並清洗排水管'),
(8, 6, 'PENDING_REVIEW', '2026-02-02 09:00:00', 12, 'CREATE', NULL),
(9, 6, 'IN_PROGRESS', '2026-02-02 10:00:00', 1, 'ASSIGN', NULL),
(10, 6, 'PENDING_USER_ACCEPTANCE', '2026-02-03 15:00:00', 3, 'REPAIR_DONE', N'已完成濾網清潔'),
(11, 6, 'COMPLETED', '2026-02-04 09:30:00', 12, 'ACCEPT', N'感謝，異味已消除'),
(12, 7, 'PENDING_REVIEW', '2026-02-10 06:00:00', 13, 'CREATE', NULL),
(13, 7, 'IN_PROGRESS', '2026-02-10 07:00:00', 1, 'ASSIGN', NULL),
(14, 7, 'PENDING_USER_ACCEPTANCE', '2026-02-10 09:00:00', 4, 'REPAIR_DONE', N'已重新設定交換器排除故障'),
(15, 7, 'COMPLETED', '2026-02-10 10:30:00', 13, 'ACCEPT', NULL),
(16, 8, 'PENDING_REVIEW', '2026-02-15 09:00:00', 14, 'CREATE', NULL),
(17, 8, 'IN_PROGRESS', '2026-02-15 10:00:00', 1, 'ASSIGN', NULL),
(18, 8, 'PENDING_USER_ACCEPTANCE', '2026-02-16 09:00:00', 4, 'REPAIR_DONE', N'已更換無線AP'),
(19, 8, 'PENDING_ADMIN_ACCEPTANCE', '2026-02-16 14:00:00', 14, 'USER_REJECT', N'訊號仍不穩定，需再確認'),
(20, 11, 'PENDING_REVIEW', '2026-03-04 09:00:00', 8, 'CREATE', NULL),
(21, 11, 'IN_PROGRESS', '2026-03-04 10:00:00', 1, 'ASSIGN', NULL),
(22, 11, 'PENDING_USER_ACCEPTANCE', '2026-03-05 11:00:00', 5, 'REPAIR_DONE', N'已疏通排水管'),
(23, 11, 'COMPLETED', '2026-03-05 16:00:00', 8, 'ACCEPT', NULL),
(24, 13, 'PENDING_REVIEW', '2026-03-12 09:00:00', 10, 'CREATE', NULL),
(25, 13, 'CANCELLED', '2026-03-13 09:00:00', 10, 'CANCEL', N'已自行請廠商修復，取消報修'),
(26, 17, 'PENDING_REVIEW', '2026-04-04 09:00:00', 14, 'CREATE', NULL),
(27, 17, 'IN_PROGRESS', '2026-04-04 10:00:00', 1, 'ASSIGN', NULL),
(28, 17, 'PENDING_USER_ACCEPTANCE', '2026-04-04 15:00:00', 2, 'REPAIR_DONE', N'已校正偵測器靈敏度'),
(29, 17, 'COMPLETED', '2026-04-05 09:00:00', 14, 'ACCEPT', NULL),
(30, 20, 'PENDING_REVIEW', '2026-04-19 09:00:00', 8, 'CREATE', NULL),
(31, 20, 'CANCELLED', '2026-04-19 15:00:00', 1, 'CANCEL', N'設備已於保固期內由原廠直接更換');
SET IDENTITY_INSERT repair_ticket_history OFF;
GO

-- ----------------------------------------------------------------------------
-- 10. contact_records 工單留言紀錄
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT contact_records ON;
INSERT INTO contact_records (record_id, author_user_id, work_order_id, content, created_time, record_type) VALUES
(1, 9, 3, N'請問大概什麼時候會有人來處理？', '2026-01-15 09:30:00', 'COMMENT'),
(2, 2, 3, N'預計今天下午到現場檢查配電盤', '2026-01-15 10:00:00', 'COMMENT'),
(3, 11, 5, N'冷氣漏水已經滴到地毯了，麻煩儘快', '2026-01-24 09:10:00', 'COMMENT'),
(4, 3, 5, N'已到現場處理，稍後回報進度', '2026-01-24 13:00:00', 'COMMENT'),
(5, 14, 8, N'訊號仍不穩定，需再確認', '2026-02-16 14:00:00', 'REJECTION'),
(6, 4, 8, N'了解，將再派人到現場複測', '2026-02-16 14:30:00', 'COMMENT'),
(7, 10, 13, N'已自行請廠商修復，取消報修', '2026-03-13 09:00:00', 'REJECTION'),
(8, 13, 16, N'灑水頭鏽蝕範圍擴大，請優先處理', '2026-03-31 10:00:00', 'COMMENT');
SET IDENTITY_INSERT contact_records OFF;
GO

-- ----------------------------------------------------------------------------
-- 11. notifications 系統通知
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT notifications ON;
INSERT INTO notifications (notification_id, work_order_id, status, title, message, is_read, sender_id, receiver_id, priority_id, created_time) VALUES
(1, 3, 'IN_PROGRESS', N'工單已指派', N'您的工單 WO-2026-0003 已指派給維修人員處理', 1, 1, 9, 1, '2026-01-15 11:00:00'),
(2, 5, 'PENDING_USER_ACCEPTANCE', N'維修完成待驗收', N'工單 WO-2026-0005 已完成維修，請確認驗收', 1, 3, 11, 3, '2026-01-26 15:00:00'),
(3, 6, 'COMPLETED', N'工單已完成', N'工單 WO-2026-0006 已由您驗收完成', 0, 12, 1, 4, '2026-02-04 09:30:00'),
(4, 8, 'PENDING_ADMIN_ACCEPTANCE', N'使用者拒絕驗收', N'工單 WO-2026-0008 使用者反映問題未解決', 0, 14, 1, 2, '2026-02-16 14:00:00'),
(5, 13, 'CANCELLED', N'工單已取消', N'工單 WO-2026-0013 已由報修人取消', 1, 10, 1, 4, '2026-03-13 09:00:00'),
(6, 14, 'IN_PROGRESS', N'工單逾期提醒', N'工單 WO-2026-0014 已逾期尚未完成，請盡速處理', 0, NULL, 4, 4, '2026-03-21 09:00:00'),
(7, 16, 'PENDING_ADMIN_ACCEPTANCE', N'待管理員複核', N'工單 WO-2026-0016 維修完成待管理員複核', 0, 2, 1, 1, '2026-03-31 15:00:00'),
(8, 18, 'IN_PROGRESS', N'工單逾期提醒', N'工單 WO-2026-0018 已逾期尚未完成，請盡速處理', 0, NULL, 2, 1, '2026-04-10 09:00:00');
SET IDENTITY_INSERT notifications OFF;
GO

-- ----------------------------------------------------------------------------
-- 12. system_announcements 系統公告
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT system_announcements ON;
INSERT INTO system_announcements (announcement_id, title, content, category, is_pinned, start_time, end_time, created_by, created_time) VALUES
(1, N'系統維護通知', N'系統將於本週六凌晨 2:00-4:00 進行例行維護，期間暫停服務。', 'MAINTENANCE', 1, '2026-02-01 00:00:00', '2026-02-08 00:00:00', 1, '2026-01-28 09:00:00'),
(2, N'新版報修流程上線', N'即日起報修單新增「附件上傳」功能，請多加利用。', 'FEATURE', 0, '2026-03-01 00:00:00', NULL, 1, '2026-02-28 09:00:00'),
(3, N'夏季用電高峰緊急公告', N'夏季用電量大增，如遇跳電請立即通報，將優先派工處理。', 'URGENT', 1, '2026-06-01 00:00:00', '2026-09-01 00:00:00', 1, '2026-05-30 09:00:00');
SET IDENTITY_INSERT system_announcements OFF;
GO

-- ----------------------------------------------------------------------------
-- 13. system_messages 獨立留言表
-- ----------------------------------------------------------------------------
SET IDENTITY_INSERT system_messages ON;
INSERT INTO system_messages (message_id, user_id, title, content, created_time) VALUES
(1, 7, N'系統操作問題', N'請問報修單送出後可以自行修改內容嗎？', '2026-02-10 09:00:00'),
(2, 1, N'回覆：系統操作問題', N'工單送出後 30 分鐘內可於編輯頁面修改，超過時間需聯繫維修人員。', '2026-02-10 10:00:00'),
(3, 12, N'建議：增加進度通知', N'希望工單處理進度能有 Email 通知，方便追蹤。', '2026-03-01 09:00:00');
SET IDENTITY_INSERT system_messages OFF;
GO
