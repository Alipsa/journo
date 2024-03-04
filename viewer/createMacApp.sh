#!/usr/bin/env bash

DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

if command -v jdk21; then
  source jdk21
fi

if command -v java ; then
	javaVersion=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
	if [[ (( $javaVersion -ge 17 )) ]]; then
	  echo "Java $javaVersion OK"
	else
	  echo "Java version 17 or greater required"
	  exit 1
	fi
else
  echo "Java not found in path"
  exit 1
fi

cd "$DIR" || exit
mvn clean package

appName="journo.app"
targetDir="$DIR/target/${appName}"
mkdir -p "$targetDir"
echo "creating mac app"
CONTENT_DIR="${targetDir}/Contents"
MACOS_DIR="${CONTENT_DIR}/MacOS"
RESOURCE_DIR="${CONTENT_DIR}/Resources"
mkdir -p "$MACOS_DIR"
mkdir -p "$RESOURCE_DIR"
cp "src/main/assembly/mac/Info.plist" "$CONTENT_DIR/"
cp "src/main/assembly/mac/journo.icns" "${RESOURCE_DIR}/"
cp "src/main/assembly/mac/journo" "${MACOS_DIR}/"
chmod +x "${MACOS_DIR}/journo"
# cd to the target so we dont have to allow full disk access in Settings -> Privacy and Security
cd "${targetDir}/.." || exit 1
SetFile -a B "${appName}"
cp "$DIR"/target/journo-viewer*.jar $targetDir/
cp "$DIR"/run.sh $targetDir/
echo 'Done!'