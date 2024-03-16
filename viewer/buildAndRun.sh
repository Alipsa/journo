#!/usr/bin/env bash

if command -v jdk21; then
  source jdk21
fi

JAVA_OPTS="-Xmx8g -Xdock:name=journo -Xdock:icon=./src/main/assembly/mac/journo.icns"
mvn package || exit 1
java $JAVA_OPTS -jar target/journo-viewer-0.6.0-SNAPSHOT.jar