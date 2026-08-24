FROM jenkins/jenkins:lts

USER root

# 裝 docker CLI，讓 Jenkins 容器裡可以下 docker / docker compose 指令
# （實際操作的是掛進來的 host docker.sock，不是另一個獨立的 Docker）
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
       > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# Docker Desktop（Windows/Mac）底層的 docker.sock 擁有者是 root:root，
# 讓 jenkins 使用者加入 root 群組才能讀寫這個 socket。
# 注意：這在本機/教學用途可以接受，正式多人共用的伺服器上不建議這樣做（等同給 jenkins 使用者 root 權限）
RUN usermod -aG root jenkins

USER jenkins
