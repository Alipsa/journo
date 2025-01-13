#!/usr/bin/env bash

if command -v jdk21; then
  source jdk21
fi

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  OS=linux
elif [[ "$OSTYPE" == "darwin"* ]]; then
  OS=mac
else
  # msys, cygwin, win32
  OS=win
fi

JAVA_OPTS="-Xmx8g"

if [[ "${OS}" == "mac" ]]; then
  JAVA_OPTS="$JAVA_OPTS -Xdock:name=journo -Xdock:icon=./src/main/assembly/mac/journo.icns"
fi

pushd ../runtime || exit
mvn -DskipTests install
popd || exit

mvn -Pfatjar package || exit 1
jarFilePath=$(ls -t target/journo-viewer-*jar-with-dependencies.jar)
java $JAVA_OPTS -jar "$jarFilePath"