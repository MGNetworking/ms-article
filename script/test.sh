#!/bin/bash

# environnement dev
# mvn clean test -Dspring.profiles.active=dev

# Activez le profil integration pour exécuter les tests d'intégration :
# mvn clean verify -P integration -Dspring.profiles.active=dev

# Il est configuré pour exécuter les fichiers *E2ETest.java dans le package endToEnd.
#mvn verify -P e2e -Dspring.profiles.active=dev

# environnement dev avant compilation Docker
#mvn clean test -Dspring.profiles.active=devDocker