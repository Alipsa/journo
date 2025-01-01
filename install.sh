#!/usr/bin/env bash
# Exit immediately on any failure
set -e
SCRIPTDIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPTDIR"
installDir=${1:-$HOME/programs/}
mvn install
pushd viewer
source ./createApp.sh skipInstructions
unzip -o "$SCRIPTDIR/viewer/target/journo-viewer.zip" -d "$installDir"
popd
echo "installed!"