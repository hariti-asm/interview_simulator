pipeline {
    agent any

    tools {
        maven 'Maven 3.8.1'
        jdk 'Java 11'
    }

    environment {
        DOCKER_REGISTRY = 'haritiasmae'
        APP_NAME = 'Simulator'
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}")
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push()
                        docker.image("${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}").push('latest')
                    }
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
            sh 'docker system prune -f'
        }
    }
}