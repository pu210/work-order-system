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
