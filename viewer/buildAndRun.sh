#!/usr/bin/env bash

if command -v jdk21; then
  source jdk21
fi

pushd ../runtime || exit
mvn -DskipTests install
popd || exit

mvn -Pfatjar package || exit 1
jarFilePath=$(ls -t target/journo-viewer-*jar-with-dependencies.jar)
java -Xmx8g -jar "$jarFilePath"