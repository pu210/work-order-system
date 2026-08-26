#!/bin/bash
# 產生本機測試用的自簽憑證（瀏覽器會跳警告，這是正常的，功能不受影響）
# 用法：在專案根目錄下執行 bash docker/nginx/generate-cert.sh

set -e
mkdir -p "$(dirname "$0")/certs"
cd "$(dirname "$0")/certs"

MSYS_NO_PATHCONV=1 openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout localhost.key -out localhost.crt \
  -subj "/C=TW/ST=Taiwan/L=Taipei/O=WorkOrderSystem/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "憑證已產生：docker/nginx/certs/localhost.crt、docker/nginx/certs/localhost.key"
