@Library('JenkinsLib_Shared') _

pipeline {

    // la déconnexion ssh ce faire a chaque stage automatiquement
    // l'authentification est au regsitry est conserver dans .docker/config.json

    agent any


    environment {
        // Définition des variables d'environnement
        CONTAINER_NAME = "configuration"
        DOMAIN_REGISTRY = "sonatype-nexus.backhole.ovh"
        USER_SESSION = "max"
        SSH_SERVER = "192.168.1.27"
        DOCKER_IMAGE_NAME = "registry-nas.backhole.ovh/config-service"

        DOCKERFILE_PATH = "./dockerfile"
        DOCKER_COMPOSE_FILE = "./docker/docker-compose-nas.yml"

        STATUS = ""
    }

    stages {

        stage('Load Environment Variables') {
            steps {
                script {

                    echo "Chargement du fichier .env";
                    def envFile = '.env'
                    sh("cat ${envFile}")    // Affiche le contenu du fichier .env pour déboguer
                    load(envFile)

                    echo "Version ms-configuration : ${version_config}";
                    echo "Version ms-eureka : ${version_eureka}";
                    echo "Version ms-gateway : ${version_gateway}";
                    echo "Version ms-article : ${version_article}";

                    echo("IP serveur : ${service_config_host_dev}")

                }
            }
        }

        stage("Git Module") {

            steps {
                script {
                    sh("git submodule init")
                    sh("git submodule update --recursive --remote")

                    // Listez les modules dans les sous-dossiers
                    def submodulesList = sh(script: "git submodule status --recursive", returnStdout: true).trim()

                    echo("Modules dans les sous-dossiers :")
                    echo("${submodulesList}")
                }
            }

        }


        stage('Deploy MS Configuration') {
            steps {
                script {
                    def image_name = "sonatype-nexus.backhole.ovh/ms-configuration-service"


                    // Credantials Preprod
                    sshagent(credentials: ['private-key-Preprod']) {

                        // Recherche de la version de l'image courant sur le serveur Preprod
                        def research_stack = sh("docker inspect stack_ms-configuration", returnStdout: true)

                        swarm.isSwarmStatus("stack_ms-configuration")

                        def commande_version = "docker inspect stack_ms-configuration | jq -r '.[0].Spec.TaskTemplate.ContainerSpec.Env[] | select(startswith(\"version=\")) | split(\"=\")[1]' "
                        def versionEnCours = sh(script: "ssh $USER_SESSION@$SSH_SERVER $commande_version ", returnStdout: true).trim()

                        // Si le service en cours possède la dernière version
                        //if ($versionEnCours == ${version_config}){
                        if (versionEnCours == "1.0.0") {

                            echo("le service Ms configuration est déjà en cours d'exécution avec la version $versionEnCours ")
                            currentBuild.result = 'ABORTED'
                            return
                        }

                        // la dernière version  de l'image ms configuration sur le serveur Preprod
                        def latest_version = sh(script: "ssh $USER_SESSION@$SSH_SERVER docker images --format '{{.Tag}}' $image_name | sort -V | tail -n 1", returnStdout: true).trim()
                        echo("La version de l'image Ms configuration sur le serveur : $latest_version")

                        // Si l'image sur le serveur est la dernière version
                        if (latest_version == "1.0.0") {
                            currentBuild.result = 'ABORTED'
                            return
                        }

                        // BUID image sur Devops
                        echo "Création de l'images : ms-configuration sur devops"
                        def stdout_build = sh(script: "docker compose -f ./dc-DEV-Build-swarm.yml build --no-cache ms-configuration", returnStdout: true)

                        echo("Status : $stdout_build")

                        // Vérifier que la version est présent sur DevOps
                        //def commande = "docker images --format \"{{.Repository}} {{.Tag}}\" | grep 'sonatype-nexus.backhole.ovh/ms-configuration-service 1.0.0'\n"
                        String commande = sh("docker images --format \"{{.Repository}} {{.Tag}}\" | grep $image_name ${version_config}", returnStdout: true)

                        if (!commande.isEmpty()) {

                            echo("L'image $image_name ${version_config} n'a pas etait trouvé sur le serveur DevOps")
                            currentBuild.result = 'ABORTED'
                            return
                        }


                        // Credentials for Docker registry
                        withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {

                            // connexion au dépôt depuis devOps
                            echo("\$PASSWORD | docker login -u \$USERNAME --password-stdin ${env.DOMAIN_REGISTRY}")
                            // Push vers le dépot depuis devOps
                            def status_push = sh("docker push $image_name:${version_config}", returnStdout: true)

                            // Ouverture de connection SSH
                            // Authentification au dépot
                            // Push images dans le dépôt nexus

                            echo("Ouverture de connexion SSH vers le serveur PREPROD")
                            sh """
                            ssh $USER_SESSION@$SSH_SERVER '
                                echo "Authentification au dépot de puis le serveur Preprod ${env.DOMAIN_REGISTRY}"
                                echo \$PASSWORD | docker login -u \$USERNAME --password-stdin ${env.DOMAIN_REGISTRY}
                                docker pull $image_name:${version_config}
                            '
                            """
                        }

                        // Soit => depuis preprod git clone ( si projet pas présent )
                        // Recherche du projet sur le serveur Preprod
                        def cmd_dossier = 'test -d /home/max/docker_home/ ms-configuration && echo "Le dossier existe" || echo "Le dossier n\'existe pas"'
                        def status = sh(script: "ssh $USER_SESSION@$SSH_SERVER $cmd_dossier ", returnStdout: true)

                        // depuis preprod : pull ou clone projet
                        if (status == 0) {
                            echo "Le dossier existe sur le serveur"
                            echo "Mise à jour de la stack"
                            sh """
                                ssh $USER_SESSION@$SSH_SERVER '
                                cd /home/max/docker_home &&
                                git pull origin main &&
                                docker service update --image $image_name:${version_config} stack_ms-configuration
                            '
                            """

                        } else {
                            echo "Le dossier n'existe pas sur le serveur."
                            echo "Clone du projet ms-configuration sur le serveur preprod"
                            sh """
                                ssh $USER_SESSION@$SSH_SERVER '
                                cd /home/max/docker_home &&
                                git clone git@github.com:MGNetworking/ms-configuration.git &&
                                docker stack deploy -c docker-compose-swarm.yml stack
                            '
                            """
                        }

                        def etat = "true"
                        def time = 0
                        while (etat) {

                            def status_service = "UNKNOWN"
                            echo("status du service ms-configuration : $status_service")

                            status_service = sh(script: 'curl -s --max-time 30 http://192.168.1.68:8089/actuator/health | jq -r \'.status\'', returnStdout: true).trim()

                            if (status_service == "UP") {
                                echo "Le service ms-configuration est : $status_service"
                                etat = "false"
                                currentBuild.result == 'SUCCESS'

                            } else if (status_service == "DOWN" || time == 10) {
                                echo "Le service ms-configuration est : $status_service "
                                echo "Nombre d'essais réaliser : $time "
                                currentBuild.result == 'FAILURE'
                                etat = "false"

                            } else {
                                echo "Le service ms-configuration est  $status_service : Attent de réponse du service en cours ..."
                                echo "Nombre d'essais réaliser : $time "
                            }

                            time++
                            sleep 300
                        }


                        if (currentBuild.result == 'ABORTED') {
                            echo("currentBuild valus : $currentBuild.result")
                            return
                        } else if (currentBuild.result == 'FAILURE') {
                            error("Une erreur est survenu pendant la constrution de la stack_ms-configuration")
                        }

                        // FIN du Stage

                    }
                }
            }
        }
    }

    post {
        always {
            script {
                sh "docker logout sonatype-nexus.backhole.ovh/ms-configuration-service"
            }
        }
        success {
            echo 'La mise en service à été réussi.'
            //                script {
            //                    STATUS = sh(script: "docker inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStdout: true)
            //                    echo "Status du conteneur ${CONTAINER_NAME} : ${STATUS} "
            //                    echo "docker logout ${DOMAIN_REGISTRY}"
            //                }
        }
        failure {

            echo "Échec de la mise en service, Rollback en cours";

            // Rollback
            //                sh "docker pull ${env.DOCKER_IMAGE_NAME}:${version_old}";
            //                sh 'docker compose -f ./docker/docker-compose-rollback.yml up -d --no-color --wait';
            //                sh "docker logout ${DOMAIN_REGISTRY}";
        }
    }

}