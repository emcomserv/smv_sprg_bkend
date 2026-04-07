pipeline {
    agent { label 'Builder' }

    environment {
        IMAGE_NAME = 'smart_vehicle'
        IMAGE_TAG = '1.0'
        IMAGE_TAR = 'smart_vehicle.tar'
        TARGET_HOST = '68.178.203.99'
        DEPLOY_DIR = '/home/appusr/application/smv_sprg_bkend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Copy Firebase JSON') {
            steps {
                sh """
                    cp /home/ec2-user/tmp/trakme-6ea58-cb7061e0641c.json src/main/resources
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build --no-cache -t ${IMAGE_NAME}:${IMAGE_TAG} .
                """
            }
        }

        stage('Export, Transfer & Remote Deploy') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'ftp-creds', usernameVariable: 'FTP_USER', passwordVariable: 'FTP_PASS'),
                    usernamePassword(credentialsId: 'ssh-creds', usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASS')
                ]) {
                    sh """
                        # Save the Docker image
                        docker save -o ${IMAGE_TAR} ${IMAGE_NAME}:${IMAGE_TAG}

                        docker system prune -f

                        # SCP the image tar to remote FTP user's home
                        sshpass -p ${FTP_PASS} scp -o StrictHostKeyChecking=no ${IMAGE_TAR} ${FTP_USER}@${TARGET_HOST}:/home/${FTP_USER}/ftp/builds

                        # SSH, move tar, load image, and use docker-compose
                        sshpass -p ${SSH_PASS} ssh -o StrictHostKeyChecking=no ${SSH_USER}@${TARGET_HOST} bash -c "'
                            sudo bash -c \\"  
                                    cp /home/${FTP_USER}/ftp/builds/${IMAGE_TAR} ${DEPLOY_DIR}/
                                    mv /home/${FTP_USER}/ftp/builds/${IMAGE_TAR} /home/${FTP_USER}/ftp/builds/smart_vehicle_\\\$(date +%Y-%m-%d_%H-%M-%S).tar
                                    cd ${DEPLOY_DIR}
                                    chmod 644 ${IMAGE_TAR}
                                    docker load -i ${IMAGE_TAR}
                                    docker compose down || true
                                    docker compose up -d
                                    docker image prune -f
                                \\"
                        '"
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful"
        }
        failure {
            echo "❌ Deployment failed"
        }
    }
}
