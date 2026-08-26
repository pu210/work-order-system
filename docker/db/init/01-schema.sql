-- ============================================================================
-- Docker 初始化用 schema（依 db_3.1  .sql 重組）
--
-- 重組原則：
--   1. 先把所有 CREATE TABLE 建完，只含主鍵（PK）與唯一約束（UNIQUE），
--      完全不帶跨表外鍵（FK），避免表跟表之間的建立順序互相牽制。
--   2. 所有外鍵統一放在最後，用一批 ALTER TABLE ... ADD CONSTRAINT 補上。
--      這樣不管表跟表依賴關係多複雜，都不用擔心先後順序，
--      之後如果有人加新表新關聯，也不會再因為順序踩雷。
--   3. 索引維持放在最後（原本就是這樣）。
--
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 優先級別表 (priorities)
-- ----------------------------------------------------------------------------
CREATE TABLE priorities (
    priorities_id INT           IDENTITY (1, 1) NOT NULL,
    name          NVARCHAR (20) NOT NULL,
    hours         INT           NOT NULL,
    status        BIT           DEFAULT 1 NOT NULL,
    CONSTRAINT PK_priorities PRIMARY KEY (priorities_id),
    CONSTRAINT UQ_priorities_name UNIQUE (name)
);

-- ----------------------------------------------------------------------------
-- 2. 維修大類別表 (repair_categories)
-- ----------------------------------------------------------------------------
CREATE TABLE repair_categories (
    repair_categories_id  INT           IDENTITY (1, 1) NOT NULL,
    name                  NVARCHAR (50) NOT NULL,
    default_priority_id   INT           NULL,
    status                BIT           DEFAULT 1 NOT NULL,
    created_time          DATETIME2     DEFAULT GETDATE() NULL,
    updated_time          DATETIME2     DEFAULT GETDATE() NULL,
    default_priority_name NVARCHAR (50) NULL,
    CONSTRAINT PK_repair_categories PRIMARY KEY (repair_categories_id),
    CONSTRAINT UQ_repair_categories_name UNIQUE (name)
);

-- ----------------------------------------------------------------------------
-- 3. 子類別表 (sub_categories)
-- ----------------------------------------------------------------------------
CREATE TABLE sub_categories (
    sub_categories_id      INT            IDENTITY (1, 1) NOT NULL,
    category_id            INT            NOT NULL,
    name                   NVARCHAR (100) NOT NULL,
    override_priority_id   INT            NULL,
    status                 BIT            DEFAULT 1 NOT NULL,
    created_time           DATETIME2      DEFAULT GETDATE() NULL,
    updated_time           DATETIME2      DEFAULT GETDATE() NULL,
    override_priority_name NVARCHAR (50)  NULL,
    CONSTRAINT PK_sub_categories PRIMARY KEY (sub_categories_id),
    CONSTRAINT UQ_sub_categories_name UNIQUE (name)
);

-- ----------------------------------------------------------------------------
-- 4. 使用者表 (users)
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    user_id              INT           IDENTITY (1, 1) NOT NULL,
    account              VARCHAR (50)  NOT NULL,
    password_hash        VARCHAR (255) NULL,
    must_change_password BIT           DEFAULT 1 NOT NULL,
    name                 NVARCHAR (50) NOT NULL,
    email                VARCHAR (255) NOT NULL,
    phone                VARCHAR (10)  NULL,
    status               TINYINT       DEFAULT 1 NOT NULL,
    created_time         DATETIME2     DEFAULT GETDATE() NOT NULL,
    updated_time         DATETIME2     DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_users PRIMARY KEY (user_id),
    CONSTRAINT UQ_users_account UNIQUE (account),
    CONSTRAINT UQ_users_email UNIQUE (email)
);

-- ----------------------------------------------------------------------------
-- 5. 角色表 (roles)
-- ----------------------------------------------------------------------------
CREATE TABLE roles (
    role_id   INT           IDENTITY (1, 1) NOT NULL,
    role_code VARCHAR (20)  NOT NULL,
    role_name NVARCHAR (20) NOT NULL,
    CONSTRAINT PK_roles PRIMARY KEY (role_id),
    CONSTRAINT UQ_roles_role_code UNIQUE (role_code),
    CONSTRAINT UQ_roles_role_name UNIQUE (role_name)
);

-- ----------------------------------------------------------------------------
-- 6. 使用者與角色關聯表 (user_roles)
-- ----------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id      INT       NOT NULL,
    role_id      INT       NOT NULL,
    created_time DATETIME2 DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id)
);

-- ----------------------------------------------------------------------------
-- 7. 密碼重置 Token 表 (password_reset_tokens)
-- ----------------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    reset_token_id INT           IDENTITY (1, 1) NOT NULL,
    user_id        INT           NOT NULL,
    token          VARCHAR (255) NOT NULL,
    expires_at     DATETIME2     NOT NULL,
    used_at        DATETIME2     NULL,
    created_time   DATETIME2     DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_password_reset_tokens PRIMARY KEY (reset_token_id),
    CONSTRAINT UQ_password_reset_tokens_token UNIQUE (token)
);

-- ----------------------------------------------------------------------------
-- 8. 第三方登入表 (user_oauth_accounts)
-- ----------------------------------------------------------------------------
CREATE TABLE user_oauth_accounts (
    oauth_id         INT            IDENTITY (1, 1) NOT NULL,
    user_id          INT            NOT NULL,
    provider         VARCHAR (20)   DEFAULT 'google' NOT NULL,
    provider_user_id VARCHAR (255)  NOT NULL,
    email            VARCHAR (255)  NULL,
    access_token     VARCHAR (1000) NULL,
    refresh_token    VARCHAR (1000) NULL,
    token_expires_at DATETIME2      NULL,
    created_time     DATETIME2      DEFAULT GETDATE() NOT NULL,
    updated_time     DATETIME2      DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_user_oauth_accounts PRIMARY KEY CLUSTERED (oauth_id),
    CONSTRAINT UQ_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT UQ_user_provider UNIQUE (user_id, provider)
);

-- ----------------------------------------------------------------------------
-- 9. 報修標的表 (repair_targets)
-- ----------------------------------------------------------------------------
CREATE TABLE repair_targets (
    target_id INT           IDENTITY (1, 1) NOT NULL,
    target_no VARCHAR (30)  NOT NULL,
    name      NVARCHAR (30) NOT NULL,
    model     NVARCHAR (50) NULL,
    status    BIT           DEFAULT 1 NOT NULL,
    CONSTRAINT PK_repair_targets PRIMARY KEY (target_id),
    CONSTRAINT UQ_repair_targets_target_no UNIQUE (target_no)
);

-- ----------------------------------------------------------------------------
-- 10. 工單主表 (work_orders)
-- ----------------------------------------------------------------------------
CREATE TABLE work_orders (
    work_order_id    INT            IDENTITY (1, 1) NOT NULL,
    work_order_no    VARCHAR (20)   NOT NULL,
    title            NVARCHAR (50)  NOT NULL,
    sub_category_id  INT            NOT NULL,
    priority_id      INT            NOT NULL,
    location_detail  NVARCHAR (100) NOT NULL,
    contact_phone    VARCHAR (10)   NULL,
    description      NVARCHAR (300) NULL,
    due_time         DATETIME2      NULL,
    status           VARCHAR (50)   DEFAULT 'PENDING_REVIEW' NOT NULL,
    created_time     DATETIME2      DEFAULT GETDATE() NOT NULL,
    creator_user_id  INT            NOT NULL,
    assigned_handler INT            NULL,
    is_overdue       BIT            DEFAULT 0 NOT NULL,
    version          INT            DEFAULT 0 NOT NULL,
    target_id        INT            NULL,
    admin_id         INT            NULL,
    CONSTRAINT PK_work_orders PRIMARY KEY (work_order_id),
    CONSTRAINT UQ_work_orders_no UNIQUE (work_order_no)
);

-- ----------------------------------------------------------------------------
-- 11. 工單附件表 (work_order_attachments)
-- ----------------------------------------------------------------------------
CREATE TABLE work_order_attachments (
    attachment_id      INT             IDENTITY (1, 1) NOT NULL,
    work_order_id      INT             NOT NULL,
    contact_record_id  INT             NULL,
    original_file_name NVARCHAR (255)  NOT NULL,
    file_data          VARBINARY (MAX) NULL,
    content_type       VARCHAR (100)   NULL,
    file_size          INT             NULL,
    created_time       DATETIME2       DEFAULT GETDATE() NOT NULL,
    uploaded_user_id   INT             NOT NULL,
    CONSTRAINT PK_work_order_attachments PRIMARY KEY (attachment_id)
);

-- ----------------------------------------------------------------------------
-- 12. 工單線上編輯 Session 表 (repair_ticket_edit_session)
-- ----------------------------------------------------------------------------
CREATE TABLE repair_ticket_edit_session (
    ticket_id        INT            NOT NULL,
    user_id          INT            NOT NULL,
    session_token    NVARCHAR (100) NOT NULL,
    start_time       DATETIME2      NOT NULL,
    last_active_time DATETIME2      NOT NULL,
    CONSTRAINT PK_repair_ticket_edit_session PRIMARY KEY (ticket_id)
);

-- ----------------------------------------------------------------------------
-- 13. 工單異動歷史紀錄表 (repair_ticket_history)
-- ----------------------------------------------------------------------------
CREATE TABLE repair_ticket_history (
    history_id  INT            IDENTITY (1, 1) NOT NULL,
    ticket_id   INT            NOT NULL,
    status      VARCHAR (50)   NOT NULL,
    edited_time DATETIME2      NOT NULL,
    editor_id   INT            NOT NULL,
    event       VARCHAR (20)   NULL,
    feedback    NVARCHAR (500) NULL,
    CONSTRAINT PK_repair_ticket_history PRIMARY KEY (history_id)
);

-- ----------------------------------------------------------------------------
-- 14. 工單留言紀錄表 (contact_records)
-- ----------------------------------------------------------------------------
CREATE TABLE contact_records (
    record_id      INT            IDENTITY (1, 1) NOT NULL,
    author_user_id INT            NOT NULL,
    work_order_id  INT            NOT NULL,
    -- [B 模組協助修復 D 模組]：留言允許純圖片、不帶文字，這種情況 content 會是 null，
    -- 原本 NOT NULL 會擋下這種留言、造成工單詳情頁只有圖片的留言直接報錯，改成 NULL
    content        NVARCHAR (500) NULL,
    created_time   DATETIME2      DEFAULT GETDATE() NOT NULL,
    record_type    VARCHAR (30)   DEFAULT 'COMMENT' NOT NULL,
    CONSTRAINT PK_contact_records PRIMARY KEY (record_id)
);

-- ----------------------------------------------------------------------------
-- 15. 系統通知表 (notifications)
-- ----------------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT            IDENTITY (1, 1) NOT NULL,
    work_order_id   INT            NULL,
    status          VARCHAR (30)   NOT NULL,
    title           NVARCHAR (100) NULL,
    message         NVARCHAR (500) NULL,
    is_read         BIT            DEFAULT 0 NOT NULL,
    sender_id       INT            NULL,
    receiver_id     INT            NULL,
    priority_id     INT            NULL,
    created_time    DATETIME2      DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_notifications PRIMARY KEY (notification_id)
);

-- ----------------------------------------------------------------------------
-- 16. 系統公告表 (system_announcements)
-- ----------------------------------------------------------------------------
CREATE TABLE system_announcements (
    announcement_id INT            IDENTITY (1, 1) NOT NULL,
    title           NVARCHAR (150) NOT NULL,
    content         NVARCHAR (MAX) NOT NULL,
    category        VARCHAR (30)   DEFAULT 'GENERAL' NOT NULL,
    is_pinned       BIT            DEFAULT 0 NOT NULL,
    start_time      DATETIME2      NULL,
    end_time        DATETIME2      NULL,
    created_by      INT            NOT NULL,
    created_time    DATETIME2      DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_system_announcements PRIMARY KEY (announcement_id)
);

-- ----------------------------------------------------------------------------
-- 17. 獨立留言表 (system_messages)
-- ----------------------------------------------------------------------------
CREATE TABLE system_messages (
    message_id   INT            IDENTITY (1, 1) NOT NULL,
    user_id      INT            NOT NULL,
    title        NVARCHAR (100) NULL,
    content      NVARCHAR (MAX) NOT NULL,
    created_time DATETIME2      DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_system_messages PRIMARY KEY (message_id)
);

-- ----------------------------------------------------------------------------
-- 18. Refresh Token 表 (refresh_tokens)
-- ----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    refresh_token_id INT          IDENTITY (1, 1) NOT NULL,
    user_id          INT          NOT NULL,
    token_hash       VARCHAR (64) NOT NULL,
    expires_time     DATETIME2    NOT NULL,
    revoked          BIT          CONSTRAINT DF_refresh_tokens_revoked DEFAULT (0) NOT NULL,
    created_time     DATETIME2    CONSTRAINT DF_refresh_tokens_created_time DEFAULT (GETDATE()) NOT NULL,
    CONSTRAINT PK_refresh_tokens PRIMARY KEY (refresh_token_id),
    CONSTRAINT UQ_refresh_tokens_token_hash UNIQUE (token_hash)
);


-- ============================================================================
-- 外鍵約束（全部集中在這裡，跟建表順序無關，統一最後補上）
-- ============================================================================

ALTER TABLE repair_categories
    ADD CONSTRAINT FK_repair_categories_priorities
        FOREIGN KEY (default_priority_id) REFERENCES priorities (priorities_id);

ALTER TABLE sub_categories
    ADD CONSTRAINT FK_sub_categories_categories
        FOREIGN KEY (category_id) REFERENCES repair_categories (repair_categories_id);
ALTER TABLE sub_categories
    ADD CONSTRAINT FK_sub_categories_priorities
        FOREIGN KEY (override_priority_id) REFERENCES priorities (priorities_id);

ALTER TABLE user_roles
    ADD CONSTRAINT FK_user_roles_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE user_roles
    ADD CONSTRAINT FK_user_roles_roles
        FOREIGN KEY (role_id) REFERENCES roles (role_id);

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT FK_password_reset_tokens_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE user_oauth_accounts
    ADD CONSTRAINT FK_user_oauth_accounts_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_sub_category
        FOREIGN KEY (sub_category_id) REFERENCES sub_categories (sub_categories_id);
ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_priority
        FOREIGN KEY (priority_id) REFERENCES priorities (priorities_id);
ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_creater
        FOREIGN KEY (creator_user_id) REFERENCES users (user_id);
ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_handler
        FOREIGN KEY (assigned_handler) REFERENCES users (user_id);
ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_repair_target
        FOREIGN KEY (target_id) REFERENCES repair_targets (target_id);
ALTER TABLE work_orders
    ADD CONSTRAINT FK_work_orders_admin
        FOREIGN KEY (admin_id) REFERENCES users (user_id);

ALTER TABLE work_order_attachments
    ADD CONSTRAINT FK_work_order_attachments_work_orders
        FOREIGN KEY (work_order_id) REFERENCES work_orders (work_order_id);
ALTER TABLE work_order_attachments
    ADD CONSTRAINT FK_work_order_attachments_uploader
        FOREIGN KEY (uploaded_user_id) REFERENCES users (user_id);
ALTER TABLE work_order_attachments
    ADD CONSTRAINT FK_work_order_attachments_contact_records
        FOREIGN KEY (contact_record_id) REFERENCES contact_records (record_id);

ALTER TABLE repair_ticket_edit_session
    ADD CONSTRAINT FK_repair_ticket_edit_session_work_orders
        FOREIGN KEY (ticket_id) REFERENCES work_orders (work_order_id);
ALTER TABLE repair_ticket_edit_session
    ADD CONSTRAINT FK_repair_ticket_edit_session_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE repair_ticket_history
    ADD CONSTRAINT FK_repair_ticket_history_work_orders
        FOREIGN KEY (ticket_id) REFERENCES work_orders (work_order_id);
ALTER TABLE repair_ticket_history
    ADD CONSTRAINT FK_repair_ticket_history_users
        FOREIGN KEY (editor_id) REFERENCES users (user_id);

ALTER TABLE contact_records
    ADD CONSTRAINT FK_contact_records_users
        FOREIGN KEY (author_user_id) REFERENCES users (user_id);
ALTER TABLE contact_records
    ADD CONSTRAINT FK_contact_records_work_orders
        FOREIGN KEY (work_order_id) REFERENCES work_orders (work_order_id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_notifications_work_orders
        FOREIGN KEY (work_order_id) REFERENCES work_orders (work_order_id);
ALTER TABLE notifications
    ADD CONSTRAINT FK_notifications_sender
        FOREIGN KEY (sender_id) REFERENCES users (user_id);
ALTER TABLE notifications
    ADD CONSTRAINT FK_notifications_receiver
        FOREIGN KEY (receiver_id) REFERENCES users (user_id);
ALTER TABLE notifications
    ADD CONSTRAINT FK_notifications_priority
        FOREIGN KEY (priority_id) REFERENCES priorities (priorities_id);

ALTER TABLE system_announcements
    ADD CONSTRAINT FK_system_announcements_users
        FOREIGN KEY (created_by) REFERENCES users (user_id);

ALTER TABLE system_messages
    ADD CONSTRAINT FK_system_messages_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_refresh_tokens_users
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;


-- ============================================================================
-- 索引
-- ============================================================================

-- 在 user_roles 的 role_id 欄位建立索引，加速搜尋「具備特定角色」的使用者
CREATE INDEX IX_user_roles_role
    ON user_roles(role_id);

-- 外鍵與狀態欄位索引優化
CREATE INDEX IX_notifications_work_order_id
    ON notifications(work_order_id);

CREATE INDEX IX_notifications_is_read
    ON notifications(is_read);

CREATE INDEX IX_system_announcements_pinned_created
    ON system_announcements(is_pinned DESC, created_time DESC);

-- 留言附圖查詢用（work_order_attachments.contact_record_id 非 null 才算留言附圖）
CREATE INDEX IX_work_order_attachments_contact_record_id
    ON work_order_attachments(contact_record_id)
    WHERE contact_record_id IS NOT NULL;
