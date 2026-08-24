# ---- Stage 1：build（用 Node 把 Vue 原始碼編成靜態檔）----
FROM node:22-alpine AS build
WORKDIR /app

# 先只複製 package.json 抓依賴，善用 Docker layer cache
COPY package*.json ./
RUN npm ci

COPY . .

# VITE_API_URL 要在「build 時」就決定好，因為 Vite 環境變數是編譯期就寫死進 JS 檔案的，
# 不是 runtime 讀取的，跟後端那種執行期讀環境變數的方式不一樣。
# 這裡設成空字串，讓 axios.js 的 baseURL 變成相對路徑，改由 nginx 反向代理轉發給後端
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# ---- Stage 2：runtime（丟進 nginx 服務靜態檔 + 反向代理）----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY app-locations.conf /etc/nginx/app-locations.conf
COPY proxy_params.conf /etc/nginx/proxy_params.conf
# HTTPS 由前面的 caddy 服務負責，這裡只對內部網路提供一般 HTTP

EXPOSE 80
