# API de Gestion d'Articles

Bienvenue dans l'API de Gestion d'Articles, une solution puissante pour la création, la modification et la mise à jour
d'articles sur votre site web.

## Table des matières

* [Fonctionnalités principales](#fonctionnalités-principales)
* [Configuration et dépendances](#configuration-requise)
    * [Étapes de Configuration](#étapes-de-configuration)
    * [Les dépendances externes](#les-dépendances-externes)
* [Intégration avec ms-gateway](#intégration-avec-ms-gateway)
    * [Accès via ms-gateway](#accès-via-ms-gateway)
    * [Accès Direct par Adresse IP](#accès-direct-par-adresse-ip)
    * [Postman](#postman)
* [Les Scripts](#les-scripts)
* [Mode Débogage](#mode-débogage)

## Documentation

La documentation complète de l'API est disponible dans le dossier [docs](/docs). Vous y trouverez des informations
détaillées sur l'installation, les points d'extrémité, des exemples pratiques, et bien plus encore.

## Fonctionnalités principales

`Création d'Articles `: Permet aux utilisateurs d'ajouter de nouveaux articles via le site web en utilisant des requêtes
simples.

`Modification d'Articles` : Offre la flexibilité nécessaire pour mettre à jour le contenu des articles existants en
fonction des besoins des utilisateurs.

`Mise à Jour d'Articles` : Fournit des mécanismes pour mettre à jour les informations des articles, garantissant une
gestion précise et à jour du contenu.

`Affichage d'Articles` : Permet aux sites web d'afficher facilement les articles stockés dans la base de données,
offrant ainsi une expérience utilisateur fluide.

## Configuration Requise

Avant de commencer, assurez-vous simplement que vous disposez d'un environnement compatible avec `Java` et `Maven`.
Cette API a été développée avec `Spring Boot version 2.4.5`, et Maven se chargera automatiquement de l'importation des
dépendances spécifiées dans le fichier `pom.xml`.

### Étapes de Configuration

1. Assurez-vous que Maven et importer les dépendances et soit prête à fonctionner.
2. Assurez-vous que l'API `ms-configuration` soit en cours d'exécution et accessible.
3. Lancement de l'API
    * `run.sh` : Il permet d'exécuter la compilation est l'exécution de L'API [voir les scripts](#les-scripts)
    * `Maven`  : Dans l'interface graphique de l'IDE intellij, vous pouvez créer une configuration en mode
      debug [voir Mode Débogage ](#mode-débogage)

### Les dépendances externes

C'est dependence externe sont indispensables et obligatoires pour le fonctionnement de ce projet.

[docker-keycloak-postgres](https://github.com/MGNetworking/docker-keycloak-postgres) : Vous aurais besoin du
projet. Il doit être en cours d'exécution dans l'environnement d'écrit dans le fichier de properties. Ce projet est en
gestion des autorisations utilisateur pour la création, la mise à jour des articles du site. Ce projet est utilisé
pendant la phase de test unitaire, il est donc obligatoire et doit être opérationnelle pour l'utilisation de l'API
ms-article.

[ms-configuration](https://github.com/MGNetworking/ms-configuration) : Ce projet fait partie du
projet principal [back-end](https://github.com/MGNetworking/back-end) qui regroupe toutes les API de ce projet, il
possède comme ce projet son propre depôt.

## Intégration avec ms-gateway

Cette API fonctionne au travers du micro-service [ms-gateway](https://github.com/MGNetworking/ms-gateway). Cette API
fait parti du projet principal [back-end](https://github.com/MGNetworking/back-end). Cependant, elle peut également être
accessible directement par son adresse IP.

### Accès via ms-gateway

L'accès à cette API via `ms-gateway`, suit le modèle standard de routage au sein de notre architecture micro-services.
Consultez la documentation de [ms-gateway](https://github.com/MGNetworking/ms-gateway) pour obtenir des informations
spécifiques sur la configuration des routes.

### Accès Direct par Adresse IP

Si nécessaire, vous pouvez accéder directement à cette API en utilisant son adresse IP. Assurez-vous que les règles de
pare-feu et de sécurité appropriées sont en place.

### Postman

Pour simplifier l'interaction avec l'API pour le test en développement, dans le dossier Postman Collection, vous
trouverez le fichier `REST API  ms-article.postman_collection.json`. Vous pouvez l'importé dans votre environnement
Postman.

Il contient une description d'utilisation et des exemples de requêtes pour les principaux points de terminaison de l'
API `ms-article`.

## Les Scripts

Les scripts `run.sh` et `down.sh` ont été créer dans le but de facilité l'exécution du projet en environment DEV, mais
pas pour le mode debug. Ils ont pour objectif de test l'exécution de l'API dans l'infrastructure docker de manière plus
simple et rapide.

* `run.sh` : Il permet de lancer l'API avec 2 modes compilation différent :

    * La Compilation via Dockerfile :  
      Cette compilation sera exécuter une phase de Build via le `Dockerfile` par le `docker-compose.yml` en utilisant
      l'image `maven:3.8.5-jdk-8-slim` , puis une phase de copiage dans l'image `openjdk:8-jdk-alpine ` des fichiers
      compilé. Cela permet d'avoir une image d'API plus légère et donc optimisée.

    * La Compilation via Maven :  
      Cette compilation exécute la compilation avec Maven avec votre environnement de manière plus directe qu'avec le
      Dockerfile. Cela permet d'éviter de compilé l'image via le `Dockerfile-dev` par le `docker-compose-dev.yml` ce qui
      est un gain de temps, ce qui est un
      gain de temps puisque la Compilation via Dockerfile doit à chaque lancement récupérer toutes les dépendances du
      projet contenu dans le pom.xml avec sa compilation.


* `down.sh` : Il permet l'arrêter est la suppression du conteneur de l'image. Il supprime aussi toutes les images et
  conteneur non utilisé ainsi que les orphelins sur le système host.


* `wait_for_config.sh` : Ce script n'est pas utilisé dans le development de manière direct, mais il est ajouté dans la
  phase de compilation dans le but d'attendre que le service `ms-configuration` soit en cours d'exécution. Si ce service
  n'est pas en cours d'exécution, il ne pourra récupérer son fichier de properties ce qui provoquera un échec
  d'exécution.

NB : Le service `ms-configuration` est aussi important pendant la phase de compilation. Sans son fichier de
configuration, il ne peut n'y compiler n'y s'exécuter !!!

## Mode Débogage

Si vous avez besoin de déboguer l'API, suivez ces étapes pour configurer un environnement de débogage :

Dans intellij, aller vers éditer une configuration, puis ajoute avec le plus et sélectionner Maven

![maven-debug.png](images/edit-configuration.png)

Cela ajoutera un onglet supplémentaire qu'il vous faudra configurer.

![maven-debug.png](images/maven-debug.png)
Dans l'espace Run ajoute la commande :

```shell
clean package spring-boot:run "-Dspring-boot.run.jvmArguments=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -Dspring.profiles.active=dev"
```

Détail de la commande pour le mode debug :

1. `clean package` : Les goals Maven
2. `spring-boot:run` : Le lancement de l'API
3. `-Dspring-boot.run.jvmArguments` le passage en arguments pour la JVM
4. `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005`  
   Cette partie de la commande correspond à la configuration du débogueur Java (Java Debugger Wire Protocol - JDWP) pour
   une application Spring Boot.

    * `-Xdebug` : Active le support du débogueur Java.
    * `-Xrunjdwptransport=dt_socket,server=y,suspend=y,address=5005` :
        * `transport=dt_socket`: Spécifie le mode de transport pour la communication entre le débogueur et
          l'application.
          Dans ce cas, il utilise le socket (dt_socket).
        * `server=y`: Indique que l'application doit agir en tant que serveur pour le débogueur, ce qui signifie qu'elle
          attendra une connexion du débogueur.
        * `suspend=y`: Indique que l'application doit être suspendue jusqu'à ce qu'une connexion de débogage soit
          établie.
          Cela signifie que l'application attendra le débogueur avant de commencer à s'exécuter.
        * `address=5005`: Spécifie le port sur lequel l'application écoutera les connexions du débogueur. Dans ce cas,
          le port est 5005.

5. `-Dspring.profiles.active=dev` : Le profile active permet de prècisé l'environnement dans lequel s'execute
   cette API. Cela aura pour effet au moment de l'exécution, de cibler le fichier de properties avec lequel cette API
   va fonctionner et aussi les testes unitaires qui seront exécuter au moment de la compilation.

Working directory : Cible le dossier projet (l'API ms-article)


