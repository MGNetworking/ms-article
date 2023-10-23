# Définition de l'image de base
FROM maven:3.8.5-jdk-8-slim as build

# Création du répertoire de travail
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/pom.xml

# arguement venant du docker compose
ARG CONFIG_SERVICE_URI_ARG
ARG env_profile
ARG eureka_defaultZone

# variable attendu dans le fichier bootstrap.yml du projet
ENV CONFIG_SERVICE_URI=$CONFIG_SERVICE_URI_ARG
ENV SPRING_PROFILES_ACTIVE=$env_profile


RUN echo "CONFIG_SERVICE_URI: $CONFIG_SERVICE_URI"
RUN echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"

# lancement de la compilation
RUN mvn package

# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine
WORKDIR /app

# Copie du jar de l'application
COPY --from=build /app/target/*.jar /app/app.jar


EXPOSE 8666
ENTRYPOINT [ "java","-jar", "app.jar" ]

# docker build -t article/latest .
# docker run -e "SPRING_PROFILES_ACTIVE=dev" --name article -p 8089:8089 -d article/latest