@Library('JenkinsLib_Shared') _

// Configurations des serveurs
def remote
def nexus
def dockers
boolean STATUS_STACK = false

pipeline {
    agent any

    environment {
        SERVICE_CONFIG_URI = ""
        // Get credentials to connection serveur
        Nas_CREDS = credentials('NAS')
        Prod_CREDS = credentials('PROD')
        Nexus_CREDS = credentials('nexus-credentials')
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

                        // Ignorer les lignes de commentaire
                        if (!line.startsWith('#')) {

                            // Diviser la ligne en clé et valeur
                            def (key, value) = line.split('=').collect { it.trim() }

                            // Définir la variable d'environnement dans le contexte du pipeline
                            env."${key.trim()}" = value.trim()
                        }
                    }

                    // Afficher les variables d'environnement pour le débogage
                    sh 'printenv'

                    // les données de connection au dépôt nexus
                    nexus = utilsServeur.credentials(
                            Nexus_CREDS_USR,
                            Nexus_CREDS_PSW,
                            'sonatype-nexus.backhole.ovh')

                }
            }
        }


        stage('Load Environment Variables : nas') {
            when {
                expression {
                    return env.BRANCH_NAME == 'nas'
                }
            }
            steps {
                script {
                    echo("Branche en cour ${env.BRANCH_NAME}")

                    // les données dockers projet
                    dockers = utilsServeur.dockers(
                            "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}",
                            '/usr/local/bin',
                            '/volume1/docker/ms-article',
                            "${env.STACK_NAME}")

                    // les données de connection serveur
                    remote = utilsServeur.remote(
                            "${env.BRANCH_NAME}",
                            '192.168.1.56',
                            true,
                            99,
                            Nas_CREDS_USR,
                            Nas_CREDS_PSW)
                }
            }
        }

//        stage('Load Environment Variables : preprod') {
//            when {
//                expression {
//                    return env.BRANCH_NAME == 'preprod'
//                }
//            }
//            steps {
//                script {
//                    echo "L'espace de travail : ${WORKSPACE}";
//                    echo("Branche en cour ${env.BRANCH_NAME}")
//
//                    // les données dockers projet
//                    dockers = utilsServeur.dockers(
//                            "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}",
//                            '/usr/bin',
//                            '/home/max/docker_home/ms-article',
//                            "${env.STACK_NAME}")
//
//                    // les données de connection serveur
//                    remote = utilsServeur.remote(env.BRANCH_NAME,
//                            '192.168.1.',
//                            true,
//                            22,
//                            Preprod_CREDS_USR,
//                            Preprod_CREDS_PSW)
//
//                }
//            }
//        }

        stage('Load Environment Variables : prod') {
            when {
                expression {
                    return env.BRANCH_NAME == 'prod'
                }
            }
            steps {
                script {
                    echo "L'espace de travail : ${WORKSPACE}";
                    echo("Branche en cour ${env.BRANCH_NAME}")

                    // les données dockers projet
                    dockers = utilsServeur.dockers(
                            "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}",
                            '/usr/bin',
                            '/home/max/docker_home/ms-article',
                            "${env.STACK_NAME}")

                    // les données de connection serveur
                    remote = utilsServeur.remote(
                            "${env.BRANCH_NAME}",
                            '192.168.1.70',
                            true,
                            22,
                            Prod_CREDS_USR,
                            Prod_CREDS_PSW)

                }
            }
        }

        stage("Test : service ms-configuration") {
            steps {
                script {

                    echo("Vérifie que le service ms-configuration fonctionne " +
                            "correctement sur le serveur ${env.BRANCH_NAME}")

                    echo "Initilaisation de l'adresse du service ms-configuration";
                    SERVICE_CONFIG_URI = "http://${remote.host}:8089"
                    status = false

                    for (int index = 0; index < 10; index++) {

                        echo("Requet CURL n° $index du service : ms-configuration a l'adresse : " +
                                "${SERVICE_CONFIG_URI}/actuator/health ")

                        String result = sh(script: "curl -s ${SERVICE_CONFIG_URI}/actuator/health | " +
                                "jq -r '.status'", returnStdout: true, returnStatus: false)

                        if (result.contains("UP")) {
                            echo("Le service ms-configuration est bien cours d'exécution, sorti: $result")
                            status = "SUCCESS"
                            break
                        } else {
                            echo("Le service ms-configuration n'est pas cours d'exécution, sorti: $result")
                            echo "Tentative n° $index"
                            sleep time: 5, unit: 'SECONDS'
                        }
                    }

                    if (status != "SUCCESS") {
                        error("Le service ms-configuration n'est pas actif !!!")
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
                    echo("Compilation du service ms-article")
                    sh("mvn clean package -Dspring.profiles.active=${env.BRANCH_NAME} " +
                            "-DIP=192.168.1.56 " +
                            "-DSERVICE_CONFIG_DOCKER=${SERVICE_CONFIG_URI}")
                }
            }
        }

        stage('Build Docker compose ') {
            agent any
            steps {
                script {
                    echo("Création de l'image Docker : ${dockers.img}")
                    sh("docker compose build --no-cache")
                }
            }
        }

        stage("docker / Nexus Open connection") {
            steps {
                script {
                    echo("Ouverture de connection au depot nexus sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.loginDepot(this, remote,
                            "${dockers.binDocker}/docker login -u ${nexus.user} -p ${nexus.pass} ${nexus.domain}")
                }
            }
        }

        stage('Push image dépôt') {
            agent any
            steps {
                script {
                    echo("push de l'image ${dockers.img} vers le dépôt")
                    docker.image("${dockers.img}").push()
                }
            }
        }

        stage('Update / Deploy ms-storage') {
            agent any
            steps {
                script {

                    echo("Mise à jours du projet ms-article sur le serveur ${env.BRANCH_NAME}")
                    utilsGit.gitPull(this, remote, "cd ${dockers.pathProjet} " +
                            "&& git checkout ${env.BRANCH_NAME} " +
                            "&& git pull origin ${env.BRANCH_NAME}")

                    echo("Pull image ${dockers.img}")
                    utilsDocker.pullCommande(this, remote, "${dockers.binDocker}/docker pull ${dockers.img}")

                    echo("Vérifi si la stack ${dockers.stackName} est deployer ou mettre à jours ")
                    STATUS_STACK = utilsSwarm.statusStackRemote(this, remote,
                            "${dockers.binDocker}/docker stack ls | grep ${dockers.stackName}")

                    echo("La stack ${dockers.stackName} sera a " + (STATUS_STACK ? "mettre à jours" : "déployée") +
                            " sur le serveur ${env.BRANCH_NAME}")

                    utilsSwarm.updateDeployStackCommande(this, remote,
                            "cd ${dockers.pathProjet} && export PROFILES=${env.BRANCH_NAME} && ./script/deploy.sh")
                }
            }
        }


        stage('Tests de Validation Post-Déploiement') {
            agent any
            steps {
                script {
                    status = false
                    for (int index = 0; index < 10; index++) {

                        echo("Requet CURL n° ${index} du service : ${NAME_SERVICE}")
                        echo("à l'adresse : http://${remote.host}:${PORT}/actuator/health ")

                        String result = sh(script: "curl -s http://${remote.host}:${PORT}/actuator/health | " +
                                "jq -r '.status'", returnStdout: true, returnStatus: false)

                        if (result.contains("UP")) {
                            echo("sorti : ${result}")
                            echo("La mise en service de ${NAME_SERVICE} à été réalisé avec Succès ")
                            status = "SUCCESS"
                            break
                        } else {
                            echo("sorti : ${result}")
                            echo "Le service n'est pas encore UP. Attente de 15 secondes..."
                            echo "Tentative n° $index"
                            sleep time: 15, unit: 'SECONDS'
                        }
                    }
                    if (status != "SUCCESS") {
                        error("Le service ${NAME_SERVICE} est en echec !!!")
                    }
                }
            }
        }
    }


    post {
        always {
            script {
                echo("Déconnection au dépôt nexus docker entre le serveur ${env.BRANCH_NAME} et le dépôt nexus")
                utilsDocker.logoutDepot(this, remote, "${dockers.binDocker}/docker logout ${nexus.domain}")

                echo("nettoyage de l'images créer : ${dockers.img} ")
                utilsDocker.clsImageLocal(this, "docker rmi ${dockers.img}")
            }
        }
        success {
            script {
                echo "Fin " + (STATUS_STACK ? "du déployement de la stack " : "de la mise à jour de la stack ")
            }
        }
        failure {
            script {

                echo('Échec')

                // Si deploiment en echec
                if (!STATUS_STACK) {
                    echo("Échec du déploiement de la stack ${dockers.stackName}")
                    echo("Delete stack ${dockers.stackName} du serveur distant ")

                    String deleteStack = sshCommand remote: remote,
                            command: "${dockers.binDocker}/docker stack rm ${dockers.stackName}"

                    echo("Sorti delete : ${deleteStack}")

                    // Si mise à jour en echec
                } else {
                    echo("Échec de la mise a jours de la stack ${dockers.stackName}")
                    echo("ROLLBACK de la stack ${dockers.stackName}")

                    String rollbackResult = sshCommand remote: remote,
                            command: "docker service rollback ${NAME_SERVICE}"

                    echo("Sorti ROLLBACK : ${rollbackResult}")
                }

                echo "Suppression de l'image en échec ${dockers.img} sur le serveur dans 15 secondes..."
                sleep time: 15, unit: 'SECONDS'
                utilsDocker.clsImageRemote(this, remote,
                        "${dockers.binDocker}/docker rmi ${dockers.img}")
            }
        }
    }

}