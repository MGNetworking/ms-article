@Library('JenkinsLib_Shared') _


// Configurations des serveurs
def remote
def nexus
def dockers

pipeline {
    agent any

    environment {
        Nas_CREDS = credentials('NAS')
        Prod_CREDS = credentials('PROD')
        Nexus_CREDS = credentials('nexus-credentials')
        GITHUB_TOKEN = credentials('Github')
    }

    parameters {
        booleanParam(name: 'VERSION', defaultValue: true, description: 'Par défaut la version sera beta')
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

                    env.NEXUS_DOMAIN = nexus.domain

                    echo("Valeur param version: ${params.VERSION}")
                    echo("Message de publication: ${params.PUBLIC_MESSAGE}")

                    // Type 2.0.0-beta
                    env.BUILD = params.VERSION ? 'beta' : 'release'
                    env.IMAGE_TAG = "${env.IMAGE_VERSION}-${params.VERSION ? 'beta' : 'release'}"

                    // Type sonatype-nexus.backhole.ovh/ms-article-service:2.0.0-beta
                    env.IMAGE_NAME = "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}"
                    echo("Nom de l'image docker : ${env.IMAGE_NAME}")

                    if (!env.IMAGE_TAG?.trim()) {
                        error("La version de l'image est obligatoire : ${env.IMAGE_TAG}")
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
                    // Les données dockers projet
                    dockers = utilsServeur.dockers(
                            env.IMAGE_NAME,                    // img
                            '/volume1/docker/ms-article',      // pathProjet
                            env.STACK_NAME                     // stackName
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
                    // les données dockers projet
                    dockers = utilsServeur.dockers(
                            env.IMAGE_NAME,                       // img
                            '/home/max/docker_home/ms-article',   // pathProjet
                            env.STACK_NAME)                         // stackName

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
                    version_beta = "${env.IMAGE_VERSION}-beta"
                    version_release = "${env.IMAGE_VERSION}-release"

                    def http_status_beta = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${env.PATH_NEXUS}/manifests/${version_beta}
                      """, returnStdout: true).trim()

                    def http_status_release = sh(script: """
                        curl -s -o /dev/null -w "%{http_code}" -u ${nexus.user}:${nexus.pass} \
                        https://${nexus.domain}/repository/docker-private/v2/${env.PATH_NEXUS}/manifests/${version_release}
                      """, returnStdout: true).trim()

                    echo("HTTP Status beta: $http_status_beta et HTTP Status release: $http_status_release")


                    if (env.IMAGE_TAG == version_beta) {

                        if (http_status_beta.equals("404")) {
                            echo("La version beta suivant : ${version_beta} n'existe pas dans le dépôt nexus")
                            echo("Donc le build peut être lancer !")
                            env.SKIP_BUILD = true
                        } else {
                            echo("La version beta suivant : ${env.IMAGE_TAG} est déjà présente sur le serveur Nexus")
                            echo("Excécution des test unitaire uniquement !")
                            env.SKIP_BUILD = false
                        }

                    } else if (env.IMAGE_TAG == version_release) {

                        if (http_status_release.equals("404")) {
                            echo("La version relase suivant : ${version_release} n'existe pas dans le dépôt nexus")
                            echo("Donc le build peut être lancer !")
                            env.SKIP_BUILD = true
                        } else {
                            echo("La version relase suivant : ${env.IMAGE_TAG} est déjà présente sur le serveur Nexus")
                            echo("Excécution des test unitaire uniquement !")
                            env.SKIP_BUILD = false
                        }
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
                    try {
                        echo("Ouverture de la connection au dépôt nexus sur le serveur ${env.BRANCH_NAME}")
                        utilsDocker.loginDepot(nexus, true, remote)

                        echo("Ouverture de la connection au dépôt nexus depuis Jenkins")
                        utilsDocker.loginDepot(nexus, false)
                    } catch (Exception e) {
                        echo "⛔ ERREUR DÉTECTÉE : ${e.message}"
                        error("🚨 Pipeline arrêté suite à une exception : ${e.message}")
                    }
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
                                    "-Dsurefire.report.directory=${env.WORKSPACE}/target/surefire-reports")
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
                                        "-Dfailsafe.report.directory=${env.WORKSPACE}/target/failsafe-reports")
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
                                        "-Dfailsafe.report.directory=${env.WORKSPACE}/target/failsafe-reports"
                                sh 'pwd'
                                sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'
                            }
                        }
                    }
                }

            }
        }

        stage('Maven Compilation') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
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
                    sh("mvn package -Dspring.profiles.active=${env.BRANCH_NAME} " +
                            "-DSERVICE_CONFIG_DOCKER=${env.SERVICE_CONFIG_URI}")

                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    echo("Création de l'image Docker : ${dockers.img}")
                    sh("docker compose build --no-cache")
                }
            }
        }

        stage('Tag / Push Docker Images dépôt Nexus') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    // TAG de l'image vers la version spécifier beta / relase
                    echo("Tag de l'image docker ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} vers ${dockers.img}")
                    sh(script: "docker tag ${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION} ${dockers.img}")
                    echo("push de l'image ${dockers.img} vers le dépôt Docker Nexus")
                    sh(script: "docker push ${dockers.img}")
                }
            }
        }

        stage('Pull du projet') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    echo("Mise à jours du projet ms-article sur le serveur ${env.BRANCH_NAME}")
                    String commande = "cd ${dockers.pathProjet} && " +
                            "git checkout ${env.BRANCH_NAME} && " +
                            "git pull origin ${env.BRANCH_NAME}"
                    utilsGit.gitPullSsh(remote, commande)
                }
            }
        }

        stage('Pull Docker Images dépôt Nexus') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    echo("Pull de l'image docker: ${dockers.img} sur le serveur: ${env.BRANCH_NAME}")
                    utilsDocker.pullImg(dockers.img, true, remote)

                    echo("Affiche la liste des images Docker sur le serveur ${env.BRANCH_NAME}")
                    utilsDocker.dockerlsImg(true, remote)
                }
            }
        }

        stage('Status Stack en cours') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    echo("Affiche le status ${dockers.stackName} de la stack en cours ")
                    utilsDocker.getPsStack(dockers.stackName, true, remote)

                    echo("Vérifi si la stack ${dockers.stackName} est deployer ou mettre à jours ")
                    env.STATUS_STACK = utilsDocker.statusStack(dockers.stackName, true, remote)

                    echo("La stack ${dockers.stackName} sera a " + (env.STATUS_STACK ? "mettre à jours" : "déployée") +
                            " sur le serveur ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Update / Deploy blog') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    echo("Deploiment sur le serveur: ${env.BRANCH_NAME} , en version: ${env.BUILD}")
                    string commande = "cd ${dockers.pathProjet} && " +
                            "export PROFILES=${env.BRANCH_NAME} && " +
                            "./script/deploy.sh ${env.BUILD}"
                    utilsDocker.deployStack(commande, true, remote)
                }
            }
        }

        stage('Vérification de disponibilité') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    status = true
                    for (int index = 0; index < 10; index++) {

                        echo("Requet CURL n° ${index} du service : ${NAME_SERVICE}")
                        echo("à l'adresse : http://${remote.host}:${PORT}/actuator/health ")

                        String result = sh(script: "curl -s http://${remote.host}:${PORT}/actuator/health | " +
                                "jq -r '.status'", returnStdout: true, returnStatus: false)

                        if (result.contains("UP")) {
                            echo("Sorti : ${result}")
                            echo("✅ La mise en service de ${env.NAME_SERVICE} à été réalisé avec Succès ")
                            status = false
                            break
                        } else {
                            echo("Sorti : ${result}")
                            echo "Le service n'est pas encore UP. Attente de 15 secondes..."
                            echo "Tentative n° $index"
                            sleep time: 15, unit: 'SECONDS'
                        }
                    }
                    if (status) {
                        error("❌ Le service ${NAME_SERVICE} est en echec !!!")
                    }
                }
            }
        }

        stage('Publication du projet sur Github') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            agent any
            steps {
                script {
                    try {
                        if (env.BUILD == 'beta') {

                            utilsGit.createOrUpdatePreRelease(
                                    env.IMAGE_TAG,
                                    env.REPO_NAME,
                                    GITHUB_TOKEN,
                                    params.PUBLIC_MESSAGE)

                        } else if (env.BUILD == 'release') {

                            utilsGit.createOrUpdateRelease(
                                    env.IMAGE_TAG,
                                    env.REPO_NAME,
                                    GITHUB_TOKEN,
                                    params.PUBLIC_MESSAGE)
                        } else {
                            echo "❌ Les paramètres de la version son manquantes."
                            echo "❌ Il ne peux y avoir une publication vers le dépôt !"
                            echo "❌ Version: ${env.IMAGE_TAG}, "
                            echo "❌ Message de publication: ${params.PUBLIC_MESSAGE}"
                            currentBuild.result = 'FAILURE'
                        }

                    } catch (Exception ex) {

                        echo "❌ Une erreur est survenu pendant le processus de création de publication"
                        echo "❌ Message : ${ex.getMessage()}"
                        echo "❌ Version : ${env.IMAGE_TAG}"
                        currentBuild.result = 'FAILURE'
                    }

                }
            }
        }

    }


    post {
        always {
            script {
                try {
                    echo("Déconnection au dépôt nexus docker entre le serveur ${env.BRANCH_NAME} et le dépôt nexus")
                    echo ("nexus domain => ${nexus.domain}")
                    utilsDocker.logoutDepot(env.NEXUS_DOMAIN, true, remote)

                    echo("Fermeture de la connection au dépôt nexus depuis Jenkins")
                    utilsDocker.logoutDepot(env.NEXUS_DOMAIN)

                    if (!env.SKIP_BUILD?.toBoolean()) {
                        echo("Nettoyage de l'images de base : ${env.IMAGE_NAME}")
                        utilsDocker.rmi(env.IMAGE_NAME)

                        echo("Nettoyage de l'images beta / relase : ${dockers.img}")
                        utilsDocker.rmi(dockers.img)
                    } else {
                        echo "Aucun nettoyage a réalisé, puisqu'il n'y a eu aucune compilation!"
                    }

                    sh 'pwd'
                    sh 'ls -al target/surefire-reports || echo "surefire-reports non trouvé"'
                    sh 'ls -al target/failsafe-reports || echo "failsafe-reports non trouvé"'

                    echo "Collecte des rapports JUnit pour les tests unitaires."
                    junit testResults: "target/surefire-reports/*.xml", allowEmptyResults: true

                    echo "Collecte des rapports JUnit pour les tests d'intégration et E2E."
                    junit testResults: "target/failsafe-reports/*.xml", allowEmptyResults: true


                    def testResults = sh(script: "grep -H '<testsuite' ${env.WORKSPACE}/target/surefire-reports/*.xml || " +
                            "echo 'Aucun résultat trouvé'", returnStdout: true).trim()
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
                def stack = "du déploiement de la stack avec la version du service ${env.IMAGE_VERSION}"
                def service = "de la mise à jour du service a la version ${env.IMAGE_VERSION}"
                echo('Fin ' + (env.STATUS_STACK ? stack : service))
            }
        }
        failure {
            script {
                echo("Échec !")

                if (!STATUS_STACK) {
                    echo("Échec du déploiement de la stack ${dockers.stackName}")
                    echo("Suppression de la stack ${dockers.stackName} sur le serveur distant")
                    utilsDocker.rmStack(dockers.stackName, true, remote)
                } else {
                    echo("Échec de la mise à jour de la stack ${dockers.stackName}")
                    echo("ROLLBACK de la stack ${dockers.stackName}")
                    utilsDocker.rollbackService(env.NAME_SERVICE, true, remote)
                }
                def time = 15
                echo "Suppression de l'image en échec ${dockers.img} sur le serveur dans ${time} secondes ..."
                sleep time: time, unit: 'SECONDS'
                utilsDocker.rmi(this, dockers.img, remote)
            }
        }
    }

}