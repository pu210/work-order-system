pipeline {
    agent any

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
                // 在檔案真正存在的地方原生執行 docker compose up，順便沿用 host 上已經有的 .env
                withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-deploy-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh 'ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "$SSH_USER"@host.docker.internal "cd C:/Users/h2322/Desktop/git/work-order-system && docker compose up -d --build"'
                }
            }
        }
    }
}
