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
                sh '''
                    cp /home/ec2-user/tmp/trakme-6ea58-cb7061e0641c.json src/main/resources
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Export, Transfer & Remote Deploy') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'ftp-creds', usernameVariable: 'FTP_USER', passwordVariable: 'FTP_PASS'),
                    usernamePassword(credentialsId: 'ssh-creds', usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASS')
                ]) {
                    sh '''
                        # Save the Docker image
                        docker save -o ${IMAGE_TAR} ${IMAGE_NAME}:${IMAGE_TAG}

                        # SCP the image tar to remote FTP user's home
                        sshpass -p ${FTP_PASS} scp -o StrictHostKeyChecking=no ${IMAGE_TAR} ${FTP_USER}@${TARGET_HOST}:/home/${FTP_USER}/ftp

                        # SSH as root, move tar, load image, and use docker-compose
                        sshpass -p ${SSH_PASS} ssh -o StrictHostKeyChecking=no ${SSH_USER}@${TARGET_HOST} << EOF
                            mv /home/${FTP_USER}/ftp/${IMAGE_TAR} ${DEPLOY_DIR}/
                            cd ${DEPLOY_DIR}
                            docker load -i ${IMAGE_TAR}
                            docker compose down || true
                            docker compose up -d
                        EOF
                    '''
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
