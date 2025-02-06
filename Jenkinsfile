@Library('JenkinsLib_Shared') _

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

pipeline {
    agent any

    environment {
        def LINE = "-----------------------------------------------------"
        Nas_CREDS = credentials('NAS')
        Prod_CREDS = credentials('PROD')
        Nexus_CREDS = credentials('nexus-credentials')
    }

    parameters {
        booleanParam(name: 'BETA', defaultValue: false, description: 'Par défaut la version sera beta')
        string defaultValue: '', description: 'Entrez votre message de Publication', name: 'PUBLIC_MESSAGE'
    }

    stages {

        stage('Load Environment Variables') {
            steps {
                script {
                    echo "L'espace de travail : ${WORKSPACE}";
                    echo("Branche en cours : ${env.BRANCH_NAME}")

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
                    env.NEXUS = JsonOutput.toJson(utilsServeur.credentials(
                            Nexus_CREDS_USR,
                            Nexus_CREDS_PSW,
                            'sonatype-nexus.backhole.ovh'))

                    echo "DEBUG: env.NEXUS après affectation = ${env.NEXUS}"
                    echo("Type de version sélectionner: ${params.BUILD}")
                    echo("Message de publication: ${params.PUBLIC_MESSAGE}")

                    // Type 2.0.0-beta
                    env.IMAGE_TAG = "${env.IMAGE_VERSION}-${params.BETA ? 'beta' : 'release'}"

                    // Type sonatype-nexus.backhole.ovh/ms-article-service:2.0.0-beta
                    env.IMAGE_NAME = "${env.DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                    echo("Nom de l'image docker : ${IMAGE_NAME}")

                    if (!env.IMAGE_TAG?.trim()) {
                        error("La version de l'image est obligatoire ...")
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
                    env.DOCKER = JsonOutput.toJson(utilsServeur.dockers(
                            env.IMAGE_NAME,                // img
                            '/volume1/docker/ms-article',  // pathProjet
                            env.STACK_NAME                 // stackName
                    ))

                    // Les données de connection serveur
                    env.REMOTE = JsonOutput.toJson(utilsServeur.remote(
                            env.BRANCH_NAME,        // name
                            '192.168.1.56',         // host
                            true,                   // allowAnyHosts
                            99,                     // port
                            Nas_CREDS_USR,          // user
                            Nas_CREDS_PSW           // password
                    ))

                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)

                    echo "DEBUG: env.DOCKER après affectation = ${env.DOCKER}"
                    echo "DEBUG: env.REMOTE après affectation = ${env.REMOTE}"
                    echo("Version de l'images docker : ${dk.img}")
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

                    // les données dockers projet
                    env.DOCKER = JsonOutput.toJson(utilsServeur.dockers(
                            env.IMAGE_NAME,                      // img
                            '/home/max/docker_home/ms-article',  // pathProjet
                            env.STACK_NAME))                     // stackName

                    env.REMOTE = JsonOutput.toJson(utilsServeur.remote(
                            env.BRANCH_NAME,        // name
                            '192.168.1.70',         // host
                            true,                   // allowAnyHosts
                            22,                     // port
                            Prod_CREDS_USR,         // user
                            Prod_CREDS_PSW))        // password

                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)

                    echo "DEBUG: env.DOCKER après affectation = ${env.DOCKER}"
                    echo "DEBUG: env.REMOTE après affectation = ${env.REMOTE}"
                    echo("Version de l'images docker : ${dk.img}")
                }
            }
        }

        stage('Check version') {
            steps {
                script {
                    echo(LINE)
                    // déséralisation des données
                    def nexus = new JsonSlurper().parseText(env.NEXUS)

                    // La version recherché exemple: 1.0.25-release
                    version_beta = "${env.IMAGE_VERSION}-beta"        // Version beta de recherche
                    version_release = "${env.IMAGE_VERSION}-release"  // Version release de recherche

                    def http_status_beta = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${env.PATH_NEXUS}/manifests/${version_beta}
                      """, returnStdout: true).trim()

                    def http_status_release = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${env.PATH_NEXUS}/manifests/${version_release}
                      """, returnStdout: true).trim()

                    echo("HTTP Status beta: $http_status_beta et HTTP Status release: $http_status_release")
                    def version_exists = (http_status_beta == "200" || http_status_release == "200")

                    // check des versions sur le serveur Nexus
                    if (env.IMAGE_TAG == version_beta && http_status_beta.equals("404")) {
                        echo("La version: ${version_beta} n'existe pas dans le dépôt nexus")
                        echo("donc le build peut être lancer !")
                        env.SKIP_BUILD = true

                    } else if (env.IMAGE_TAG == version_release && http_status_release.equals("404")) {
                        echo("La version: ${version_release} n'existe pas dans le dépôt nexus")
                        echo("donc le build peut être lancer !")
                        env.SKIP_BUILD = true

                    } else if (version_exists) {
                        echo("La version: ${env.IMAGE_TAG} est déjà présente sur le serveur Nexus")
                        echo("Excécution des test unitaire uniquement !")
                        env.SKIP_BUILD = false

                    } else {
                        error("Une erreur inattendu est survenu pendant la recherche de la version du " +
                                "projet dans le dépôt nexus")
                    }

                }
            }
        }

        stage("Test : service ms-configuration") {
            steps {
                script {

                    // déséralisation des données
                    def remote = new JsonSlurper().parseText(env.REMOTE)

                    echo("Vérifie que le service ms-configuration fonctionne " +
                            "correctement sur le serveur ${env.BRANCH_NAME}")

                    echo "Initilaisation de l'adresse du service ms-configuration";
                    env.SERVICE_CONFIG_URI = "http://${remote.host}:8089"
                    status = false

                    for (int index = 0; index < 10; index++) {

                        echo("Requet CURL n° $index du service : ms-configuration a l'adresse : " +
                                "${env.SERVICE_CONFIG_URI}/actuator/health ")

                        String result = sh(script: "curl -s ${env.SERVICE_CONFIG_URI}/actuator/health | " +
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
                    // déséralisation des données
                    def nexus = new JsonSlurper().parseText(env.NEXUS)

                    echo("Ouverture de la connection au dépôt nexus sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.loginDepot(this, nexus.user, nexus.pass, nexus.domain, REMOTE)

                    echo("Ouverture de la connection au dépôt nexus depuis Jenkins")
                    utilsDocker.loginDepot(this, nexus.user, nexus.pass, nexus.domain)
                }
            }
        }

        stage('Maven Compilation') {
            when {
                expression { !env.SKIP_BUILD }
            }
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
                    sh("mvn clean package -Dspring.profiles.active=${env.BRANCH_NAME} " +
                            "-DSERVICE_CONFIG_DOCKER=${env.SERVICE_CONFIG_URI}")

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
                            sh("mvn test -Dspring.profiles.active=test " +
                                    "-Dsurefire.report.directory=${WORKSPACE}/target/surefire-reports")
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
                                sh("mvn verify -P integration -Dspring.profiles.active=test " +
                                        "-Dfailsafe.report.directory=${WORKSPACE}/target/failsafe-reports")
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
                                sh "mvn verify -P e2e -Dspring.profiles.active=test " +
                                        "-Dfailsafe.report.directory=${WORKSPACE}/target/failsafe-reports"
                                sh 'pwd'
                                sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'
                            }
                        }
                    }
                }

            }
        }

        stage('Build Docker Image') {
            when {
                expression { !env.SKIP_BUILD }
            }
            agent any
            steps {
                script {
                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)
                    echo("Création de l'image Docker : ${dk.img}")
                    sh("docker compose build --no-cache")
                }
            }
        }

        stage('Tag / Push Docker Images dépôt Nexus') {
            when {
                expression { env.SKIP_BUILD }
            }
            agent any
            steps {
                script {
                    echo(LINE)
                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)

                    // TAG de l'image vers la version spécifier beta / relase
                    echo("Tag de l'image docker ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} vers ${dk.img}")
                    sh(script: "docker tag ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} ${dk.img}")
                    echo("push de l'image ${dk.img} vers le dépôt Docker Nexus")
                    sh(script: "docker push ${dk.img}")
                }
            }
        }

        stage('Pull du projet') {
            agent any
            steps {
                script {
                    echo(LINE)
                    echo("Mise à jours du projet ms-article sur le serveur ${env.BRANCH_NAME}")
                    utilsGit.gitPullSsh(this, new JsonSlurper().parseText(env.REMOTE),
                            "cd ${DOCKER.pathProjet} " +
                                    "&& git checkout ${env.BRANCH_NAME} " +
                                    "&& git pull origin ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Pull Docker Images dépôt Nexus') {
            when {
                expression { !env.SKIP_BUILD }
            }
            agent any
            steps {
                script {
                    echo(LINE)
                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)
                    def remote = new JsonSlurper().parseText(env.REMOTE)

                    echo("Pull de l'image docker: ${dk.img} sur le serveur: ${env.BRANCH_NAME}")

                    if (!utilsDocker.pullImg(this, dk.img, remote)) {
                        error("Une erreur est survenu pendant le pull de l'imges sur le serveur !")
                    }

                    echo("Affiche la liste des images Docker sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.getImg(this, dk.img)


                }
            }
        }

        stage('Status Stack en cours') {
            agent any
            steps {
                script {
                    echo(LINE)
                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)
                    def remote = new JsonSlurper().parseText(env.REMOTE)

                    echo("Vérifi si la stack ${dk.stackName} est deployer ou mettre à jours ")

                    env.STATUS_STACK = utilsDocker.statusStack(this, dk.stackName, remote)
                    echo("La stack ${dk.stackName} sera a " + (env.STATUS_STACK ? "mettre à jours" : "déployée") +
                            " sur le serveur ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Update / Deploy ms-article') {
            agent any
            steps {
                script {
                    echo(LINE)

                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)
                    def remote = new JsonSlurper().parseText(env.REMOTE)

                    echo("Deploiment sur le serveur: ${env.BRANCH_NAME}, en version: ${params.BUILD}")
                    String commande = "cd ${dk.pathProjet} && " +
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
                    status = true
                    // déséralisation des données
                    def dk = new JsonSlurper().parseText(env.DOCKER)
                    def remote = new JsonSlurper().parseText(env.REMOTE)

                    for (int index = 0; index < 10; index++) {

                        echo("Requet CURL n° ${index} du service : ${env.NAME_SERVICE}")
                        echo("à l'adresse : http://${remote.host}:${PORT}/actuator/health ")

                        String result = sh(script: "curl -s http://${remote.host}:${PORT}/actuator/health | " +
                                "jq -r '.status'", returnStdout: true, returnStatus: false)

                        if (result.contains("UP")) {
                            echo("sorti : ${result}")
                            echo("La mise en service de ${env.NAME_SERVICE} à été réalisé avec Succès ")
                            status = false

                            echo("Liste des processus en cours sur stack : ${env.STACK_NAME}")
                            utilsDocker.getPsStack(this, env.NAME_SERVICE, remote)

                            echo("Docker log de la stack : ${STACK_NAME}")
                            utilsDocker.getServiceLogs(this, env.NAME_SERVICE, remote)

                            break
                        } else {
                            echo("sorti : ${result}")
                            echo "Le service n'est pas encore UP. Attente de 15 secondes..."
                            echo "Tentative n° $index"
                            sleep time: 15, unit: 'SECONDS'
                        }
                    }
                    if (status) {
                        error("Le service ${NAME_SERVICE} est en echec !!!")
                    }
                }
            }
        }

        stage('Publication du projet sur Github') {
            when {
                expression { !env.SKIP_BUILD }
            }
            agent any
            steps {
                script {
                    echo(LINE)
                    try {
                        if (params.BUILD == 'beta') {

                            utilsGit.createOrUpdatePreRelease(
                                    this,
                                    env.IMAGE_TAG,
                                    env.REPO_NAME,
                                    credentials('Github'),
                                    params.PUBLIC_MESSAGE)

                        } else if (params.BUILD == 'release') {

                            utilsGit.createOrUpdateRelease(
                                    this,
                                    env.IMAGE_TAG,
                                    env.REPO_NAME,
                                    credentials('Github'),
                                    params.PUBLIC_MESSAGE)
                        } else {
                            error("Les paramètres de la version son manquantes. Il ne peux y avoir " +
                                    "une publication vers le dépôt ! version: ${env.IMAGE_TAG} , " +
                                    "message de publication: ${params.PUBLIC_MESSAGE}")
                        }

                    } catch (Exception ex) {
                        error("Une erreur est survenu pendant le processus de création de publication " +
                                "de la version ${env.IMAGE_TAG} , message: ${ex}")
                    }

                }
            }
        }
    }

    post {
        always {
            script {
                echo(LINE)
                // déséralisation des données
                def dk = new JsonSlurper().parseText(env.DOCKER)
                def remote = new JsonSlurper().parseText(env.REMOTE)
                def nexus = new JsonSlurper().parseText(env.NEXUS)

                try {
                    echo("Déconnection du dépôt entre le serveur ${env.BRANCH_NAME} et le dépôt nexus")
                    utilsDocker.logoutDepot(this, nexus.domain, remote)

                    echo("Fermeture de la connection au dépôt nexus depuis Jenkins")
                    utilsDocker.logoutDepot(this, nexus.domain)

                    echo("Nettoyage de l'images de base : ${env.IMAGE_NAME}")
                    utilsDocker.rmi(this, env.IMAGE_NAME)

                    echo("Nettoyage de l'images beta / relase : ${dk.img}")
                    utilsDocker.rmi(this, dk.img)

                    sh 'pwd'
                    sh 'ls -al target/surefire-reports || echo "surefire-reports non trouvé"'
                    sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'

                    echo "Collecte des rapports JUnit pour les tests unitaires."
                    junit testResults: "target/surefire-reports/*.xml", allowEmptyResults: true

                    echo "Collecte des rapports JUnit pour les tests d'intégration et E2E."
                    junit testResults: "target/failsafe-reports/*.xml", allowEmptyResults: true

                    def testResults = sh(script: "grep -H '<testsuite' ${WORKSPACE}/target/surefire-reports/*.xml " +
                            "|| echo 'Aucun résultat trouvé'", returnStdout: true).trim()
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
                echo('Fin ' + (env.STATUS_STACK ? stack : service))
            }
        }
        failure {
            script {
                echo(LINE)
                // déséralisation des données
                def dk = new JsonSlurper().parseText(env.DOCKER)
                def remote = new JsonSlurper().parseText(env.REMOTE)
                def nexus = new JsonSlurper().parseText(env.NEXUS)

                echo("Échec du déploiement. Effectuer un rollback.")

                if (!env.STATUS_STACK) {
                    echo("Échec du déploiement de la stack ${dk.stackName}")
                    echo("Suppression de la stack ${dk.stackName} sur le serveur distant")
                    utilsDocker.rmStack(this, dk.stackName, remote)
                } else {
                    echo("Échec de la mise à jour de la stack ${dk.stackName}")
                    echo("ROLLBACK de la stack ${dk.stackName}")
                    utilsDocker.rollbackService(this, env.NAME_SERVICE, false)
                }
                def time = 15
                echo "Suppression de l'image en échec ${dk.img} sur le serveur dans ${time} secondes ..."
                sleep time: time, unit: 'SECONDS'
                utilsDocker.rmi(this, dk.img, remote)
            }
        }
    }

}