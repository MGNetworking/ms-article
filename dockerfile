# Définition de l'image de base
FROM maven:3.8.5-jdk-8-slim as build

# Création du répertoire de travail
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/pom.xml

# arguement venant du docker compose
ARG CONFIG_SERVICE_URI_ARG
ARG env_profile

# variable attendu dans le fichier bootstrap.yml du projet
ENV CONFIG_SERVICE_URI=$CONFIG_SERVICE_URI_ARG
ENV SPRING_PROFILES_ACTIVE=$env_profile

# lancement de la compilation
RUN mvn package

# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine
WORKDIR /app

# Copie du jar de l'application
COPY --from=build /app/target/*.jar /app/app.jar

# Création du répertoire de travail
RUN mkdir /app
WORKDIR /app

# Copie du jar de l'application
COPY --from=build /app/target/*.jar /app/app.jar

# Définition de la variable d'environnement pour activer le profil "dev"
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8666
ENTRYPOINT [ "java","-jar", "app.jar" ]

# docker build -t article/latest .
# docker run -e "SPRING_PROFILES_ACTIVE=dev" --name article -p 8089:8089 -d article/latest