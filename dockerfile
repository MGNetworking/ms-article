FROM maven:3.8.5-jdk-8-slim as build

# Création du répertoire de travail
WORKDIR /app

COPY src /app/src
COPY pom.xml /app/pom.xml

RUN mvn package -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments=-Dspring.profiles.active=dev

# Image de base pour l'exécution de l'application
FROM openjdk:8-jdk-alpine

# Définition de l'utilisateur
#USER root

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