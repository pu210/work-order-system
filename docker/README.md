# Docker 化架構說明

工單管理系統的容器化與 CI/CD 架構說明：前端、後端、資料庫全部用 Docker 包裝，搭配 Jenkins 做到 push 上去即可自動測試、打包、部署。

---

## 整體架構

```
                         使用者瀏覽器
                              │
                              │ 80 / 443
                              ▼
                    ┌───────────────────┐
                    │   frontend (nginx)│  ← 對外唯一入口
                    │  - 服務 Vue build │
                    │  - :80 自動轉 :443│
                    │  - HTTPS 終止在這 │
                    │  - 依路徑轉發後端 │
                    └─────────┬─────────┘
                              │ 內部 network，不對外開 port
                              ▼
                    ┌───────────────────┐
                    │ backend (Tomcat)  │
                    │  webapps/ROOT.war │
                    │     port 8080     │
                    └─────────┬─────────┘
                              │ 內部 network，不對外開 port
                              ▼
                    ┌───────────────────┐
                    │   db (MSSQL)      │
                    │     port 1433     │
                    │  volume 持久化    │
                    │  01-schema.sql /  │
                    │  02-seed.sql      │
                    │  首次啟動建表     │
                    └───────────────────┘
```

另外有一個獨立的 **Jenkins** 容器，負責 CI/CD，跟上面三個服務的生命週期分開管理。

---

## 關鍵設計決定

- **後端 runtime 用 Tomcat，不是純 JRE 跑 jar。**
  `pom.xml` 是 `packaging=war`，且 `spring-boot-starter-tomcat` 是 `<scope>provided</scope>`——代表設計上就是打包成 WAR、丟進外部 Tomcat 容器跑。Dockerfile 分兩階段：build 階段用 Maven+JDK21 產出 WAR，runtime 階段用官方 `tomcat` image，把 WAR 複製進去改名 `ROOT.war` 放到 `webapps/`。

- **只有 `frontend`（nginx）對外開 port**，`backend`、`db` 都只在 Docker 內部網路，外部連不到,不用額外防火牆規則。

- **HTTPS 終止在 nginx**，不在 Spring Boot 層處理 TLS。本機用自簽憑證，正式部署到有網域的伺服器可換成 Let's Encrypt。

- **前端 `nginx` 依 HTTP method 分流路由**：前端頁面路由（如 `/auth/login`）跟後端 API 路徑剛好相同,用 GET/POST 判斷該交給前端頁面還是轉發後端,不用更動任何一行後端程式碼。

- **MSSQL 用一次性的 `db-init` 容器建表**：MSSQL 沒有像 MySQL/Postgres 那種「自動執行 `/docker-entrypoint-initdb.d/`」的機制,所以另外用一個容器,等 `db` healthy 後主動用 `sqlcmd` 把 schema 跟假資料灌進去,執行完就結束,不是常駐服務。`db-init` 會依檔名順序自動跑 `/init` 底下所有 `.sql`，之後要加新的初始化腳本，丟檔案進去就好，不用改設定。

- **環境變數規則**：密碼、密鑰、port 這類每人/每環境可能不同的東西一律走 `.env`（不進版控），`docker-compose.yml` 裡只寫 `${VAR_NAME}`。新增變數要同步更新 `.env.example`（進版控，當範本）。

- **CI/CD 用 SSH 部署，不直接透過容器操作 Docker 部署**：Jenkins 容器透過掛載 host 的 `docker.sock` 操作 Docker 來 build image；但部署（`docker compose up`，牽涉檔案掛載）改成讓 Jenkins SSH 回 host，在檔案真正存在的地方原生執行部署指令，同時沿用 host 上已有的 `.env`，密碼不需要另外交給 Jenkins。

---

## Jenkins Pipeline

`Jenkinsfile` 定義 4 個關卡：

```
Checkout       → 拉最新程式碼
Backend Test   → 跑後端單元測試
Build Images   → 打包前後端 Docker image
Deploy         → SSH 回 host，原生執行 docker compose up -d --build
```

---

## 檔案結構

```
work-order-system/
├── docker-compose.yml              # db / db-init / backend / frontend 四個 service
├── .env                            # 不進版控，每個人自己的密碼/port/密鑰
├── .env.example                    # 進版控，變數範本
│
├── docker/
│   ├── README.md                   # 這份文件
│   ├── ENV.md                      # 環境變數速查表
│   ├── BUILD_GUIDE.md              # 從零開始的建置手冊
│   ├── db/init/
│   │   ├── 01-schema.sql           # 資料庫 schema（18 張表、外鍵統一在後段補上）
│   │   └── 02-seed.sql             # 假資料
│   ├── nginx/
│   │   ├── generate-cert.sh        # 產生本機自簽憑證
│   │   └── certs/                  # 憑證存放位置（不進版控）
│   └── jenkins/
│       ├── Dockerfile              # Jenkins + docker CLI
│       └── docker-compose.yml      # Jenkins 獨立部署
│
├── work-order-backend/
│   ├── Dockerfile                  # Maven+JDK21 build WAR → Tomcat 跑
│   ├── .dockerignore
│   └── src/main/resources/
│       └── application-docker.properties   # Docker 專用設定，敏感值走環境變數
│
└── work-order-front/
    ├── Dockerfile                  # Node build 靜態檔 → nginx 服務
    ├── .dockerignore
    ├── nginx.conf                  # server block 骨架：:80 轉址、:443 掛憑證
    ├── app-locations.conf          # 實際路由規則
    ├── proxy_params.conf           # 共用 proxy header
    └── src/plugins/axios.js        # baseURL 支援相對路徑（供 nginx 反向代理）
```

---

## 指令備忘

```bash
# 啟動（第一次 / 改了 Dockerfile 或程式碼）
docker compose up -d --build

# 啟動（沒改東西）
docker compose up -d

# 看目前有哪些容器（含已結束的一次性任務）
docker compose ps -a

# 看某個 service 的 log
docker compose logs -f <service名稱>

# 全部停掉（保留資料）
docker compose down

# 全部停掉 + 清掉資料庫資料
docker compose down -v
```

詳細的從零開始建置步驟，見 [`BUILD_GUIDE.md`](./BUILD_GUIDE.md)。
