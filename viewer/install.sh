#!/usr/bin/env bash
# Exit immediately on any failure
set -e
SCRIPTDIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPTDIR"
installDir=${1:-$HOME/programs/}
pushd ../runtime
mvn install
popd
source ./createApp.sh skipInstructions
cd "$SCRIPTDIR"
unzip -o target/journo-viewer.zip -d "$installDir"
echo "installed!"