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

        stage('Build JAR using Docker (Multi-stage)') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
            }
        }
    //     stage('Deploy using Docker Compose') {
    //         steps {
    //             sh 'docker-compose down || true'
    //             sh 'docker-compose up -d'
    //         }
    //     }
     }

    // post {
    //     success {
    //         echo "✅ Deployment successful"
    //     }
    //     failure {
    //         echo "❌ Deployment failed"
    //     }
    // }
}
