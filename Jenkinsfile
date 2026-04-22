pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'TestingJenkinsMavenDocker'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = 'docker.io/myuser'
        APP_PORT = '8080'
    }

    tools {
        maven 'Maven-3'
        jdk 'JDK-21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./scripts/build.sh'
                sh './scripts/build.sh'
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x ./scripts/test.sh'
                sh './scripts/test.sh'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'chmod +x ./scripts/package.sh'
                sh './scripts/package.sh'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'chmod +x ./scripts/docker-build.sh'
                sh "./scripts/docker-build.sh ${DOCKER_IMAGE} ${DOCKER_TAG}"
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'chmod +x ./scripts/docker-push.sh'
                    sh "./scripts/docker-push.sh ${DOCKER_REGISTRY} ${DOCKER_IMAGE} ${DOCKER_TAG} ${DOCKER_USER} ${DOCKER_PASS}"
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'chmod +x ./scripts/deploy.sh'
                sh "./scripts/deploy.sh ${DOCKER_IMAGE} ${DOCKER_TAG} ${APP_PORT}"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

