# Définition de l'image de base
FROM maven:3.8.5-jdk-8-slim as build
MAINTAINER "ghalem maxime"

# Création du répertoire de travail
WORKDIR /app

# copie des fichiers source
COPY ./src /app/src
COPY ./pom.xml /app/pom.xml

# argument venant du docker compose
ARG SPRING_PROFILES_ACTIVE
ARG CONFIG_SERVICE_URI_host

# variable d'environnement set pour maven
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
ENV CONFIG_SERVICE_URI_host=$CONFIG_SERVICE_URI_host

# lancement de la compilation
RUN mvn package

# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine

# Installation des tools pour wait_for_config.sh
RUN apk --no-cache add curl jq

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

COPY ./script/wait_for_config.sh /app
RUN chmod +x /app/wait_for_config.sh

EXPOSE 9010
ENTRYPOINT ["sh", "/app/wait_for_config.sh"]