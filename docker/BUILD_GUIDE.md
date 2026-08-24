# Docker 建置手冊

照著這份文件的步驟做，可以在自己電腦上把整套系統跑起來。

---

## 第一步：把專案抓下來

```bash
git clone <repo 的網址>
cd work-order-system
```

之後所有指令都預設你人在 `work-order-system` 這個資料夾裡執行。

---

## 第二步：準備你自己的密碼設定檔（`.env`）

這個系統需要的密碼、密鑰不會寫死在程式碼裡，需要你自己準備一份設定檔。

**複製範本：**

```bash
cp .env.example .env
```

**打開 `.env`（用記事本、VSCode 都可以），照下面表格填：**

| 變數 | 一定要填嗎？ | 怎麼填 |
|---|---|---|
| `DB_SA_PASSWORD` | ✅ 一定要 | 資料庫管理者密碼，隨便你設，但**至少 8 碼**，而且大寫、小寫、數字、符號至少要出現 3 種（例如 `MyPass123!` 這種格式），不符合規則資料庫會直接啟動失敗 |
| `JWT_TOKEN_SECRET` | ✅ 一定要 | 一組**至少 64 個字元**的亂碼，用來加密登入憑證。太短會直接啟動失敗。可以用這行指令產生一組：`openssl rand -base64 64 \| tr -d '\n'`，把印出來的結果整段貼進去 |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | ✅ 一定要（但可以先亂填） | 沒有要測「用 Google 帳號登入」這個功能的話，直接填假的就好，例如 `fake-client-id`、`fake-client-secret`，因為系統啟動當下就會檢查這兩個值不能是空的（不是說用到才檢查） |

**其他變數（`DB_PORT`、`BACKEND_PORT`、`FRONTEND_PORT` 等）都已經有預設值，不確定的話先不用動，全部先留著範本裡的樣子。**

**⚠️ 常見錯誤**：`.env` 這支檔案一定要放在跟 `docker-compose.yml` **同一層**（也就是 `work-order-system/` 這一層），不要放到 `work-order-backend/` 或 `work-order-front/` 裡面，不然 Docker 會讀不到，變成密碼全部是空的（啟動時會看到一堆 `variable is not set` 的警告）。

---

## 第三步：產生 HTTPS 憑證

這個系統走 HTTPS，需要一組憑證。本機測試用「自己簽發」的憑證就夠了（瀏覽器會跳警告，這是正常的，不是壞掉）。

```bash
bash docker/nginx/generate-cert.sh
```

跑完應該會看到：
```
憑證已產生：docker/nginx/certs/localhost.crt、docker/nginx/certs/localhost.key
```

如果你的終端機沒有 `bash` 指令（例如純 Windows CMD），改用 Git Bash 執行這個指令。

---

## 第四步：啟動整套系統

```bash
docker compose up -d --build
```

**第一次執行會比較久（10~20 分鐘都有可能）**，因為要：
- 下載 MSSQL 資料庫的 image（本身就有 1.5GB 左右）
- 下載後端需要的所有 Java 套件
- 下載前端需要的所有 npm 套件
- 編譯打包前後端程式碼

看到終端機停下來、出現提示字元，代表跑完了（不代表一定成功，下一步要確認）。

---

## 第五步：確認每個容器都正常

```bash
docker compose ps -a
```

你應該會看到 4 個容器，狀態要是這樣：

| 容器名稱 | 應該顯示的狀態 |
|---|---|
| `work-order-db` | `Up (healthy)` |
| `work-order-db-init` | `Exited (0)` ← **這個是正常的！**這支容器的工作是「把資料庫結構跟假資料灌進去」，做完就會自動結束，不是常駐服務，`Exited (0)` 代表「順利做完退出」，不是失敗 |
| `work-order-backend` | `Up` |
| `work-order-frontend` | `Up` |

**如果 `work-order-db` 不是 `healthy`**，通常是密碼不符合規則，執行 `docker compose logs db` 看詳細錯誤訊息。

**如果 `work-order-db-init` 不是 `Exited (0)`（例如變成 `Exited (1)`）**，執行 `docker compose logs db-init` 看是哪裡出錯。

---

## 第六步：打開瀏覽器

網址列輸入：
```
https://localhost
```

**會跳出「你的連線不是私人連線」這種警告畫面，這是正常的**（因為我們用的是自己簽發的憑證，瀏覽器不認得），點「進階」→「繼續前往 localhost（不安全）」就好，不影響功能。

看到登入頁面就代表整套系統成功跑起來了。

---

## 常見問題排查

### `docker compose up` 跳出 `variable is not set` 的警告

代表 `.env` 沒被讀到，檢查它是不是放錯資料夾（要跟 `docker-compose.yml` 同一層），或是根本沒有複製 `.env.example` 出來。

### 資料庫容器一直 `Exited (255)`

執行 `docker compose logs db`，通常是密碼不符合強度規則。改 `.env` 裡的 `DB_SA_PASSWORD`，然後：

```bash
docker compose down -v
docker compose up -d --build
```

（`down -v` 會清掉舊資料重新開始，因為密碼是「第一次啟動」才會生效，改密碼一定要配合 `-v` 重來）

### 某個 port 被佔用，啟動失敗

如果你電腦上剛好有東西佔用了 1433（資料庫）、8080（後端）、80/443（前端）這些 port，去 `.env` 改對應的 `DB_PORT`／`BACKEND_PORT`／`FRONTEND_PORT`／`FRONTEND_HTTPS_PORT`，改成別的數字，不用動 `docker-compose.yml`。

### 想砍掉重練，完全重新開始

```bash
docker compose down -v
docker compose up -d --build
```

`-v` 會連資料庫資料一起清掉，等於回到最原始的狀態。

---

## 指令

```bash
docker compose up -d --build     # 啟動整套系統（有改過程式碼才需要 --build）
docker compose up -d             # 啟動整套系統（沒改東西，用之前 build 過的版本）
docker compose ps -a             # 看所有容器的狀態
docker compose logs <容器名稱>    # 看某個容器的詳細 log，除錯用
docker compose down              # 全部關掉（資料庫資料還在）
docker compose down -v           # 全部關掉 + 清空資料庫資料
```
