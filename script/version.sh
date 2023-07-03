#!/bin/bash

# Permet de remonter dans la hiérarchie des répertoires
dirname_remonte() {

  # donne la base path à version.sh
  base=$(readlink -f "$0")

  if [ -z "$1" ]; then
    echo "Variable vide "
  else
    for ((i = 0; i <= $1; i++)); do
      base=$(dirname $base)
    done

    echo "$base"
  fi
}
### init variable path
# base du projet
path_msarticle=$(dirname_remonte 1)
#echo "path_msarticle: $path_msarticle"

# path .env
path_env="$path_msarticle/docker/.env"
#echo "path_docker : $path_env"

# Vérifier si xmlstarlet est installé
if ! command -v xmlstarlet &>/dev/null; then
  echo "Installation de xmlstarlet..."
  sudo apt-get update && apt-get install -y xmlstarlet
fi

# Récupérer la version du projet à partir du fichier pom.xml
PROJECT_VERSION=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" -t -v "//x:project/x:version" "$path_msarticle/pom.xml")

# renvoi la valeur
echo "$PROJECT_VERSION"

# Vérification si le fichier de path_env existe
if [ -f "$path_env" ]; then
  # Extraction de la valeur actuelle de IP_DEV
  version=$(grep -oP '^version=\K.*' "$path_env" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  echo "Valeur actuelle de la version : ${version}"
else
  echo "Le fichier de path_env $path_env n'existe pas."
  exit 1 # renvoy false
fi

# Vérification des permissions d'écriture sur le fichier
if [ ! -w "$path_env" ]; then
  echo "Impossible d'écrire dans le fichier $path_env. Vérifiez les permissions."
  exit 1 # renvoy false
fi

# Copie de la chaîne de caractères au bon emplacement
awk -v str="$PROJECT_VERSION" 'BEGIN {FS=OFS="="} $1=="version" && NF==2 {$2=str; copied=1} 1;
END {if (!copied) print "version="str}' "$path_env" >temp_file && mv temp_file "$path_env"

# Vérification de la copie
if grep -q "^version=$PROJECT_VERSION$" "$path_env"; then
  echo "La variable a été initialisée avec succès."
  exit 0 # renvoy true
else
  echo "Erreur lors de la copie de la chaîne dans le fichier $path_env."

  exit 1 # renvoy false
fi
