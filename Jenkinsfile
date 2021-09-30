pipeline {
    agent any

    tools{
        maven "3.8.1"
    }

    stages {




        stage('build service msarticle...'){

            steps{
            sh """mvn -version"""
            sh """mvn clean install -Dspring.profiles.active=prod"""
            }
        }

        stage('Start service msarticle '){

            steps{

                // rechargement des deamons
                  sh """echo max | sudo -S systemctl daemon-reload"""

                // lancement du service
                sh """echo max | sudo -S systemctl start msarticle"""
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
