# Docker 化筆記

這份文件記錄容器化的架構決定、目前進度、指令備忘，方便討論跟同步調整。

---

## 目標

1. **本機開發環境統一化**：`docker-compose up -d` 一行指令，前端＋後端＋資料庫全部啟動，組員不用各自裝 MSSQL、對版本。
2. **可交付部署的映像檔**：之後能真的部署到有網域的伺服器上。
3. 之後銜接 **Jenkins**（CI/CD build/deploy）與 **HTTPS**（自簽憑證本機用／Let's Encrypt 正式環境用），先不實作，只確定架構方向。
4. 理想狀態：**一鍵部署**（`deploy.sh` 之類的腳本包住 `docker compose up -d --build`）。

---

## 整體架構

```
                         使用者瀏覽器
                              │
                              │ 80 / 443
                              ▼
                    ┌───────────────────┐
                    │   frontend (nginx)  │  ← 對外唯一入口
                    │  - 服務 Vue build   │
                    │  - 反向代理 /api →  │
                    │    backend:8080     │
                    │  - HTTPS 終止在這   │
                    └─────────┬─────────┘
                              │ 內部 network，不對外開 port
                              ▼
                    ┌───────────────────┐
                    │ backend (Tomcat)    │
                    │  webapps/ROOT.war   │
                    │     port 8080       │
                    └─────────┬─────────┘
                              │ 內部 network，不對外開 port
                              ▼
                    ┌───────────────────┐
                    │   db (MSSQL)        │
                    │     port 1433       │
                    │  volume 持久化       │
                    │  db_docker_init.sql  │
                    │  首次啟動建表         │
                    └───────────────────┘
```

### 關鍵決定

- **後端 runtime 用 Tomcat，不是純 JRE 跑 jar。**
  `pom.xml` 是 `packaging=war`，且 `spring-boot-starter-tomcat` 是 `<scope>provided</scope>`——代表設計上就是打包成 WAR、丟進外部 Tomcat 容器跑。Dockerfile 兩階段：build 階段用 Maven+JDK21 產出 WAR，runtime 階段用官方 `tomcat` image，把 WAR 複製進去改名 `ROOT.war` 放到 `webapps/`。

- **只有 `frontend`（nginx）對外開 port**，`backend`、`db` 都只在 Docker 內部網路，外部連不到，不用額外防火牆規則。

- **HTTPS 終止在 nginx**，不在 Spring Boot 層處理 TLS。本機/demo 用自簽憑證，真的部署到有網域的伺服器才用 Let's Encrypt。

- **MSSQL 沒有像 MySQL/Postgres 那種「自動執行 `/docker-entrypoint-initdb.d/`」的機制**，所以多一個一次性的 `db-init` 容器，等 `db` healthy 後用 `sqlcmd` 主動把 schema 灌進去。

- **環境變數規則**：密碼、密鑰、port 這種「每人/每環境可能不同」的東西一律走 `.env`（不進版控），`docker-compose.yml` 裡只寫 `${VAR_NAME}`。新增變數要同步更新 `.env.example`（進版控，當範本）。

---

## 目前進度

- [x] **第 1 步：資料庫容器（`db` + `db-init`）** —— 已驗證成功
  - `db`：`Up (healthy)`
  - `db-init`：`Exited (0)`（一次性任務，正常結束）
  - 直接連進去查證：18 張表、29 個外鍵，跟 `docker/db/init/01-schema.sql` 完全一致
- [x] **第 2 步：後端容器（Tomcat + WAR）** —— 已驗證成功
  - `backend`：`Up`，Spring profile `docker` 正確載入
  - Hibernate `ddl-auto=validate` 通過，代表 42 支 Entity 跟 `db_docker_init.sql` 建的 schema 完全一致
  - HikariCP 成功連上 `db:1433/workorderDB`
  - `curl http://localhost:8080/api/work-orders` 回 401（沒帶 JWT 被正確擋下，代表 Tomcat → Spring Security → JWT 過濾器整條鏈路都通）
- [x] **第 3 步：前端容器（nginx）** —— 已驗證成功
  - `frontend`：`Up`，`http://localhost` 對外開放
  - `axios.js` 的 `\|\|` 改成 `??`，讓 `VITE_API_URL=""`（build arg）能生效，改走相對路徑
  - 解掉 `/auth/login`、`/auth/register`、`/auth/reset-password` 三個前端路由跟後端 API 撞路徑的問題（nginx 依 method 分流，見 `work-order-front/nginx.conf`）
  - `db-init` 改成依檔名順序自動跑 `/init` 底下所有 `.sql`，加了 `02-seed.sql`（假資料，15 使用者/20 工單等），驗證灌入成功
- [x] **第 4 步：HTTPS（自簽憑證）** —— 已驗證成功
  - `:80` 自動 301 轉址到 `:443`，`:443` 用自簽憑證提供 HTTPS
  - `curl -k https://localhost` 回 200；瀏覽器打開會跳「連線不安全」警告（自簽憑證正常現象，點繼續前往即可）
  - `nginx.conf` 拆成 `nginx.conf`（server block 骨架）+ `app-locations.conf`（實際路由規則），避免 `:80`/`:443` 兩邊重複貼一樣的規則
- [x] **第 5 步：Jenkins（checkout + 後端測試）** —— 已驗證成功
  - `docker/jenkins/`：獨立 compose，跟 app 生命週期分開；掛 host `docker.sock`（DooD），讓 Jenkins 容器能操作 host 的 Docker
  - `jenkins` 使用者要加進 `root` 群組才能讀寫 socket（Docker Desktop 底層 socket 是 `root:root`）
  - `Jenkinsfile`：checkout GitHub repo → 跑 B 模組 4 支測試（43 案例）→ `junit` 回報結果
  - 踩過的坑：`mvnw` 在 git 裡沒標記可執行權限（`git update-index --chmod=+x`）；`mvn test` 不能跑全專案，因為有些測試（如樣板 `WorkOrderSystemTest`）需要真實資料庫，CI 環境沒有，範圍縮小成只跑 B 模組自己的測試
  - **待做**：build image、部署這兩關還沒接上，卡在 DooD 的 bind mount 路徑問題（見上方「待確認」）

---

## 檔案結構

```
work-order-system/
├── docker-compose.yml              # db / db-init / backend / frontend 四個 service
├── .env                            # 不進版控，每個人自己的密碼/port/密鑰
├── .env.example                    # 進版控，變數範本
├── .gitignore                      # 有加 .env、docker/nginx/certs/*.key|crt|pem 排除
│
├── docker/
│   ├── README.md                   # 這份文件
│   ├── ENV.md                      # 環境變數速查表（簡短版）
│   ├── db/
│   │   └── init/
│   │       ├── 01-schema.sql       # 資料庫初始化 schema（重組自 db_3.0.sql：
│   │       │                       #   先建表、外鍵統一在後段 ALTER TABLE 補上，
│   │       │                       #   避免表格建立順序互相牽制）
│   │       └── 02-seed.sql         # 假資料（15 使用者/20 工單/17 子類別等），
│   │                                #   db-init 會依檔名順序自動跑完 /init 底下所有 .sql
│   └── nginx/
│       ├── generate-cert.sh        # 產生本機自簽憑證的腳本（憑證本身不進版控）
│       └── certs/                  # openssl 產生的 localhost.crt / localhost.key（gitignore）
│
├── work-order-backend/
│   ├── Dockerfile                  # 兩階段：Maven+JDK21 build WAR → Tomcat 跑
│   ├── .dockerignore                # 排除 target/、含真實密碼的 application.properties
│   └── src/main/resources/
│       └── application-docker.properties   # Docker 專用 Spring profile，
│                                            #   所有敏感值走 ${ENV_VAR} 環境變數，
│                                            #   不含任何真實密碼，進版控
│
└── work-order-front/
    ├── Dockerfile                  # 兩階段：Node build 靜態檔 → nginx 服務
    ├── .dockerignore                # 排除 node_modules/、dist/、.env
    ├── nginx.conf                  # server block 骨架：:80 轉址 :443、:443 掛憑證 + include
    ├── app-locations.conf          # 實際路由規則（/api/、/auth/ 分流、/ws/、SPA fallback）
    ├── proxy_params.conf           # 共用的 proxy header 設定，被各 location 用 include 引用
    └── src/plugins/axios.js        # 改動：baseURL 判斷從 || 改成 ??，
                                     #   讓 VITE_API_URL="" 能生效走相對路徑（原行保留註解對照）
```

---

## 指令備忘

```bash
# 啟動（第一次 / 改了 Dockerfile 或程式碼）
docker compose up -d --build

# 啟動（沒改東西）
docker compose up -d

# 看目前有哪些容器（含已結束的一次性任務，不加 -a 只顯示還在跑的）
docker compose ps -a

# 看某個 service 的 log
docker compose logs -f <service名稱>

# 全部停掉（保留資料）
docker compose down

# 全部停掉 + 清掉資料庫資料（會清空重來，通常只有想重跑 init script 才用）
docker compose down -v

# 直接連進資料庫容器下 SQL 驗證（Git Bash 要加 MSYS_NO_PATHCONV=1 避免路徑被自動轉換）
MSYS_NO_PATHCONV=1 docker exec work-order-db /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "你的密碼" -C -d workorderDB \
  -Q "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES ORDER BY TABLE_NAME;"
```



## 待確認 / 開放問題

- [ ] Jenkins 要自己用 Docker 架（已確認，會帶著一起做），但還沒開始：Jenkins 容器要怎麼操作到 host 的 Docker（掛 `docker.sock` 或 SSH 到部署主機）待決定
- [ ] 真的要部署到有網域的雲端伺服器時，自簽憑證要換成 Let's Encrypt（`certbot`），現在先用自簽憑證跑本機/demo
- [x] ~~後端容器要用的 Tomcat 版本~~ —— 已確認用 `tomcat:10.1-jdk21`，跟 Spring Boot 4 / Jakarta EE 10 相容，驗證通過
