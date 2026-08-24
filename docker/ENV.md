# 環境變數清單

複製 `.env.example` 成 `.env`（`.env` 不進版控），照下面表格填。

## 必填

| 變數 | 說明 |
|---|---|
| `DB_SA_PASSWORD` | MSSQL sa 密碼。至少 8 碼，且大寫/小寫/數字/符號至少包含 3 種 |
| `JWT_TOKEN_SECRET` | JWT 簽章密鑰，至少 64 字元。產生指令：`openssl rand -base64 64 \| tr -d '\n'` |  在終端機（Git Bash 或 PowerShell）執行，產生真正的密鑰
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google 登入用。Spring Security 啟動時就會檢查不能是空字串，沒有要測 Google 登入先填假值（如 `fake-client-id`） |

## 有預設值，不設也能跑

| 變數 | 預設值 | 說明 |
|---|---|---|
| `DB_PORT` | `1433` | 資料庫對外 port，被佔用才需要改 |
| `BACKEND_PORT` | `8080` | 後端對外 port |
| `JWT_TOKEN_EXPIRE` | `10` | JWT 效期（分鐘） |
| `REFRESH_TOKEN_EXPIRE_DAYS` | `7` | Refresh token 效期（天） |
| `REFRESH_COOKIE_SECURE` | `false` | 正式環境（HTTPS）要改 `true` |
| `FRONTEND_URL` | `http://localhost:5173` | 後端寄信/OAuth2 導向用的前端網址，要跟 `FRONTEND_PORT` 對得上 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` | 允許的來源 |
| `GOOGLE_REDIRECT_URI` | `http://localhost:8080/login/oauth2/code/google` | Google OAuth callback |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | 空 | 留空只會讓「忘記密碼寄信」失效，不影響啟動 |
| `FRONTEND_PORT` | `80` | 前端（nginx）對外 port，HTTP 自動轉址到 HTTPS |
| `FRONTEND_HTTPS_PORT` | `443` | 前端（nginx）HTTPS 對外 port |
