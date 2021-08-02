pipeline {
    agent any

    tools{
        maven "3.8.1"
    }

    stages {

        stage('Stop ms-article-service '){

            steps{
                // arrête du service
                sh """echo max | sudo -S systemctl stop ms-article-service"""
            }

        }

        stage('build ...'){

            steps{
            sh """mvn -version"""
            sh """mvn clean install -P prod"""
            }
        }

        stage('Start ms-article-service '){

            steps{

                // rechargement des deamons
                  sh """echo max | sudo -S systemctl daemon-reload"""

                // lancement du service
                sh """echo max | sudo -S systemctl start ms-article-service"""
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
