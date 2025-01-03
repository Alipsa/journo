#!/usr/bin/env bash
# Exit immediately on any failure
set -e
SCRIPTDIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPTDIR"
defaultInstallDir=$HOME/programs/
case $OSTYPE in darwin*) defaultInstallDir=$HOME/Applications ;; esac
installDir=${1:-$defaultInstallDir}
mvn install
pushd viewer
source ./createApp.sh skipInstructions
unzip -o "$SCRIPTDIR/viewer/target/journo-viewer.zip" -d "$installDir"
popd
echo "installed!"