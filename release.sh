#!/usr/bin/env bash

if command -v jdk21; then
  . jdk21
fi
mvn -Prelease clean site deploy
echo "Now build the fatjars for upload to github"
mvn -DskipTests -Pfatjar -q package
echo "Released and ready for manual release at github!"