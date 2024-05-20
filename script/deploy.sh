#!/bin/bash

echo "export des variables "
export $(cat .env)

echo "déploiement / update de la stack : $STACK_NAME"
echo "PROFILES : $PROFILES"

# le déploiement sur le nas
if [ "$PROFILES" == "nas" ]; then
  echo "deploy with PROFILES : nas"
  /usr/local/bin/docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME
else
  echo "deploy with PROFILES : $PROFILES"
  docker stack deploy -c ./docker-compose-swarm.yml $STACK_NAME
fi
