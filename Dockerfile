# Définition de l'image de base
FROM maven:3.8.5-jdk-8-slim as build
MAINTAINER ghalem maxime

# Création du répertoire de travail
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/pom.xml

# argument venant du docker compose
ARG env_profile

# profile attendu pour la compilation
ENV SPRING_PROFILES_ACTIVE=$env_profile

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