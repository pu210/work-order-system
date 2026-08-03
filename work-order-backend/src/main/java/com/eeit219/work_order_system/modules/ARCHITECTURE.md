# Workorder 後端專案架構與開發規範

為了維持程式碼的整潔性與可維護性，本專案採用**模組化高內聚**的架構設計。

請各位開發者在新增功能或模組時，嚴格遵循以下目錄結構與開發規範。

---

## 專案目錄結構 (Project Directory Structure)

```text
workorder
│
├── common/             # 全域通用組件與設定（跨模組共用）
│   ├── config/         # 系統全域配置（如：Security, CORS 等）
│   ├── exception/      # 全域例外處理（自訂 Exception）
│   ├── response/       # 統一 API 回傳格式（ApiResponse, Result Code 等）
│   ├── util/           # 通用工具類（DatetimeConverter等）
│   └── security/       # JWT 驗證機制
│
└── module/             # 各模組（按功能特徵劃分）
    ├── a/           # 登入與角色權限模組範例
    │   ├── controller/ # API 控制層（路由定義、參數校驗）
    │   ├── service/    # 核心業務邏輯層
    │   ├── repository/ # 資料存取層（DB 操作）
    │   ├── dto/        # 資料傳輸物件（Request / Response DTO）
    │   └── entity/     # 資料庫實體物件 (Data Model)
    │
    ├── b/       # 報修單建立模組範例
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── dto/
    │   └── entity/
    │
    ├── c/       # 維修任務指派流程模組
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── dto/
    │   └── entity/
    ├── d/       # 維修紀錄、人員管理模組
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── dto/
    │   └── entity/
    ├── e/       # 首頁、通知、報表模組
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── dto/
    │   └── entity/
    └── f/       # 系統設定、優先級模組
        ├── controller/
        ├── service/
        ├── repository/
        ├── dto/
        └── entity/
```
