pipeline {
    agent any

    tools{
        maven "3.8.1"
    }

    stages {

        stage('Stop ms-article '){

            steps{
                // arrête du service
                sh """echo max | sudo -S systemctl stop ms-article"""
            }

        }

        stage('build ...'){

            steps{
            sh """mvn -version"""
            sh """mvn clean install"""
            }
        }

        stage('Start ms-article '){

            steps{

                // rechargement des deamons
                  sh """echo max | sudo -S systemctl daemon-reload"""

                // lancement du service
                sh """echo max | sudo -S systemctl start ms-article"""
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
