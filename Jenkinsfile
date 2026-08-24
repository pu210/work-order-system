pipeline {
    agent any

    // 每個人在自己的 Jenkins 裡跑，這兩個值因人/因機器而異，不寫死在程式碼裡。
    // 第一次跑會用預設值，之後在 Jenkins 網頁「Build with Parameters」改成自己的，
    // Jenkins 會記住你上次填的值，之後直接「Build Now」也會沿用
    parameters {
        string(
            name: 'DEPLOY_PATH',
            defaultValue: 'C:/Users/h2322/Desktop/git/work-order-system',
            description: '這個專案在部署目標機器上的絕對路徑（.env、docker-compose.yml 所在位置）'
        )
        string(
            name: 'DEPLOY_HOST',
            defaultValue: 'host.docker.internal',
            description: 'SSH 部署目標主機。Docker Desktop 底下用 host.docker.internal 連回自己電腦；若目標是別台機器，改成那台的位址'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Test') {
            steps {
                dir('work-order-backend') {
                    // mvnw 在 git 裡沒有標記可執行權限（Windows 開發常見），這裡先手動補上
                    sh 'chmod +x mvnw'
                    sh './mvnw clean test -B -Dtest=WorkOrderServiceTest,WorkOrderCreationCoordinatorTest,WorkOrderAttachmentServiceTest,WorkOrderCreateRequestValidationTest'
                }
            }
            post {
                always {
                    junit 'work-order-backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Images') {
            steps {
                // 只 build，不 up：build context 是打包傳給 daemon，跟路徑無關，DooD 底下沒問題
                sh 'docker compose build backend frontend'
            }
        }

        stage('Deploy') {
            steps {
                // 不透過 DooD 直接 up（bind mount 路徑會對不上），改成 SSH 回 host，
                // 在檔案真正存在的地方原生執行 docker compose up，順便沿用 host 上已經有的 .env。
                // 帳號從 Credentials 裡的私鑰本身取得（每個人在自己的 Jenkins 存自己的 jenkins-deploy-key 即可）
                withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-deploy-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh """
                        ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=no "\$SSH_USER"@${params.DEPLOY_HOST} "cd ${params.DEPLOY_PATH} && docker compose up -d --build"
                    """
                }
            }
        }
    }
}
