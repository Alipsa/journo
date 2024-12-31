#!/usr/bin/env bash

if command -v java ; then
	javaVersion=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
	if [[ (( $javaVersion -ge 21 )) ]]; then
	  echo "Java $javaVersion OK"
	else
	  echo "Java version 21 or greater required"
	  if command -v jdk21; then
      source jdk21
    elif command -v jdk23; then
      source jdk23
    fi
	  exit 1
	fi
else
  echo "Java not found in path"
  exit 1
fi

cd "$DIR" || exit 1
mvn clean package || exit 1