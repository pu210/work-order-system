# 工單管理系統 - 前端 (Work Order System Frontend)

本目錄為工單系統（Work Order System）的前端 Web 應用程式，基於 **Vue 3** 與 **Vite** 建構。

---

## 🛠 技術棧 (Tech Stack)

- **框架 (Framework)**: Vue 3
- **狀態管理 (State Management)**: Pinia
- **路由管理 (Routing)**: Vue Router
- **HTTP 客戶端 (HTTP Client)**: Axios
- **建構工具 (Build Tool)**: Vite

---

## 🚀 快速開始 (Quick Start)

### 1. 環境需求 (Prerequisites)

- Node.js: `v24(LTS)`
- npm

### 2. 安裝依賴 (Install Dependencies)

```bash
npm install
```

### 3. 設定環境變數 (Environment Setup)

初次下載專案後，請複製 .env.exapmle 範本檔建立 .env 檔案：

```bash
cp .env.example .env
```

### 4. 啟動本地開發伺服器 (Development)

```bash
npm run dev
```

啟動後預設存取網址為：http://localhost:5173

### 5. 專案打包 (Production Build)

```bash
npm run build
```

打包後的靜態資源將生成於 dist/ 目錄下。

### 6. 打包預覽 (Preview Build)

```bash
npm run preview
```

📁 目錄結構說明 (Directory Structure)

```Plaintext
work-order-front/
├── .vscode/ # vscode 相關設定
├── node_modules/ # 第三方套件資料夾
├── public/ # 靜態資源 (不經 Vite 打包處理，如 favicon)
├── src/
│ ├── assets/ # 靜態資源 (圖片、全域 CSS 等)
│ ├── components/ # 存放組件
│ ├── plugins/ # 建立 Axios 實例、設定 baseURL、請求與回應攔截器等
│ ├── views/ # 頁面級元件 (對應路由頁面)
│ ├── router/ # 路由設定 (負責設定網址與對應的 view)
│ ├── stores/ # 狀態管理 (ex.使用 Pinia)
│ ├── api/ # 後端 API 封裝 (Axios)
│ ├── App.vue # 入口組件
│ └── main.js # 應用程式入口點
├── .env.example # 環境變數範本檔 (提交至 Git，供其他人參考與複製)
├── .env # 本地真實環境變數檔 (包含敏感資訊，受 .gitignore 保護)
├── index.html # 唯一個 HTML
├── package.json # 專案設定黨，定義腳本與使用套件
├── package-lock.json # 鎖定實際安裝的套件版本與結構
├── vite.config.js # Vite 設定檔
└── jsconfig.json # JavaScript 專案設定檔
```

⚙️ 環境變數設定 (Environment Variables)
專案預設使用 .env 檔案管理環境變數。

💡 請勿將包含敏感資訊的 .env 檔案提交至版本控制。

環境變數檔案說明:

.env.example: 環境變數範本檔（提交至 Git，供成員建立環境使用）。

.env: 本機開發環境設定檔（不會提交至 Git）。

.env.development: 本地開發環境變數 (API 預設指向 http://localhost:8080)。

.env.production: 線上正式環境變數。
