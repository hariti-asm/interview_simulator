pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKER_REGISTRY = 'haritiasmae'
        APP_NAME = 'simulator'
        DOCKER_PATH = '/usr/local/bin'
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
                sh '''
                    export PATH=$DOCKER_PATH:$PATH
                    docker build -t $DOCKER_REGISTRY/$APP_NAME:$BUILD_NUMBER .
                '''
            }
        }

        stage('Docker Push') {
            environment {
                DOCKER_HUB = credentials('docker-hub-credentials')
            }
            steps {
                sh '''
                    export PATH=$DOCKER_PATH:$PATH
                    # Create a temporary file for credentials
                    echo "{\\\"auths\\\":{\\\"https://index.docker.io/v1/\\\":{\\\"auth\\\":\\\"$(echo -n $DOCKER_HUB_USR:$DOCKER_HUB_PSW | base64)\\\"}}}" > $WORKSPACE/docker-config.json
                    # Use the temporary config file for docker
                    export DOCKER_CONFIG=$WORKSPACE
                    # Push the images
                    docker push $DOCKER_REGISTRY/$APP_NAME:$BUILD_NUMBER
                    docker tag $DOCKER_REGISTRY/$APP_NAME:$BUILD_NUMBER $DOCKER_REGISTRY/$APP_NAME:latest
                    docker push $DOCKER_REGISTRY/$APP_NAME:latest
                    # Clean up the temporary config file
                    rm $WORKSPACE/docker-config.json
                '''
            }
        }

        stage('Check Deploy Prerequisites') {
            steps {
                script {
                    def missingCredentials = []
                    def requiredCredentials = [
                        'openai-api-key',
                        'jwt-secret',
                        'mail-username',
                        'mail-password',
                        'mail-host',
                        'mail-port'
                    ]

                    for (cred in requiredCredentials) {
                        try {
                            withCredentials([string(credentialsId: cred, variable: 'TEST')]) {
                                echo "Credential $cred is available"
                            }
                        } catch (Exception e) {
                            missingCredentials.add(cred)
                        }
                    }

                    if (missingCredentials.size() > 0) {
                        echo "Missing credentials: ${missingCredentials.join(', ')}"
                        echo "Deploy stage will be skipped"
                        env.DEPLOY_READY = 'false'
                    } else {
                        env.DEPLOY_READY = 'true'
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                expression { return env.DEPLOY_READY == 'true' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'openai-api-key', variable: 'OPENAI_API_KEY'),
                    string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET'),
                    string(credentialsId: 'mail-username', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'mail-password', variable: 'MAIL_PASSWORD'),
                    string(credentialsId: 'mail-host', variable: 'MAIL_HOST'),
                    string(credentialsId: 'mail-port', variable: 'MAIL_PORT')
                ]) {
                    sh '''
                        export PATH=$DOCKER_PATH:$PATH

                        echo "OPENAI_API_KEY=$OPENAI_API_KEY" > .env
                        echo "JWT_SECRET=$JWT_SECRET" >> .env
                        echo "DB_USERNAME=postgres" >> .env
                        echo "DB_PASSWORD=secret" >> .env
                        echo "MAIL_USERNAME=$MAIL_USERNAME" >> .env
                        echo "MAIL_PASSWORD=$MAIL_PASSWORD" >> .env
                        echo "MAIL_HOST=$MAIL_HOST" >> .env
                        echo "MAIL_PORT=$MAIL_PORT" >> .env

                        docker-compose down || true
                        docker-compose up -d
                    '''
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
            sh '''
                export PATH=$DOCKER_PATH:$PATH
                docker system prune -f || true
            '''
        }
    }
}