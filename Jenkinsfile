pipeline {
    agent any
    
    environment {
        APP_NAME = 'cinevibe'

        DOCKERHUB_CREDENTIALS = credentials('dockerhub-account')
        DOCKER_HUB_USERNAME = credentials('docker-hub-username')
        DOCKER_HUB_PASSWORD = credentials('docker-hub-password')
        FIREBASE_SERVICE_ACCOUNT = credentials('firebase-service-account') // Thêm credential cho Firebase
        
        BUILD_TIMESTAMP = sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim()

        GIT_COMMIT_SHORT = ""
        IMAGE_VERSION = ""
    }

    stages {
        stage('Prepare Environment') {
            steps {
                cleanWs()

                checkout([$class: 'GitSCM',
                    branches: [[name: '*/deploy']],
                    extensions: [
                        [$class: 'CleanBeforeCheckout'],
                        [$class: 'CloneOption', depth: 0, noTags: false, reference: '', shallow: false],
                        [$class: 'CheckoutOption', timeout: 30]
                    ],
                    userRemoteConfigs: [[credentialsId: 'git', url: 'https://github.com/vku-k23/TTT-BE.git']]
                ])

                script {
                    GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    IMAGE_VERSION = "${BUILD_NUMBER}-${GIT_COMMIT_SHORT}-${BUILD_TIMESTAMP}"

                    def FULL_COMMIT = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

                    sh "echo 'Jenkins BUILD_NUMBER: ${BUILD_NUMBER}'"
                    sh "echo 'Current commit: ${GIT_COMMIT_SHORT}'"
                    sh "echo 'Full commit hash: ${FULL_COMMIT}'"
                    sh "echo 'Building image version: ${IMAGE_VERSION}'"
                    sh 'git log -n 3 --pretty=format:"%h - %s (%an, %ar)"'
                }
                
                script {
                    sh 'mkdir -p src/main/resources'
                    
                    withCredentials([file(credentialsId: 'firebase-service-account', variable: 'FIREBASE_JSON')]) {
                        sh 'cp $FIREBASE_JSON src/main/resources/firebase-service-account.json'
                    }
                    
                    sh 'ls -la src/main/resources/firebase-service-account.json || echo "File not created"'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build --no-cache -t ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION} ."

                    sh "docker images | grep ${DOCKER_HUB_USERNAME}/${APP_NAME}"
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
                }
            }
        }

        stage('Deploy Application') {
            steps {
                script {
                    def DOCKER_COMPOSE_PATH = "/home/ubuntu/cinevibe/docker-compose.yml"
                    
                    sh "sed -i 's|image: ${DOCKER_HUB_USERNAME}/${APP_NAME}:[^ ]*|image: ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION}|g' ${DOCKER_COMPOSE_PATH}"
                    
                    sh "echo 'Updated docker-compose.yml:'"
                    sh "cat ${DOCKER_COMPOSE_PATH}"
                    
                    sh "docker pull ${DOCKER_HUB_USERNAME}/${APP_NAME}:${IMAGE_VERSION}"
                    
                    sh "cd /home/ubuntu/cinevibe && docker compose -f ${DOCKER_COMPOSE_PATH} down --remove-orphans || true"
                    sh "cd /home/ubuntu/cinevibe && docker compose -f ${DOCKER_COMPOSE_PATH} up -d --force-recreate"
                    
                    sh "docker ps | grep ${APP_NAME}"
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

