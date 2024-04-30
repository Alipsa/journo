#!/usr/bin/env bash

DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source "$DIR"/build.sh

appName="journoViewer"
targetDir="$DIR/target/${appName}"
mkdir -p "$targetDir"

echo "creating windows app"
cd "$DIR" || exit
cp target/journo-viewer*.jar "$targetDir"/
cp src/main/assembly/win/* "$targetDir"/
cp src/main/assembly/linux/run.sh "$targetDir"/
cp src/main/resources/journo-rounded.* "$targetDir"/
chmod +x "$targetDir"/*.sh

echo "Done! Drag the $appName folder in $targetDir to your applications folder and run createShortcut.ps1 to install!"

