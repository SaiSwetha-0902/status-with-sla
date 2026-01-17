pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("status-with-sla:${env.BUILD_NUMBER}")
                }
            }
        }

        stage('Docker Compose Test') {
            steps {
                script {
                    sh 'docker compose up -d --build'
                    sh 'sleep 30'
                    sh 'curl -f http://localhost:8085/actuator/health || exit 1'
                    sh 'docker compose down'
                }
            }
        }
    }

    post {
        always {
            sh 'docker compose down || true'
            cleanWs()
        }
    }
}