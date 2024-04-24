#!/usr/bin/env bash
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

cd "$DIR" || exit 1
mvn clean package || exit 1