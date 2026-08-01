# 🛠️ 工單管理系統 (Work Order Management System)

這是一個專為企業內部設計的工單管理系統，旨在優化報修與派工流程，提升內部維運效率。

---

## 👥 團隊成員與核心分工

團隊共有 6 位成員，各模組皆包含完整的前後端 API 開發與對應前端頁面實作：

| 成員       | 主責模組                                 | 實際開發產出確認                       |
| :--------- | :--------------------------------------- | :------------------------------------- |
| **成員 A** | 帳號、登入、角色權限、使用者管理         | 包含登入、權限控管、使用者管理頁。     |
| **成員 B** | 報修單建立、列表、搜尋篩選               | 包含核心報修、列表查詢、搜尋篩選頁。   |
| **成員 C** | 維修任務、工程師指派、狀態流程、逾期標記 | 包含指派、狀態轉換、任務管理頁。       |
| **成員 D** | 報修詳情、使用者大廳                     | 包含報修詳情、維修紀錄、留言管理。     |
| **成員 E** | 系統首頁、報表、通知                     | 包含報表統計、通知功能。               |
| **成員 F** | 系統設定、報修類型、優先級               | 包含系統設定、報修列表、報優先級管理。 |

---

## 📁 專案目錄架構 (Directory Structure)

```text
work-order-system/
├── work-order-frontend/     # 前端 Vue 3 專案
└── work-order-backend/      # 後端 Spring Boot 專案
    ├── common/              # 全域通用元件（Config, Exception, Util 等）
    └── module/              # 各業務模組目錄

```

---

## 🚀 技術棧 (Tech Stack)

### 後端 (Backend)

- **程式語言**: Java 21
- **核心框架**: Spring Boot 3.x
- **資料庫**: Microsoft SQL Server
- **ORM 框架**: Spring Data JPA / Hibernate
- **專案管理**: Maven (內建 Maven Wrapper 環境防呆)

### 前端 (Frontend)

- **核心框架**: Vue.js 3
- **套件管理**: npm

---

## 🌿 Git 協作規範

為了確保 6 人團隊高效協作並避免程式碼衝突，請全體成員嚴格遵守以下規範：

### 1. 分支管理策略 (Branching Strategy)

- `main`: 穩定的生產環境分支，禁止成員直接 push 代碼至此分支。
- `dev`: 開發整合分支，所有新功能測試無誤後皆合併至此。
- **功能分支 (Feature Branch)**: 每位成員開發自己主責的模組時，請從 `dev` 延伸出獨立分支，命名格式為 `feature/功能名稱-成員名字`（例如：`feature/login-memberA`）。

### 2. 協作流程-Feature 分支開發標準作業流程 (SOP)

1. 從 `dev` 切出自己的功能分支開始開發。
2. 開發完成並於地端測試通過後，將功能分支 push 到 GitHub。
3. 在 GitHub 上發起 **Pull Request (PR)** 請求合併回 `dev` 分支。
4. 由其他團隊成員進行 Code Review，確認無衝突後進行合併。

以成員 alex 要開發 ticket（工單模組）為例，分支名稱為：feature/ticket-alex

#### Step 0. 下載遠端專案並進入專案目錄（只有「第一次」加入專案需要）

```bash
git clone https://github.com/pu210/work-order-system.git
cd work-order-system
```

確認目前分支

```bash
git branch
```

如果沒有 dev：

```bash
git fetch origin
git checkout dev
```

原因： 有些人 clone 完後預設會停留在 main。

#### Step 1. 更新本地 dev 分支

每次準備開新模組、切新分支前，務必確保本地的 dev 是遠端最新的版本：

1. 切換到 dev 分支

```bash
git checkout dev
```

2. 拉取遠端 dev 的最新程式碼

```bash
git pull origin dev
```

#### Step 2. 從 dev 建立自己的功能分支並切換至獨立分支

使用 checkout -b 從最新的 dev 延伸出新分支：

語法：git checkout -b feature/功能名稱-成員名字

```bash
git checkout -b feature/ticket-alex
```

（較新的 Git 語法也可以用：git switch -c feature/ticket-alex）

💡 確認分支：執行 git branch，看到星號 \* 指向 feature/ticket-alex 即代表切換成功。

#### Step 3. 本地開發與提交 (Commit)

在 module/ticket/ 下新增或修改程式碼後，進行本地 Commit：

1. 暫存變更
   建議先執行 git status 確認此次要提交的檔案。

```bash
git add .
```

2. 提交 Commit (建議加上規範前綴，如 feat, fix)

```bash
   git commit -m "feat: 新增工單模組基本資料夾架構與 Controller"
```

#### Step 4. 推送分支至 GitHub

第一次推送該分支到 GitHub 時，加上 -u (設定 upstream)

```bash
git push -u origin feature/ticket-alex
```

之後直接輸入 git push 即可：

```bash
git push
```

#### Step 5. 開發完成後：發送 Pull Request (PR)

1. 開啟 GitHub 專案頁面。
2. 會看到提示跳出 feature/ticket-alex had recent pushes，點擊 Compare & pull request。
3. 重要設定：
   Base (目標分支)：選擇 dev
   Compare (來源分支)：選擇 feature/ticket-alex
4. 寫下 PR 簡述（例如：新增 ticket 模組初版架構），由其他組員確認內容及是否可正常合併。
5. 確認內容無誤後，點擊 Merge Pull Request，將功能分支合併至 dev。

⚠️ 開發注意事項

- 每次開發新功能前，先更新本地 dev。
- 不要直接修改 main 分支。
- 不要直接 Push 到 dev、main 分支。
- 每完成一項功能，請先確認程式可正常執行再提交 Commit。
- 共用程式請放置於 common，避免重複實作。
- 一律透過 Pull Request 合併到 dev

### 3. Commit 訊息規範 (Conventional Commits)

每次提交的 Commit 時請遵循以下格式：

- `feat:` 新增功能 / 新資料夾架構（例如：`feat: 新增登入 API`）
- `fix:` 修補 Bug（例如：`fix: 修復搜尋篩選失效問題`）
- `docs:` 修改文件或 README
- `chore:` 調整配置或日常雜務（例如：`chore: 調整專案配置`）
- `style:` 排版調整
- `refactor:` 重構程式，不影響功能

---

## 💻 本地端開發環境架設 (Getting Started)

請全體成員依據自己負責的區塊，進行對應的環境架設：

### 🟢 前端開發環境 (work-order-frontend)

1. **環境準備**：
   - 請確保本機電腦已安裝 **Node.js** (建議 LTS 版本)。
2. **安裝依賴套件**：
   - 打開終端機，切換進入前端專案目錄：
     ```bash
     cd work-order-frontend
     ```
   - 執行安裝指令，自動下載 `package.json` 中定義的所有前端套件：
     ```bash
     npm install
     ```
3. **啟動專案**：
   - 本地套件安裝完成後，執行以下指令啟動開發伺服器：
     ```bash
     npm run dev
     ```
   - 啟動成功後，終端機將會顯示本地訪問網址（通常為 `http://localhost:5173/`）。

---

### 🔵 後端開發環境 (work-order-backend)

1.  **環境準備**：
    - 請確保本機電腦已安裝 **Java 21**。
    - 請確保本機已安裝並啟動 **Microsoft SQL Server**，且已手動建立一個專案用的空資料庫。
2.  **設定檔複製與配置**：
    - 進入 `work-order-backend/src/main/resources/` 目錄。
    - 複製 `application.properties.example` 檔案，並在同目錄下更名為 `application.properties`。
    - 打開 `application.properties`，將裡面的資料庫連線帳號與密碼，修改為符合你本地 MSSQL 的實際設定。

      ⚠️ 安全提醒：application.properties 包含個人資料庫密碼，請確認已加入 .gitignore，切勿推送至 GitHub！

      ⚠️ 如果第一次啟動、資料庫是空的，可以先暫時把spring.jpa.hibernate.ddl-auto=validate 改成 update 讓 Hibernate 自動幫大家建表；等表建立好之後，再切回 validate

3.  **安裝依賴套件與啟動**：
    - 返回終端機，切換進入後端專案目錄：
      ```bash
      cd work-order-backend
      ```
    - 執行以下指令（使用專案內建的 Maven Wrapper 進行防呆），它會自動下載所有需要的 Java 依賴套件（如 Spring Boot Starter、JPA 等）並直接啟動後端伺服器：

      ```bash
      # Mac / Linux 系統請執行：
      ./mvnw spring-boot:run

      # Windows 系統請執行：
      mvnw.cmd spring-boot:run
      ```

    - 啟動成功後，預設的後端服務將會運行在 `http://localhost:8080/`。
