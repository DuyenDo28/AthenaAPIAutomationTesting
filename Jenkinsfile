pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run API Tests') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'api-credential',
                        usernameVariable: 'API_USERNAME',
                        passwordVariable: 'API_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: 'db-credential',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    bat 'mvn clean test -Dtest=AllApiTestSuite'
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
        }
    }
}
