pipeline {
    agent any

    environment {
        IMAGE_NAME = 'smart_vehicle'
        IMAGE_TAG = '1.0'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Copy Firebase JSON') {
            steps {
                sh 'cp /home/appusr/application/smv_sprg_bkend/src/main/resources/trakme-6ea58-cb7061e0641c.json src/main/resources'
            }
        }
        
        stage('Build & Deploy using Docker Compose') {
            steps {
                sh '''
                    docker compose down || true
                    docker compose up -d --build
                    docker system prune -f
                '''
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
