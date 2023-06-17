#!/bin/bash

# Script de developpement locale
export CONFIG_SERVICE_URI=http://192.168.1.30:8089
mvn package -Dspring.profiles.active=dev
