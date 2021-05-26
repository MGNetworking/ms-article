pipeline {
    agent any

    tools{
        maven "3.8.1"
    }

    stages {

        stage('Stop eureka-service '){

            steps{
                // arrête du service
                sh """echo max | sudo -S systemctl stop eureka-service"""
            }

        }

        stage('build ...'){

            steps{
            sh """mvn -version"""
            sh """mvn clean install"""
            }
        }

        stage('Start eureka-service '){

            steps{

                // rechargement des deamons
                  sh """echo max | sudo -S systemctl daemon-reload"""

                // lancement du service
                sh """echo max | sudo -S systemctl start eureka-service"""
            }

        }
    }

    post {
        // raffraichi le workspace
        always {
            cleanWs()
        }
  }
}
