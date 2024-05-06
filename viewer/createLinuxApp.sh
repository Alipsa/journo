#!/usr/bin/env bash

DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source "$DIR"/build.sh

appName="journoViewer"
targetDir="$DIR/target/${appName}"
mkdir -p "$targetDir"

echo "creating linux app"
cd "$DIR" || exit
cp target/journo-viewer*.jar "$targetDir"/
cp src/main/assembly/linux/* "$targetDir"/
cp src/main/resources/journo-rounded.png "$targetDir"/
chmod +x "$targetDir"/*.sh

cd "$DIR/target" || exit 1
zip -r journo-viewer-linux.zip "${appName}"

echo "Done! Drag the $appName folder in $targetDir to your applications folder and run createLauncher.sh to install!"

