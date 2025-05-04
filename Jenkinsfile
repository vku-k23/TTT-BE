pipeline {
    agent any
    
    environment {
        APP_NAME = 'cinevibe'

        DOCKERHUB_CREDENTIALS = credentials('dockerhub-account')
        DOCKER_HUB_USERNAME = credentials('docker-hub-username')
        DOCKER_HUB_PASSWORD = credentials('docker-hub-password')
        
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        IMAGE_VERSION = "${BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
    }
    
    stages {
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION} ."
                    sh "docker tag ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION} ${DOCKER_HUB_USERNAME}/${APP_NAME}:latest"
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-account', usernameVariable: 'DOCKER_HUB_USERNAME', passwordVariable: 'DOCKER_HUB_PASSWORD')]) {
                        sh "echo ${DOCKER_HUB_PASSWORD} | docker login -u ${DOCKER_HUB_USERNAME} --password-stdin"
                    }
                    sh "docker push ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION}"
                    sh "docker push ${DOCKER_HUB_USERNAME}/${APP_NAME}:latest"
                }
            }
        }
        
        stage('Deploy Application') {
            steps {
                script {
                    def DOCKER_COMPOSE_PATH = "/home/ubuntu/cinevibe/docker-compose.yml"
                    
                    sh "sed -i 's|image: ${DOCKER_HUB_USERNAME}/${APP_NAME}:[^ ]*|image: ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION}|g' ${DOCKER_COMPOSE_PATH}"
                    
                    sh "docker compose down || true"
                    sh "docker compose up -d"
                }
            }
        }
        
        stage('Notify Deployment') {
            steps {
                script {
                    echo "Successfully deployed ${APP_NAME}:${IMAGE_VERSION}"
                }
            }
        }
    }
    
    post {
        always {
            sh "docker rmi ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION} || true"
            sh "docker rmi ${DOCKER_HUB_USERNAME}/${APP_NAME}:latest || true"
            sh "docker system prune -af"
            sh "docker logout"
            
            cleanWs()
        }
        
        success {
            echo 'Build and deployment successful!'
        }
        
        failure {
            echo 'Build or deployment failed!'
        }
    }
}
