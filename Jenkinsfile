pipeline {
    agent any

    tools {
        maven 'Maven'
        dockerTool 'Docker'
    }

    environment {
        DOCKER_REGISTRY = 'haritiasmae'
        APP_NAME = 'Simulator'
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/hariti-asm/interview_simulator.git', branch: 'master'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin"
                    sh "docker push ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}"
                    sh "docker tag ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} ${DOCKER_REGISTRY}/${APP_NAME}:latest"
                    sh "docker push ${DOCKER_REGISTRY}/${APP_NAME}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'openai-api-key', variable: 'OPENAI_API_KEY'),
                    string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET'),
                    string(credentialsId: 'mail-username', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'mail-password', variable: 'MAIL_PASSWORD'),
                    string(credentialsId: 'mail-host', variable: 'MAIL_HOST'),
                    string(credentialsId: 'mail-port', variable: 'MAIL_PORT')
                ]) {
                    sh """
                        echo "OPENAI_API_KEY=${OPENAI_API_KEY}" > .env
                        echo "JWT_SECRET=${JWT_SECRET}" >> .env
                        echo "DB_USERNAME=postgres" >> .env
                        echo "DB_PASSWORD=secret" >> .env
                        echo "MAIL_USERNAME=${MAIL_USERNAME}" >> .env
                        echo "MAIL_PASSWORD=${MAIL_PASSWORD}" >> .env
                        echo "MAIL_HOST=${MAIL_HOST}" >> .env
                        echo "MAIL_PORT=${MAIL_PORT}" >> .env

                        docker-compose down
                        docker-compose up -d
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}