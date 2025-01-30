#!/bin/bash

echo "export des variables "
export $(cat .env)

# Vérifier si le fichier ~/.profile existe avant de le charger
if [ -f ~/.profile ]; then
    echo "Chargement et application des variables d’environnement, "
    echo "définies dans le fichier .profile de la session utilisateur"
    source ~/.profile
    echo "✅ Fichier ~/.profile chargé avec succès."
else
    echo "⚠️ Fichier ~/.profile introuvable, chargement ignoré."
fi


echo "Chargement des droites utilisateur du système host"
USER_ID=$(id -u)
GROUP_ID=$(id -g)
export USER_ID
export GROUP_ID

echo "Déploiement / update de la stack : $STACK_NAME"
echo "PROFILES : $PROFILES"

# Demander le suffixe à ajouter (ou utiliser une variable pour le définir)
if [ -z "$1" ]; then
  echo "Aucun suffixe n'a était spécifié. Utilisation du suffixe par défaut 'release' "
  SUFFIX="release"
else
  SUFFIX="$1"
  echo "Utilisation du suffixe spécifié pour la modification du nom de la version: $SUFFIX"
fi

# Modifier la version de l'image avec le suffixe
export IMAGE_VERSION="${IMAGE_VERSION}-${SUFFIX}"
echo "Modification du nom de la version: $IMAGE_VERSION"
echo "Le nom de la stack: $STACK_NAME"
echo "PROFILES: $PROFILES"

echo "Vérification et création du dossier logs"
LOGS_DIR="$(pwd)/logs"

if [ ! -d "$LOGS_DIR" ]; then
    echo "Le dossier logs n'existe pas. Création en cours..."

    mkdir -p "$LOGS_DIR" || { echo "Erreur : Échec de la création du dossier logs."; exit 1; }
    chmod -R 760 "$LOGS_DIR" || { echo "Erreur : Impossible de modifier les permissions."; exit 1; }
    chown -R ${USER_ID}:${GROUP_ID} "$LOGS_DIR" || { echo "Erreur : Impossible de modifier le propriétaire."; exit 1; }

    echo "✅ Dossier logs créé avec succès !"
else
    echo "✅ Le dossier logs existe déjà."
fi

# Le déploiement sur le serveur
echo "🚀 Deploiement avec le PROFILES : " $PROFILES
echo "Commande de déploiement sur le serveur $PROFILES"
echo "docker stack deploy -c ./docker-compose-swarm.yml --resolve-image never $STACK_NAME"
docker stack deploy -c ./docker-compose-swarm.yml --resolve-image never $STACK_NAME

echo "Liste des processus en cours sur stack : " $STACK_NAME
docker service ps article_ms-article

echo "Docker log de la stack : " $STACK_NAME
docker service logs article_ms-article --tail 100

echo "✅ Fin du script de déploiement"
