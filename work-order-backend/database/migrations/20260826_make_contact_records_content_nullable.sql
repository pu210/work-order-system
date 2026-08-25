USE workorderDB;

-- [B 模組協助修復 D 模組]：留言允許純圖片、不帶文字，這種情況 content 會是 null，
-- 原本欄位是 NOT NULL，寫入 null 會被資料庫拒絕，造成工單詳情頁「只有圖片的留言」直接報錯。
-- 只在還是 NOT NULL 時才改，重複執行不會出錯。
IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo'
      AND TABLE_NAME = 'contact_records'
      AND COLUMN_NAME = 'content'
      AND IS_NULLABLE = 'NO'
)
BEGIN
    ALTER TABLE dbo.contact_records
        ALTER COLUMN content NVARCHAR(500) NULL;
END;
