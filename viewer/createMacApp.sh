#!/usr/bin/env bash

DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source "$DIR"/build.sh || exit 1

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
cp "$DIR"/target/journo-viewer*.jar "$targetDir"/
cp src/main/assembly/mac/run.sh "$targetDir"/
chmod +x "${MACOS_DIR}/journo"
# cd to the target so we dont have to allow full disk access in Settings -> Privacy and Security
cd "${targetDir}/.." || exit 1
SetFile -a B "${appName}"

cd "$DIR/target" || exit 1
zip -r journo-viewer-mac.zip "${appName}"

echo "Done! Drag the app in $targetDir to your applications folder to install!"