#!/bin/bash

network_spring="sso_bd"

echo "Affichage de la liste des réseau "

docker network ls

echo "voulez vous afficher le réseau $network_spring ? (y/n)"
read reponse

if [[ $reponse -eq "y" ]]; then
  docker network inspect $network_spring
fi
