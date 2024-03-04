#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "${DIR}" || exit 1

LAUNCHER=~/.local/share/applications/journo.desktop
{
echo "[Desktop Entry]
Name=Gade
Exec=${DIR}/run.sh
Comment=Journo, a PDF Report Development Environment
Terminal=false
Icon=${DIR}/journo-rounded.png
Type=Application
Categories=Development"
} > ${LAUNCHER}

chmod +x run.sh
chmod +x ${LAUNCHER}
if [[ -f ~/Desktop/journo.desktop ]]; then
  rm ~/Desktop/journo.desktop
fi
ln -s ${LAUNCHER} ~/Desktop/journo.desktop

echo "Launcher shortcuts created!"