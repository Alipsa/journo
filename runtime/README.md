# Journo - html to pdf
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo)
[![javadoc](https://javadoc.io/badge2/se.alipsa/journo/javadoc.svg)](https://javadoc.io/doc/se.alipsa/journo)

Journo is a tool to generate PDF from Freemarker templates i.e. it
creates PDF documents from Freemarker markup.

Usage of this could be as a reporting engine in an application server (Spring Boot, Play, Quarkus etc.) or a
java gui (Swing, JavaFx or SWT).

See [the main readme file](../README.md) for more!

# Building the journo runtime
Journo uses maven. The prerequisites for building are
1. A JDK  version 17 or later
2. Maven version 3.8.4 installed

Then it's just a matter of `mvn install`!
