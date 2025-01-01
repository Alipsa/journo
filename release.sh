#!/usr/bin/env bash

if command -v jdk21; then
  . jdk21
fi
mvn -Prelease clean site deploy