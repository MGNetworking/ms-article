@Library('JenkinsLib_Shared') _


// note => le WORKSPACE /var/jenkins_home/workspace/Ms article Teste Maven
// donc c'est /var/jenkins_home/workspace/${Nom du pipeline projet}

pipeline {
    agent any
    stages {

        stage('Maven compilation') {
            agent {
                docker {
                    image 'maven:3.8.5-jdk-8-slim'
                    // conteneur créer a la voler dans le workspace/projet pour exécuter des commandes
                    // mapping /var/jenkins_home/workspace/projet (pom.xml) vers le conteneur
//                    args '-v /var/jenkins_home/settings.xml:/root/.m2/settings.xml' +
//                            ' -v /var/run/docker.sock:/var/run/docker.sock' +
//                            ' -v /var/jenkins_home/maven:/root/.m2/repository'

                    args '-v /var/jenkins_home/maven/.m2:/root/.m2' +
                            ' -v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                script {
                    sh '''
                        export CONFIG_SERVICE_URI_host="http://192.168.1.27:8089"
                        mvn clean package "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=dev"
                    '''
                }
            }
        }

        stage('Docker Build') {
            agent any
            steps {
                script {
                    sh 'ls -al target/'
                    // Accédez aux résultats du build précédent dans le dossier de travail
                    sh 'docker compose -f docker-compose.yml build --no-cache'

                }
            }
        }

        stage('Deploy Build ms-article') {
            agent any
            steps {
                script {
                    // Accédez aux résultats du build précédent dans le dossier de travail
                    sh 'docker stack deploy -c ./docker-compose-swarm.yml ms-article'
                }
            }
        }
    }


    post {
        always {
            script {
                echo "Fin du test ..."
            }
        }
        success {
            echo 'Réussite du build'
        }
        failure {
            echo "Échec du build";

        }
    }

}