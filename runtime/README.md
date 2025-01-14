# Journo Runtime - html to pdf
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo)
[![javadoc](https://javadoc.io/badge2/se.alipsa/journo/javadoc.svg)](https://javadoc.io/doc/se.alipsa/journo)

Journo is a tool to generate PDF from Freemarker templates i.e. it
creates PDF documents from Freemarker markup.

Usage of this could be as a reporting engine in an application server (Spring Boot, Play, Quarkus etc.) or a
java gui (Swing, JavaFx or SWT).

See [the main readme file](../README.md) for more!

## Integration with a Spring-boot application
Typically you would create the journoEngine in a post construct in a Service, e.g:

```groovy
  @PostConstruct
  protected void initialize() {
    reportEngine = new se.alipsa.journo.JournoEngine(this, "/pdfreports");
  }
```
Then you just use reportEngine.renderPdf to generate a byte[], or pass it a path create a file, or an outputstream for 
streaming the report. 

# Building the journo runtime
Journo uses maven. The prerequisites for building the runtime are
1. A JDK  version 17 or later
2. Maven version 3.8.4 installed

Then it's just a matter of `mvn install`!
