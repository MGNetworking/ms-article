@Library('JenkinsLib_Shared') _

// Configurations des serveurs
def remote
def nexus
def dockers
def VERSION_Docker
def LINE = "-----------------------------------------------------"
boolean STATUS_STACK = false

pipeline {
    agent any

    environment {
        SERVICE_CONFIG_URI = ""
        Nas_CREDS = credentials('NAS')
        Prod_CREDS = credentials('PROD')
        Nexus_CREDS = credentials('nexus-credentials')
        GITHUB_TOKEN = credentials('Github')
    }

    parameters {
        booleanParam(name: 'ALLOW_REBUILD', defaultValue: false, description: 'Autoriser le rebuild même si la version existe déjà')
        choice choices: ['beta', 'release'], description: 'selection du type de version', name: 'BUILD'
        string defaultValue: '', description: 'Entrez votre message de Publication', name: 'PUBLIC_MESSAGE'
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

                    echo "DEBUG - nexus: ${nexus}"
                    echo "DEBUG - nexus.user: ${nexus?.user}"
                    echo "DEBUG - nexus.pass: ${nexus?.pass}"
                    echo "DEBUG - nexus.domain: ${nexus?.domain}"

                    echo("Type de version sélectionner: ${params.BUILD}")
                    echo("Message de publication: ${params.PUBLIC_MESSAGE}")

                    // création du nom de version de l'image docker
                    if (params.BUILD == 'beta' || params.BUILD == 'release') {
                        VERSION_Docker = "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}-${params.BUILD}"
                        echo("Version docker projet : ${VERSION_Docker}")
                    } else {
                        error("Aucun version n'a était détecter en paramétre !!! ")
                    }

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
                    echo(LINE)
                    echo("Branche en cour ${env.BRANCH_NAME}")

                    // Les données dockers projet
                    dockers = utilsServeur.dockers(
                            "${VERSION_Docker}",                                        // img
                            '/volume1/docker/ms-article',                               // pathProjet
                            env.STACK_NAME                                              // stackName
                    )

                    // Les données de connection serveur
                    remote = utilsServeur.remote(
                            "${env.BRANCH_NAME}",   // name
                            '192.168.1.56',         // host
                            true,                   // allowAnyHosts
                            99,                     // port
                            Nas_CREDS_USR,          // user
                            Nas_CREDS_PSW           // password
                    )

                    echo("Version de l'application : ${dockers.img}")
                }
            }
        }

        stage('Load Environment Variables : prod') {
            when {
                expression {
                    return env.BRANCH_NAME == 'prod'
                }
            }
            steps {
                script {
                    echo(LINE)
                    echo "L'espace de travail: ${WORKSPACE}";
                    echo("Branche en cour ${env.BRANCH_NAME}")

                    // les données dockers projet
                    dockers = utilsServeur.dockers(
                            "${VERSION_Docker}",                                    // img
                            '/home/max/docker_home/ms-article',                     // pathProjet
                            "${env.STACK_NAME}")                                    // stackName

                    // les données de connection serveur
                    remote = utilsServeur.remote(
                            "${env.BRANCH_NAME}",   // name
                            '192.168.1.70',         // host
                            true,                   // allowAnyHosts
                            22,                     // port
                            Prod_CREDS_USR,         // user
                            Prod_CREDS_PSW)         // password

                    echo("Version de l'application : ${dockers.img}")
                }
            }
        }

        stage('Check version') {
            steps {
                script {
                    echo(LINE)
                    version = "${env.IMAGE_VERSION}-${params.BUILD}"  // La version recherché exemple: 1.0.25-release
                    version_beta = "${env.IMAGE_VERSION}-beta"        // Version de recherche
                    version_release = "${env.IMAGE_VERSION}-release"  // Version de recherche
                    path = "ms-article-service"                       // Référence au dossier projet nexus

                    def http_status_beta = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${path}/manifests/${version_beta}
                      """, returnStdout: true).trim()

                    def http_status_release = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${path}/manifests/${version_release}
                      """, returnStdout: true).trim()

                    echo("HTTP Status beta: $http_status_beta et HTTP Status release: $http_status_release")

                    // soit il y a un beta mes pas de release
                    if (version == version_beta && http_status_beta.equals("404")) {
                        echo("La version: ${version} n'existe pas dans le dépôt nexus donc le build peut être lancer !")

                    } else if (version == version_release && http_status_release.equals("404")) {
                        echo("La version: ${version} n'existe pas dans le dépôt nexus donc le build peut être lancer !")

                    } else if (http_status_beta.equals("404") || http_status_release.equals("404") || params.ALLOW_REBUILD) {
                        echo("La version: ${version} est absente ou le rebuild est autorisé. Le build peut être lancé.")
                    } else {
                        error("Une erreur inattendu est survenu pendant la recherche de la version du projet dans le dépôt nexus")
                    }

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


        stage("Open connection Nexus: Docker repository") {
            steps {
                script {
                    echo("Ouverture de la connection au dépôt nexus sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.loginDepot(this, nexus.user, nexus.pass, nexus.domain, remote)

                    echo("Ouverture de la connection au dépôt nexus depuis Jenkins")
                    utilsDocker.loginDepot(this, nexus.user, nexus.pass, nexus.domain)
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
                    echo("Compilation du service ms-article sous le profile Spring ${env.BRANCH_NAME}")
                    sh("mvn clean package -Dspring.profiles.active=${env.BRANCH_NAME} -DSERVICE_CONFIG_DOCKER=${SERVICE_CONFIG_URI}")

                }
            }
        }


        stage('Tests parallèles') {
            parallel {
                stage('UNITAIRE') {
                    agent {
                        docker {
                            image 'maven:3.8.5-jdk-8-slim'
                            args '-v /var/jenkins_home/maven/.m2:/root/.m2' +
                                    ' -v /var/run/docker.sock:/var/run/docker.sock'
                        }
                    }
                    steps {
                        script {
                            // Si les tests échouent, le pipeline est interrompu
                            echo("Lancement des tests unitaire")
                            sh("mvn test -Dspring.profiles.active=test -Dsurefire.report.directory=${WORKSPACE}/target/surefire-reports")
                            sh 'pwd'
                            sh 'ls -al target/surefire-reports || echo "surefire-reports non trouvé"'
                        }
                    }
                }
                stage('INTEGRATION') {
                    agent {
                        docker {
                            image 'maven:3.8.5-jdk-8-slim'
                            args '-v /var/jenkins_home/maven/.m2:/root/.m2' +
                                    ' -v /var/run/docker.sock:/var/run/docker.sock'
                        }
                    }
                    steps {
                        script {
                            // Si une erreur survient, le stage est marqué comme FAILURE, mais le pipeline continue
                            // Le résultat global du pipeline est marqué comme UNSTABLE
                            echo("Lancement des tests d'intégration et end to end")
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                sh("mvn verify -P integration -Dspring.profiles.active=test -Dfailsafe.report.directory=${WORKSPACE}/target/failsafe-reports")
                                sh 'pwd'
                                sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'
                            }
                        }
                    }
                }

                stage('END TO END') {
                    agent {
                        docker {
                            image 'maven:3.8.5-jdk-8-slim'
                            args '-v /var/jenkins_home/maven/.m2:/root/.m2' +
                                    ' -v /var/run/docker.sock:/var/run/docker.sock'
                        }
                    }
                    steps {
                        script {
                            // Si une erreur survient, le stage est marqué comme UNSTABLE,
                            // mais cela n’affecte pas le résultat global du pipeline.
                            echo("Lancement des tests d'intégration et end to end")
                            catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                                sh "mvn verify -P e2e -Dspring.profiles.active=test -Dfailsafe.report.directory=${WORKSPACE}/target/failsafe-reports"
                                sh 'pwd'
                                sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'
                            }
                        }
                    }
                }

            }
        }

        stage('Build Docker Image') {
            agent any
            steps {
                script {
                    echo("Création de l'image Docker : ${dockers.img}")
                    sh("docker compose build --no-cache")
                }
            }
        }

        stage('Tag / Push Docker Images dépôt Nexus') {
            agent any
            steps {
                script {
                    echo(LINE)
                    // TAG de l'image vers la version spécifier beta / relase
                    echo("Tag de l'image docker ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} vers ${dockers.img}")
                    sh(script: "docker tag ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} ${dockers.img}")
                    echo("push de l'image ${dockers.img} vers le dépôt Docker Nexus")
                    sh(script: "docker push ${dockers.img}")
                }
            }
        }

        stage('Pull du projet') {
            agent any
            steps {
                script {
                    echo(LINE)
                    echo("Mise à jours du projet ms-article sur le serveur ${env.BRANCH_NAME}")
                    utilsGit.gitPullSsh(this, remote, "cd ${dockers.pathProjet} " +
                            "&& git checkout ${env.BRANCH_NAME} " +
                            "&& git pull origin ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Pull Docker Images dépôt Nexus') {
            agent any
            steps {
                script {
                    echo(LINE)
                    echo("Pull de l'image docker: ${dockers.img} sur le serveur: ${env.BRANCH_NAME}")

                    if (!utilsDocker.pullImg(this, dockers.img, remote)) {
                        error("Une erreur est survenu pendant le pull de l'imges sur le serveur !")
                    }

                    echo("Affiche la liste des images Docker sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.getImg(this, dockers.img)


                }
            }
        }

        stage('Status Stack en cours') {
            agent any
            steps {
                script {
                    echo(LINE)
                    echo("Vérifi si la stack ${dockers.stackName} est deployer ou mettre à jours ")
                    STATUS_STACK = utilsDocker.statusStack(this, dockers.stackName, remote)
                    echo("La stack ${dockers.stackName} sera a " + (STATUS_STACK ? "mettre à jours" : "déployée") +
                            " sur le serveur ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Update / Deploy ms-article') {
            agent any
            steps {
                script {
                    echo(LINE)
                    echo("Deploiment sur le serveur: ${env.BRANCH_NAME}, en version: ${params.BUILD}")
                    String commande = "cd ${dockers.pathProjet} && " +
                            "export PROFILES=${env.BRANCH_NAME} && " +
                            "./script/deploy.sh ${params.BUILD}";
                    utilsDocker.deployStack(this, commande, remote)
                }
            }
        }


        stage('Vérification de disponibilité') {
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

                            echo("Liste des processus en cours sur stack : ${STACK_NAME}")
                            utilsDocker.getPsStack(this, NAME_SERVICE, remote)

                            echo("Docker log de la stack : ${STACK_NAME}")
                            utilsDocker.getServiceLogs(this, NAME_SERVICE, remote)

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

        stage('Publication du projet sur Github') {
            agent any
            steps {
                script {
                    echo(LINE)

                    def TAGE_NAME = "${env.IMAGE_VERSION}-${params.BUILD}"
                    def REPO_NAME = "MGNetworking/ms-article"

                    try {
                        if (params.BUILD == 'beta') {
                            utilsGit.createOrUpdatePreRelease(this, TAGE_NAME, REPO_NAME, GITHUB_TOKEN, params.PUBLIC_MESSAGE)
                        } else if (params.BUILD == 'release') {
                            utilsGit.createOrUpdateRelease(this, TAGE_NAME, REPO_NAME, GITHUB_TOKEN, params.PUBLIC_MESSAGE)
                        } else {
                            error("Les paramètres de la version son manquantes. Il ne peux y avoir une publication vers le dépôt !" +
                                    " version: ${TAGE_NAME} , message de publication: ${params.PUBLIC_MESSAGE}")
                        }

                    } catch (Exception execp) {
                        error("Une erreur est survenu pendant le processus de création de publication de la version ${TAGE_NAME} " +
                                ", message: ${execp}")
                    }

                }
            }
        }
    }


    post {
        always {
            script {
                echo(LINE)
                try {
                    echo("Déconnection au dépôt nexus docker entre le serveur ${env.BRANCH_NAME} et le dépôt nexus")
                    utilsDocker.logoutDepot(this, nexus.domain, remote)

                    echo("Fermeture de la connection au dépôt nexus depuis Jenkins")
                    utilsDocker.logoutDepot(this, nexus.domain)

                    echo("Nettoyage de l'images de base : ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}")
                    utilsDocker.rmi(this, "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}")

                    echo("Nettoyage de l'images beta / relase : ${dockers.img}")
                    utilsDocker.rmi(this, dockers.img)

                    sh 'pwd'
                    sh 'ls -al target/surefire-reports || echo "surefire-reports non trouvé"'
                    sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'

                    echo "Collecte des rapports JUnit pour les tests unitaires."
                    junit testResults: "${WORKSPACE}/target/surefire-reports/*.xml", allowEmptyResults: true

                    echo "Collecte des rapports JUnit pour les tests d'intégration et E2E."
                    junit testResults: "${WORKSPACE}/target/failsafe-reports/*.xml", allowEmptyResults: true

                    def testResults = sh(script: "grep -H '<testsuite' ${WORKSPACE}/target/surefire-reports/*.xml || echo 'Aucun résultat trouvé'", returnStdout: true).trim()
                    if (testResults) {
                        echo "Résumé des résultats des tests :"
                        echo testResults
                    } else {
                        echo "Aucun résultat de test trouvé dans target/surefire-reports."
                    }

                } catch (Exception e) {
                    echo("Une erreur est survenu dans la parti POST always, message : ${e.message}")
                }
            }
        }
        success {
            script {
                echo(LINE)
                def stack = "du déploiement de la stack avec la version du service ${env.IMAGE_VERSION}"
                def service = "de la mise à jour du service a la version ${env.IMAGE_VERSION}"
                echo('Fin ' + (STATUS_STACK ? stack : service))
            }
        }
        failure {
            script {
                echo(LINE)
                echo("Échec du déploiement. Effectuer un rollback.")

                if (!STATUS_STACK) {
                    echo("Échec du déploiement de la stack ${dockers.stackName}")
                    echo("Suppression de la stack ${dockers.stackName} sur le serveur distant")
                    utilsDocker.rmStack(this, dockers.stackName, remote)
                    echo("Sortie Delete stack: ${deleteStack}")
                } else {
                    echo("Échec de la mise à jour de la stack ${dockers.stackName}")
                    echo("ROLLBACK de la stack ${dockers.stackName}")
                    utilsDocker.rollbackService(this, NAME_SERVICE, false)
                }
                def time = 15
                echo "Suppression de l'image en échec ${dockers.img} sur le serveur dans ${time} secondes ..."
                sleep time: time, unit: 'SECONDS'
                utilsDocker.rmi(this, dockers.img, remote)
            }
        }
    }

}