pipeline {
    agent any

    tools{
        maven "3.8.1"
    }

    stages {

        stage('build ...'){

            steps{
            sh """mvn -version"""
            sh """mvn clean install -P prod"""
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
