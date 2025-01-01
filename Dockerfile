# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine

# Installation des tools pour wait_for_config.sh
RUN apk --no-cache add curl jq

# Création du dossier repertoire de travail
WORKDIR /app
COPY target/*.jar /app/app.jar

# Création du dossier de logs
RUN mkdir /app/logs

# Créer le fichier de log de santer
RUN touch /app/logs/healthcheck.log

COPY ./script/wait_for_config.sh  /app
COPY ./script/healthcheck.sh /app

# modification des droits d'exécution
RUN chmod +x /app/wait_for_config.sh
RUN chmod +x /app/healthcheck.sh

EXPOSE 9010
ENTRYPOINT ["sh", "/app/wait_for_config.sh"]