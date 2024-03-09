#!/usr/bin/env bash

if command -v jdk21; then
  source jdk21
fi

mvn package || exit 1
java -jar target/journo-viewer-0.6.0-SNAPSHOT.jar