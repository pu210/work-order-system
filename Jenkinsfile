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
                // 只 build，不 up：build context 是打包傳給 daemon，跟路徑無關，DooD 底下沒問題；
                // 部署（up，牽涉 bind mount）先不做，卡在 DooD 的路徑問題，留到之後解決
                sh 'docker compose build backend frontend'
            }
        }
    }
}
