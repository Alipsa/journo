#!/usr/bin/env bash

if command -v jdk17; then
  . jdk17
fi
mvn -Prelease clean site deploy