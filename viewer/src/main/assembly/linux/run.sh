#!/usr/bin/env bash

JV=17

if command -v java ; then
	javaVersion=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
	if [[ (( $javaVersion -ge $JV )) ]]; then
	  echo "Java $javaVersion OK"
	else
	  echo "Java version $JV or greater required, trying to switch with sdkman"
	  if [[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
	    source "$HOME/.sdkman/bin/sdkman-init.sh"
      jdk=$(sdk list java | grep installed | grep -E "$JV." | head -n 1 | cut -d '|' -f 6 | sed 's/^ *//g')
      jdk=$(echo "$jdk" | xargs)
      sdk use java "${jdk}"
      javaVersion=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
      if [[ (( $javaVersion -ge $JV )) ]]; then
      	  echo "Java $javaVersion OK"
      else
        echo "Failed to switch to java $JV"
	      exit 1
	    fi
	  fi
	fi
else
  echo "Java not found in path"
  read -r
  exit 1
fi

DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$DIR" || exit

JAVA_OPTS="-Xmx8g"

JAR=$(ls -1 -t journo-viewer-*-with-dependencies.jar | head -1)
java $JAVA_OPTS -jar ./$JAR

