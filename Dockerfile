# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine

# Installation des tools pour wait_for_config.sh
RUN apk --no-cache add curl jq

# Définition de la variable contenant le nom du script
ENV WAIT_SCRIPT=wait_for_config.sh

# Création du dossier repertoire de travail
WORKDIR /app
COPY target/*.jar /app/app.jar

COPY script/${WAIT_SCRIPT}  /app
COPY ./script/healthcheck.sh /app

RUN mkdir /app/logs \
    && touch /app/logs/healthcheck.log \
    && chmod +x /app/*.sh

EXPOSE 9010
ENTRYPOINT ["sh", "-c","sh /app/${WAIT_SCRIPT}"]