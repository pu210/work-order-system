# Jenkins 建置手冊

前面的 `docker/BUILD_GUIDE.md` 是把系統（前端＋後端＋資料庫）跑起來；這份是額外再架一套 **Jenkins**，讓你 push 程式碼後可以自動測試、打包、部署到你自己的電腦。

每個人要在自己電腦上用，都要照這份文件重做一次（金鑰、帳號、路徑都是每台機器各自獨立的）。

---

## 前提

先照 `docker/BUILD_GUIDE.md` 把系統跑起來過一次，確認 `.env`、憑證都準備好了，Docker 本身沒問題。

---

## 第一步：開啟 Windows 的 SSH 服務

Jenkins 之後要透過 SSH 連回你這台電腦執行部署指令，所以要先開啟 Windows 內建的 OpenSSH Server。

**用系統管理員身分開一個 PowerShell**（搜尋 PowerShell → 右鍵 → 以系統管理員身分執行），依序執行：

```powershell
# 確認有沒有裝過
Get-WindowsCapability -Online | Where-Object Name -like 'OpenSSH*'

# 安裝 OpenSSH Server
Add-WindowsCapability -Online -Name OpenSSH.Server~~~~0.0.1.0

# 啟動服務，並設成開機自動啟動
Start-Service sshd
Set-Service -Name sshd -StartupType 'Automatic'

# 確認防火牆規則有開（通常裝完會自動加）
Get-NetFirewallRule -Name *ssh*
```

`Get-NetFirewallRule` 那步應該會看到 `OpenSSH-Server-In-TCP`，`Enabled : True`，代表 port 22 對內部網路是開放的。

---

## 第二步：產生一組專用的 SSH 金鑰

**不要用你平常登入用的那組金鑰**，另外產生一組只給 Jenkins 部署用的：

```bash
mkdir -p docker/jenkins/ssh
ssh-keygen -t ed25519 -f docker/jenkins/ssh/jenkins_deploy_key -N "" -C "jenkins-deploy"
```

Windows

```bash
New-Item -ItemType Directory -Force docker/jenkins/ssh
ssh-keygen -t ed25519 -f docker/jenkins/ssh/jenkins_deploy_key -N '""' -C "jenkins-deploy"
```

（這個資料夾已經被 `.gitignore` 排除，私鑰不會進版控）

---

## 第三步：把公鑰放進 Windows 的授權清單

**這一步要注意**：如果你的 Windows 帳號是系統管理員（大部分開發用電腦都是），公鑰不能放在一般的 `~/.ssh/authorized_keys`，要放在**專屬於管理員帳號的位置**，而且權限要設定得很嚴格。

回到系統管理員 PowerShell：

```powershell
# 把公鑰內容貼進去（把下面這段換成你自己 jenkins_deploy_key.pub 檔案的內容）
$pubKey = "貼上 docker/jenkins/ssh/jenkins_deploy_key.pub 檔案裡的完整內容"
Add-Content -Path "$env:ProgramData\ssh\administrators_authorized_keys" -Value $pubKey -Encoding ASCII

# 修正權限：只能 SYSTEM 跟 Administrators 存取，不然 sshd 會拒絕登入
icacls "$env:ProgramData\ssh\administrators_authorized_keys" /inheritance:r
icacls "$env:ProgramData\ssh\administrators_authorized_keys" /grant "SYSTEM:F" "Administrators:F"

# 重啟 sshd 讓設定生效
Restart-Service sshd
```

**如果你的帳號不是系統管理員**，改成一般路徑就好：

```powershell
Add-Content -Path "$env:USERPROFILE\.ssh\authorized_keys" -Value $pubKey
```

---

## 第四步：架 Jenkins

```bash
cd docker/jenkins
docker compose up -d --build
```

第一次啟動後，拿初始密碼：

```bash
docker exec work-order-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

瀏覽器打開 `http://localhost:9090`（或你自己在 `.env` 設定的 `JENKINS_PORT`），貼密碼解鎖，選「安裝建議的外掛」，建立自己的管理員帳號。

---

## 第五步：把私鑰存進 Jenkins Credentials

1. **Manage Jenkins** → **Credentials** → **System** → **Global credentials** → **Add Credentials**
2. **Kind**：SSH Username with private key
3. **ID**：填 `jenkins-deploy-key`（**一定要打這個名字**，`Jenkinsfile` 就是用這個 ID 去抓，名字要一致）
4. **Username**：填你的 Windows 使用者帳號名稱
5. **Private Key** → **Enter directly**，打開 `docker/jenkins/ssh/jenkins_deploy_key` 檔案，**全選（含頭尾 `-----BEGIN...`/`-----END...`）複製貼上**
6. **Passphrase** 留空
7. 存檔

---

## 第六步：建立 Pipeline Job

1. **新增作業** → 輸入名稱 → 選 **Pipeline** → 確定
2. **Pipeline** 區塊：
   - **Definition**：Pipeline script from SCM
   - **SCM**：Git
   - **Repository URL**：`https://github.com/pu210/work-order-system.git`
   - **Branch Specifier**：`*/你要追蹤的分支`（例如 `*/dev`）
   - **Script Path**：`Jenkinsfile`（預設值，不用改）
3. 存檔

---

## 第七步：第一次建置（用預設參數跑一次）

`Jenkinsfile` 裡的 `DEPLOY_PATH`／`DEPLOY_HOST` 這兩個參數，**要等 Jenkins 讀過一次 `Jenkinsfile` 才會出現在畫面上**，所以：

1. 第一次先直接點「**立即建置**」（這次會照 `Jenkinsfile` 裡寫的預設值跑，如果預設路徑不是你的，Deploy 那關會失敗，沒關係）
2. 跑完之後，job 畫面左側會出現「**Build with Parameters**」
3. 點進去，把 `DEPLOY_PATH` 改成你自己這個專案在你電腦上的絕對路徑（例如 `C:/Users/你的帳號/Desktop/git/work-order-system`），`DEPLOY_HOST` 維持預設 `host.docker.internal` 就好
4. 建置，這次應該就會四關全部成功

之後 Jenkins 會記住你上次填的參數值，直接點「立即建置」也會沿用，不用每次都重填。

---

## 驗證整條流程通了沒

跑完 4 關都成功後，瀏覽器打開你自己電腦上系統的網址（`https://localhost`），確認畫面正常，代表整條「push → 自動測試 → 自動打包 → 自動部署」都通了。
