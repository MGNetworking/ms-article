@Library('JenkinsLib_Shared') _

// Configurations des serveurs
def remote

pipeline {
    agent any

    environment {

        DOMAIN_REGISTRY = "sonatype-nexus.backhole.ovh"
        DEPLOY = false

        // Get credentials to connection serveur
        Preprod_CREDS = credentials('PREPROD')

    }

    stages {

        stage('Load Environment Variables') {
            steps {
                script {
                    echo "L'espace de travail : ${WORKSPACE}";

                    // lecture du fichier
                    def envContent = readFile(".env").trim()

                    // Séparer le contenu en lignes et traiter chaque ligne
                    envContent.readLines().each { line ->
                        // Diviser la ligne en clé et valeur
                        def (key, value) = line.split('=').collect { it.trim() }

                        // Définir la variable d'environnement dans le contexte du pipeline
                        env."${key.trim()}" = value.trim()
                    }

                    // Afficher les variables d'environnement pour le débogage
                    env.each { key, value ->
                        echo "${key}=${value}"
                    }

                    echo "Nouvelle version de l'application : ${IMAGE_VERSION}";
                    echo "service config host preprod : ${service_config_host_pre}";

                    sh '''
                    docker  version
                    docker info
                    docker compose version
                    '''

                }
            }
        }

        stage("Open connection") {
            steps {
                script {
                    // definition config serveur
                    remote = configurerServeur.config('Preprod', '192.168.1.27', true)
                    remote.user = env.Preprod_CREDS_USR
                    remote.password = env.Preprod_CREDS_PSW

                    // Ouverture connection au depot nexus sur Preprod
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {

                        String loginResult = sshCommand remote: remote, command: "docker login -u $USERNAME -p $PASSWORD $DOMAIN_REGISTRY"

                        loginResult.contains("status: 401 Unauthorized") ? error("Erreur de connection status: 401 Unauthorized") :
                                echo("Connection au dépôt depuis le serveur réussi : Login Succeeded")
                    }

                    // Pull projet sur branch preprod
                    String commande = sshCommand remote: remote, command: "cd /home/max/docker_home/ms-article &&  git checkout preprod && git pull origin preprod"
                    echo("sorti : $commande")
                }
            }
        }

        // Détermine Update ou deploy
        stage("Status stack : article") {
            steps {
                script {
                    String result = ""
                    try {
                        result = sshCommand remote: remote, failOnError: false, sudo: false, command: "docker stack ls | grep $NAME_SERVICE"

                        result.contains($NAME_SERVICE) ? DEPLOY = true : false
                        echo "La stack $NAME_SERVICE est " + (DEPLOY ? "déployée" : "non déployée") + " sur le serveur"
                    } catch (Exception e) {
                        echo("La Stack $NAME_SERVICE n'a pas etait trouver !!!")
                    }

                }
            }
        }


        stage('Maven Compilation') {
            agent {
                docker {
                    image 'maven:3.8.5-jdk-8-slim'
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

        stage('Build Docker compose ') {
            agent any
            steps {
                script {
                    sh '''
                        ls -al target/
                        docker compose build --no-cache
                    '''
                }
            }
        }

        stage('Push image dépôt') {
            agent any
            steps {
                script {

                    def pushResult = docker.image("$env.DOCKER_IMAGE_NAME:$env.IMAGE_VERSION").push()
                    echo("pushResult : $pushResult")
                    // Vérifier si le push a réussi
//                    if (pushResult) {
//                        echo "Le push de l'image a été réalisé avec succès."
//                    } else {
//                        error "Erreur lors du push de l'image."
//                    }


                }
            }
        }

        // Deploy if not exist
        stage('Deploy ms-article') {
            agent any
            when {
                expression { return DEPLOY  }
            }
            steps {
                script {

                    // pull depuis preprod
                    String pullResult = sshCommand remote: remote, command: "docker pull $env.DOCKER_IMAGE_NAME:$env.IMAGE_VERSION"
                    echo("Sorti pullResult : $pullResult")

                    pullResult.contains("Status: Downloaded newer image") ?
                            echo("Le pull de l'image a été réalisé avec succès.") :
                            error("Erreur lors du pull de l'image.")
                    try {
                        def deployResult = sshCommand remote: remote, command: "cd /home/max/docker_home/ms-article && export \$(cat .env) && docker stack deploy -c ./docker-compose-swarm.yml $env.STACK_NAME"
                        echo("Sorti deployResult : $deployResult")
                    }catch (Exception e){
                        error("$e.getMessage()")
                    }

                }

            }
        }

        // Update if exist
        stage('Update ms-article') {
            agent any
            when {
                expression { return DEPLOY  }
            }
            steps {
                script {
                    // Pull image in preprod and update with image
                    def deployResult = sshCommand remote: remote, command: "docker pull $env.DOCKER_IMAGE_NAME:$env.IMAGE_VERSION && " +
                            "docker service update --image $env.DOCKER_IMAGE_NAME:$env.IMAGE_VERSION $NAME_SERVICE"

                }
            }
        }

        stage('Test du service ') {
            agent any
            when {
                expression { return DEPLOY }
            }
            steps {
                script {

                    for (int i = 0; i < 5; i++) {
                        curl - s
                        String network = sh(script: "$service_config_host_pre/actuator/health", returnStdout: true).trim()

                        if (network.contains("UP")) {
                            echo("Le service : $network")
                            currentResult = "SUCCESS"
                            break
                        }

                        sleep time: 30, unit: 'SECONDS'
                    }

                    if (currentResult.equals("SUCCESS")) {
                        echo("La mise en service de $NAME_SERVICE à été réalisé avec Succès ")
                        return
                    } else {
                        currentResult = "FAILURE"
                        error("Le service $NAME_SERVICE est en echec !!!")
                    }


                }
            }
        }

    }


    post {
        always {
            script {
                echo "Fin de " + (DEPLOY ? "La mise en service " : "la mise en service")

            }
        }
        success {

            script {
                echo('Réussite du build')
                String loginResult = sshCommand remote: remote, command: "docker logout $DOMAIN_REGISTRY"
                if (loginResult.contains("Removing login credentials")) {
                    echo "La deconnection au dépôt depuis le serveur réussi"
                } else {
                    error("Echec de la deconnexion au dépot depuis le serveur Preprod")
                }
            }


        }
        failure {
            script {

                echo "Échec du build ";

                if (currentResult == "FAILURE"){
                    // le rollback
                    String rollbackResult = sshCommand remote: remote, command: "docker service rollback $NAME_SERVICE"
                    echo("Rollback : $rollbackResult")

                    // Logout du depot sur preprod
                    String loginResult = sshCommand remote: remote, command: "docker logout $DOMAIN_REGISTRY"
                    if (loginResult.contains("Removing login credentials")) {
                        echo "La deconnection au dépôt depuis le serveur réussi"
                    } else {
                        error("Echec de la deconnexion au dépot depuis le serveur Preprod")
                    }
                }


            }


        }
    }

}