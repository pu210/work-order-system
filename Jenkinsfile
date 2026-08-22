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
                    sh './mvnw clean test -B'
                }
            }
            post {
                always {
                    junit 'work-order-backend/target/surefire-reports/*.xml'
                }
            }
        }
    }
}
