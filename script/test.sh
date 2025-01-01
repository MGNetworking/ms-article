#!/bin/bash

echo Les tests unitaire classique avec le profile spring  dev
mvn clean test -Dspring.profiles.active=dev

echo Les test unitaire end t end avec le profile spring dev
mvn clean verify -P e2e -Dspring.profiles.active=dev

echo Les test unitaire d\'integration avec le profile spring dev
mvn clean verify -P integration -Dspring.profiles.active=dev

