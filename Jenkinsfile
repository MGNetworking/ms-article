@Library('JenkinsLib_Shared') _

def remote = [:]
def nexus = [:]
def dockers = [:]

pipeline {
    agent {
        label 'master'
    }

    environment {
        Nas_CREDS = credentials('NAS')
        Prod_CREDS = credentials('PROD')
        Nexus_CREDS = credentials('nexus-credentials')
        GITHUB_TOKEN = credentials('Github')

        // User Keycloak
        TEST_USER_ONE = credentials('keycloak-test-user-one')
        TEST_USER_TWO = credentials('keycloak-test-user-two')

        // Postman
        POSTMAN_API_KEY = credentials('postman-api-key')
        COLLECTION_ID = credentials('MS_ARTICLE_COLLECTION_ID')
    }

    parameters {
        booleanParam(name: 'VERSION', defaultValue: true, description: 'Par défaut la version sera beta')
        booleanParam(name: 'FORCE', defaultValue: false, description: 'Forcer une compilation')
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


                    echo("Valeur param version: ${params.VERSION}")
                    echo("Message de publication: ${params.PUBLIC_MESSAGE}")

                    echo "Création de l'image de base "
                    env.IMAGE_NAME_BASE = "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_VERSION}"
                    echo("Nom de l'image docker de base : ${env.IMAGE_NAME}")

                    // Type 2.0.0-beta
                    echo "Création du nom de l'images avec sa version de construction"
                    env.BUILD = params.VERSION ? 'beta' : 'release'
                    env.IMAGE_TAG = "${env.IMAGE_VERSION}-${env.BUILD}"
                    env.IMAGE_NAME = "${env.DOCKER_IMAGE_NAME}:${env.IMAGE_TAG}"
                    echo("Nom de l'image docker avec sa version : ${env.IMAGE_NAME}")


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

                    ENVIRONMENT_ID = credentials('ENV_NAS_ID')
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
                            env.STACK_NAME)                       // stackName

                    // les données de connection serveur
                    remote = utilsServeur.remote(
                            "${env.BRANCH_NAME}",   // name
                            '192.168.1.xx',         // host
                            true,                   // allowAnyHosts
                            22,                     // port
                            Prod_CREDS_USR,         // user
                            Prod_CREDS_PSW)         // password

                    ENVIRONMENT_ID = '' // ID de votre environnement PROD
                    echo("Version de l'application : ${dockers.img}")
                }
            }
        }

        stage('Check version') {
            steps {
                script {
                    try {
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
                                echo("La version release suivant : ${version_release} n'existe pas dans le dépôt nexus")
                                echo("Donc le build peut être lancer !")
                                env.SKIP_BUILD = true
                            } else {
                                echo("La version release suivant : ${env.IMAGE_TAG} est déjà présente sur le serveur Nexus")
                                echo("Excécution des tests unitaires uniquement !")
                                env.SKIP_BUILD = false
                            }
                        }

                    } catch (Exception e) {
                        echo "❌ FAILURE - ${e.message}"
                    }

                }
            }
        }

        stage("Test : service ms-configuration") {
            steps {
                script {
                    try {
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

                    } catch (Exception e) {
                        echo "❌ FAILURE - ${e.message}"
                    }
                }
            }
        }


        stage("Open connection Nexus") {
            steps {
                script {
                    try {
                        echo("Ouverture de la connection au dépôt nexus sur le serveur ${env.BRANCH_NAME}")
                        utilsDocker.loginDepot(nexus, true, remote)

                        echo("Ouverture de la connection au dépôt nexus depuis Jenkins")
                        utilsDocker.loginDepot(nexus, false)
                    } catch (Exception e) {
                        error("⛔ ERROR - Pipeline arrêté suite à une exception : ${e.message}")
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
                            echo("Lancement des tests unitaires")
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE', message: "Echec des tests unitaires") {

                                sh """
                                    mvn test -Dspring.profiles.active=test \\
                                    -Dsurefire.reportsDirectory=target/unit-reports
                                """

                                def xmlFiles = sh(
                                        script: "find target/unit-reports -name '*.xml' | wc -l",
                                        returnStdout: true
                                ).trim()

                                echo "Nombre de fichiers de rapports XML trouvés : ${xmlFiles}"

                                if (xmlFiles == '0') {
                                    echo "ATTENTION : Aucun fichier de rapport de test trouvé !"
                                }

                                // Vérification du contenu de chaque fichier XML
                                sh """
                                    for file in target/unit-reports/*.xml; do
                                        echo "Contenu de \$file :"
                                        cat \$file
                                        echo "---"
                                    done
                                """

                                echo "Archivage des résultats des tests unitaires"
                                archiveArtifacts artifacts: 'target/unit-reports/*.xml', allowEmptyArchive: true

                                // Publication des résultats avec des options de débogage
                                try {
                                    junit(
                                            testResults: "target/unit-reports/*.xml",
                                            allowEmptyResults: true,    // permet de continuer même si aucun test n'est trouvé
                                            healthScaleFactor: 1.0,     // donne un poids égal à tous les tests
                                            skipPublishingChecks: true
                                    )
                                } catch (Exception e) {
                                    echo "Erreur lors de la publication des résultats : ${e.message}"
                                }
                            }
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
                            echo("Lancement des tests d'intégration")
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE', message: "Echec des tests d'intégration") {

                                sh """
                                    mvn verify -P integration -Dspring.profiles.active=test \\
                                    -Dfailsafe.reportsDirectory=target/failsafe-reports
                                """

                                def xmlFiles = sh(
                                        script: "find target/integration-reports -name '*.xml' | wc -l",
                                        returnStdout: true
                                ).trim()

                                echo "Nombre de fichiers de rapports XML trouvés : ${xmlFiles}"

                                if (xmlFiles == '0') {
                                    echo "ATTENTION : Aucun fichier de rapport de test trouvé !"
                                }

                                echo "Archivage des résultats de test"
                                archiveArtifacts artifacts: 'target/integration-reports/*.xml', allowEmptyArchive: true

                                echo "Publication immédiate des résultats de test d'intégration"
                                junit(
                                        testResults: "target/integration-reports/*.xml",
                                        allowEmptyResults: true,
                                        healthScaleFactor: 1.0,
                                        skipPublishingChecks: true
                                )
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
                            echo("Lancement des tests d'intégration et end to end")
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE', message: "Echec des test End to End") {

                                profileTest = "test-${env.BRANCH_NAME}"
                                echo("Le profile utililsé pour les tests end to end: ${profileTest}")
                                sh """
                                    mvn verify -P e2e -Dspring.profiles.active=${profileTest} \\
                                    -DSERVICE_CONFIG_DOCKER=http://192.168.1.56:8089 \\
                                    -Dfailsafe.reportsDirectory=target/failsafe-reports \\
                                    -Dtest.keycloak.user.one=${TEST_USER_ONE_USR} \\
                                    -Dtest.keycloak.password.one=${TEST_USER_ONE_PSW} \\
                                    -Dtest.keycloak.user.two=${TEST_USER_TWO_USR} \\
                                    -Dtest.keycloak.password.two=${TEST_USER_TWO_PSW}
                                """

                                def xmlFiles = sh(
                                        script: "find target/e2e-reports -name '*.xml' | wc -l",
                                        returnStdout: true
                                ).trim()

                                echo "Nombre de fichiers de rapports XML trouvés : ${xmlFiles}"

                                if (xmlFiles == '0') {
                                    echo "ATTENTION : Aucun fichier de rapport de test trouvé !"
                                }


                                echo "Archivage des résultats de test end to end"
                                archiveArtifacts artifacts: 'target/e2e-reports/*.xml', allowEmptyArchive: true

                                echo "Publication immédiate des résultats des tests end to end"
                                junit(
                                        testResults: "target/e2e-reports/*.xml",
                                        allowEmptyResults: true,
                                        healthScaleFactor: 1.0,
                                        skipPublishingChecks: true
                                )
                            }
                        }
                    }
                }
            }
        }

        stage('Maven Compilation') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
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
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    echo("Création de l'image Docker : ${dockers.img}")
                    sh("docker compose build --no-cache")
                }
            }
        }

        stage('Tag / Push Docker Images dépôt Nexus') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
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
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    try {
                        echo("Mise à jours du projet ms-article sur le serveur ${env.BRANCH_NAME}")

                        String commande = "cd ${dockers.pathProjet} && " +
                                "git checkout ${env.BRANCH_NAME} && " +
                                "git pull origin ${env.BRANCH_NAME}"

                        utilsGit.gitPullSsh(remote, commande)

                    } catch (Exception e) {
                        echo("❌ DÉTAILS DE L'ERREUR: - ${e.message}")
                        error("❌ STACK TRACE: ${e.getStackTrace().join('\n')}")

                    }
                }
            }
        }

        stage('Pull Docker Images dépôt Nexus') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    try {
                        echo("Pull de l'image docker: ${dockers.img} sur le serveur: ${env.BRANCH_NAME}")
                        utilsDocker.pullImg(dockers.img, true, remote)

                        echo("Affiche la liste des images Docker sur le serveur ${env.BRANCH_NAME}")
                        utilsDocker.dockerlsImg(true, remote)

                    } catch (Exception e) {
                        echo("❌ DÉTAILS DE L'ERREUR: - ${e.message}")
                        error("❌ STACK TRACE: ${e.getStackTrace().join('\n')}")
                    }
                }
            }
        }

        stage('Status Stack en cours') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    try {
                        echo("Affiche le status ${dockers.stackName} de la stack en cours ")
                        utilsDocker.getPsStack(dockers.stackName, true, remote)

                        echo("Vérifi si la stack ${dockers.stackName} est deployer ou mettre à jours ")
                        env.STATUS_STACK = utilsDocker.statusStack(dockers.stackName, true, remote)

                        echo("La stack ${dockers.stackName} sera a " + (env.STATUS_STACK ? "mettre à jours" : "déployée") +
                                " sur le serveur ${env.BRANCH_NAME}")

                    } catch (Exception e) {
                        echo("❌ DÉTAILS DE L'ERREUR: - ${e.message}")
                        error("❌ STACK TRACE: ${e.getStackTrace().join('\n')}")
                    }
                }
            }
        }

        stage('Update / Deploy') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    try {
                        echo("Deploiment sur le serveur: ${env.BRANCH_NAME}, en version: ${env.BUILD}")

                        string cd = "cd ${dockers.pathProjet} && ./script/deploy.sh ${env.BUILD} ${env.BRANCH_NAME}"

                        utilsDocker.deployStack(cd, true, remote)

                    } catch (Exception e) {
                        echo("❌ DÉTAILS DE L'ERREUR: - ${e.message}")
                        error("❌ STACK TRACE: ${e.getStackTrace().join('\n')}")
                    }
                }
            }
        }

        stage('Vérification de disponibilité') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
            steps {
                script {
                    def maxRetries = 10
                    def retryDelay = 15
                    def success = false

                    for (int i = 1; i <= maxRetries; i++) {
                        echo "👉 Tentative ${i}/${maxRetries} de vérification du service..."

                        try {
                            def response = sh(script: "curl -s http://${remote.host}:${PORT}/actuator/health", returnStdout: true).trim()
                            echo "Réponse reçue: ${response}"

                            if (response.contains('"status":"UP"')) {
                                echo "✅ SUCCESS - Le service ${NAME_SERVICE} est disponible après ${i} tentative(s)"
                                success = true
                                break
                            } else {
                                echo "⏳ Le service n'est pas encore disponible. Attente de ${retryDelay} secondes..."
                                sleep time: retryDelay, unit: 'SECONDS'
                            }
                        } catch (Exception e) {
                            echo "⚠️ Erreur lors de la vérification: ${e.message}"
                            echo "⏳ Attente de ${retryDelay} secondes avant nouvelle tentative..."
                            sleep time: retryDelay, unit: 'SECONDS'
                        }
                    }

                    if (!success) {
                        error "⛔ ERROR - Le service ${NAME_SERVICE} n'est toujours pas disponible après ${maxRetries} tentatives!"
                    }
                }
            }
        }


        stage('REGRESSION') {
            agent {
                docker {
                    image 'postman/newman:5-alpine'
                    args '--entrypoint="" -u root'
                }
            }
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                sh 'newman --version'
                sh 'mkdir -p postman_files newman-reports' // Création d'un répertoire pour stocker les fichiers

                script {
                    def collectionUrl = "https://api.getpostman.com/collections/${COLLECTION_ID}?apikey=${POSTMAN_API_KEY}"

                    // Exécution Newman avec publication continue même en cas d'échec
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE', message: "Echec pendant l'exécution des tests de régressions") {


                        // Récupération de l'environnement si spécifié
                        sh '''
                            if [ ! -z "${ENVIRONMENT_ID}" ]; then
                                curl -s -X GET "https://api.getpostman.com/environments/${ENVIRONMENT_ID}" \
                                    -H "X-Api-Key: ${POSTMAN_API_KEY}" \
                                    -o postman_files/environment_response.json
        
                                jq '.environment' postman_files/environment_response.json > postman_files/environment.json
        
                                echo "Environnement récupéré avec succès"
                            fi
                        '''

                        sh 'ls -la postman_files/'

                        if (fileExists('postman_files/environment.json')) {
                            sh """
                                newman run "${collectionUrl}" \
                                --environment=postman_files/environment.json \
                                --reporters cli,junit,htmlextra \
                                --reporter-junit-export=newman-reports/junit-report.xml \
                                --reporter-htmlextra-export=newman-reports/report.html || true
                            """
                        } else {
                            sh """
                                newman run "${collectionUrl}" \
                                --reporters cli,junit,htmlextra \
                                --reporter-junit-export=newman-reports/junit-report.xml \
                                --reporter-htmlextra-export=newman-reports/report.html || true
                            """
                        }
                    }

                    // Vérifier si les fichiers de rapport existent
                    sh 'ls -la newman-reports/ || echo "Répertoire newman-reports vide ou inexistant"'
                    sh 'ls -la postman_files/ || echo "Répertoire postman_files vide ou inexistant"'

                    junit(
                            testResults: "newman-reports/junit-report.xml",
                            allowEmptyResults: true,
                            healthScaleFactor: 1.0,
                            skipPublishingChecks: true
                    )

                    publishHTML([
                            allowMissing         : true,
                            alwaysLinkToLastBuild: true,
                            keepAll              : true,
                            reportDir            : 'newman-reports',
                            reportFiles          : 'report.html',
                            reportName           : 'Newman HTML Report'
                    ])
                }

            }
        }

        stage('Publication du projet sur Github') {
            when {
                expression { env.SKIP_BUILD?.toBoolean() || params.FORCE?.toBoolean() }
            }
            agent {
                label 'master'
            }
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

                            echo "⚠️ Les paramètres de la version sont manquants."
                            echo "⚠️ Version: ${env.IMAGE_TAG}, Publication: ${params.PUBLIC_MESSAGE}"
                            currentBuild.result = 'UNSTABLE'
                        }

                    } catch (Exception ex) {
                        echo "⚠️ Une erreur est survenue pendant la création de la publication " +
                                "de l'image: ${env.IMAGE_TAG}, Publication: ${params.PUBLIC_MESSAGE}"
                        echo "⚠️ DÉTAILS DE L'ERREUR: ${ex.message}"
                        echo "⚠️ STACK TRACE: ${ex.getStackTrace().join('\n')}"
                        currentBuild.result = 'UNSTABLE'
                    }

                }
            }
        }

        stage('Close connection Nexus') {
            agent {
                label 'master'
            }
            steps {
                script {
                    try {
                        echo("Déconnection du dépôt nexus et le serveur ${env.BRANCH_NAME}")
                        utilsDocker.logoutDepot(nexus, true, remote)

                        echo("Déconnection du dépôt nexus et Jenkins")
                        utilsDocker.logoutDepot(nexus)
                    } catch (Exception e) {
                        echo "⛔ DÉTAILS DE L'ERREUR: ${e.message}"
                        echo "⛔ STACK TRACE: ${e.getStackTrace().join('\n')}"
                    }
                }
            }
        }

        stage('Clean images') {
            agent {
                label 'master'
            }
            when {
                expression { env.SKIP_BUILD?.toBoolean() }
            }
            steps {
                script {
                    try {
                        echo("Nettoyage de l'images de base : ${env.IMAGE_NAME_BASE}")
                        utilsDocker.rmi(env.IMAGE_NAME_BASE)

                        echo("Nettoyage de l'images beta / relase : ${dockers.img}")
                        utilsDocker.rmi(dockers.img)
                    } catch (Exception e) {
                        echo "⛔ DÉTAILS DE L'ERREUR: ${e.message}"
                        echo "⛔ STACK TRACE: ${e.getStackTrace().join('\n')}"
                    }
                }
            }
        }

    }


    post {
        success {
            script {
                def stack = "du déploiement de la stack avec la version du service ${env.IMAGE_VERSION}"
                def service = "de la mise à jour du service a la version ${env.IMAGE_VERSION}"
                echo('Fin ' + (env.STATUS_STACK ? stack : service))
            }
        }
        failure {
            script {
                echo("Échec pipeline ")

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

                if (env.BRANCH_NAME == 'nas') {
                    utilsDocker.rmi(dockers.img, true, remote)
                } else {
                    utilsDocker.rmi(dockers.img, false, remote)
                }
            }
        }
        unstable {
            echo "Build instable, vérification nécessaire"
        }
    }

}