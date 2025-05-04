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
//         stage('Build Application') {
//             steps {
//                 sh 'chmod +x mvnw'
//                 sh './mvnw clean package -DskipTests=true'
//             }
//         }
//
//         stage('Run Tests') {
//             steps {
//                 sh './mvnw test'
//             }
//             post {
//                 always {
//                     junit '**/target/surefire-reports/*.xml'
//                 }
//             }
//         }
        
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
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-account', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
                                             sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
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
                    
                    sh "docker-compose down || true"
                    sh "docker-compose up -d"
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