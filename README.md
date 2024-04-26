# Journo - a pdf creation library converting Freemarker markup to pdf
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo-runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo-runtime)
[![javadoc](https://javadoc.io/badge2/se.alipsa/journo-runtime/javadoc.svg)](https://javadoc.io/doc/se.alipsa/journo-runtime)

Journo is a tool to generate PDF from Freemarker templates i.e. it
creates PDF documents from Freemarker markup.

Usage of this could be as a reporting engine in an application server (Spring Boot, Play, Quarkus etc.) or a
java gui (Swing, JavaFx or SWT).

The freemarker markup should ideally generate xhtml. If it does not you need to convert
the html code into xhtml e.g. using `reportEngine.htmlToXhtml(html)`

This library uses Flying Saucer to generate the PDF so the html produced by the Freemarker
template needs to take into account the xhtml requirements posed by Flying Saucer.
[See the Flying Saucer user guide for details](https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html)

Journo requires JDK 17 or later.

Below is a short introduction, for more comprehensive documentation, see [the wiki](https://github.com/Alipsa/journo/wiki/Journo-Wiki-Home)

Example usage:

```groovy
import se.alipsa.journo.ReportEngine;

ReportEngine engine = new ReportEngine(this, "/templates");

Map<String, Object> data = new HashMap<>();
data.put("user", "Per");

// Render the html using the template and the data
String html = engine.renderHtml("test.ftlh", data);

// Create a pdf file from the html
Path path = Paths.get("test.pdf");
engine.renderPdf(html, path);
```

To use it, add the following dependency to your maven pom.xml (or equivalent for your build system)
```xml
<dependency>
    <groupId>se.alipsa</groupId>
    <artifactId>journo</artifactId>
    <version>0.6.1</version>
</dependency>
```

## Handling images

### Regular images
Regular images should be converted to base64 data url's that are passed in as data.

Assuming you have an image like this in your Freemarker template saved as a file called svgImage.ftlh:
```xhtml
<img src="${alice2}" width="200px" height="300px"/>
```
You can then convert the image to a data url using the Image util:

```groovy
import se.alipsa.journo.ReportEngine;
import se.alipsa.journo.ImageUtil;
import java.util.HashMap;
import java.util.Map;

ReportEngine engine = new ReportEngine(this, "/templates");
Map<String, Object> data = new HashMap<>();

// fetch the image from the classloader, in this case its in src/main/resources/alice2.png
// which is copied to the classes dir and to the root in the jar file
data.put("alice2", ImageUtil.asDataUrl("/alice2.png"));
// Render the html using the template and the data
String html = engine.renderHtml("svgImage.ftlh", data);
```

### SVG images
Svg images are handled automatically using Batik, so you can just insert your
svg image into the Freemarker template and use it. However, you must associate the svg image with a block 
or inline block.

If you are dynamically inserting your svg image, you need to 
[disable autoescaping](https://freemarker.apache.org/docs/dgui_misc_autoescaping.html)

Assuming your Freemarker markup saved as a file called svgImage.ftlh with the following content:

```xhtml
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>SVG image example</title>
    <style>
        svg {display: block;width:400px;height:400px}
    </style>
</head>
<body>
<h1>A big SVG circle</h1>
<div>
    <!-- inserting a svg image directly -->
    <svg xmlns="http://www.w3.org/2000/svg">
        <circle cx="150" cy="65" r="60" stroke="black" stroke-width="3"
                fill="red" />
    </svg>
    <!-- inserting a svg image from the data, must disable auto escaping -->
    ${svgImage?no_esc}
</div>
</body>
</html>
```

You can then do:

```groovy
import se.alipsa.journo.ReportEngine;
import java.util.HashMap;
import java.util.Map;

ReportEngine engine = new ReportEngine(this, "/templates");
Map<String, Object> data = new HashMap<>();
data.put("svgImage", """
        <h2>A big blue circle</h2>
        <svg xmlns="http://www.w3.org/2000/svg">
            <circle cx="150" cy="90" r="80" stroke="black" stroke-width="3"
                    fill="blue" />
        </svg>
    """);

// Create a pdf file:
Path path = Paths.get("svgImage.pdf");
engine.renderPdf("svgImage.ftlh", data, path);
```

## Page structure

A good way to structure your document is to define 3 sections in your page style i.e. for header, footer and content, e.g:

```xhtml
<style>
    div.header {
        display: block;
        position: running(header);
    }

    div.footer {
        display: block;
        position: running(footer);
    }

    div.content {page-break-after: always;}
    div.lastpage {page-break-after: avoid;}

    @page {
        @top-center { content: element(header) }
    }
    @page {
        @bottom-right-corner { content: element(footer) }
    }

    #pagenumber:before {
        content: counter(page);
    }

    #pagecount:before {
        content: counter(pages);
    }

</style>
```

Then, in the body you put div sections for each e.g:
```xhtml
<body>
    <div class="header">Here goes the header text</div>
    <div class="footer" style="">  Page <span id="pagenumber"/> of <span id="pagecount"/> </div>
    <div class="content" id="page1">
        <h1>CHAPTER I</h1>
        <p>
         Some text and images for the first page
        </p>
    </div>
    <div class="content" id="page2">
      <h1>CHAPTER 2</h1>
      <p>
        Some text and images for the second page
      </p>
    </div>
    <div class="lastpage" id="page3">
      <p>
        some text and images for the last page
      </p>
    </div>
</body>
```

## External Resources
External resources, such as an external css must be treated with some care.
If you run Journo in a servlet environment and need to reference an external css
that is not publicly available, you should parameterize the location using 
`getServletContext().getRealPath()`; if you are in Spring Boot you can do
`getClass().getResource("/path/to/resource.css").toExternalForm()`.

For example, in your Freemarker template:
```xhtml
<link rel="stylesheet" href="${externalCssPath}" type="text/css" media="all" />
```

... you then find the css and set the url parameter:
```groovy
String externalCssPath = this.getClass().getResource("/templates/mystyle.css").toExternalForm();
data.put("externalCssPath", externalCssPath);
```

Of course if you either make your css available from some url or put your style inline in the
xhtml document you don't need to do any of this.

## Fonts
Starting with version 0.6.1, Journo now detects and adds declared fonts. I.e. if you do:
```html
<style>

        /* declared fonts are automatically added to the Journo Engine */
        @font-face {
            font-family: "Jersey 25";
            src: url(${jerseyUrl});
        }
</style>
```
And the Jersey font is in src/main/resources/fonts you can insert the location
```groovy
import se.alipsa.journo.ReportEngine;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

ReportEngine engine = new ReportEngine(this, "/templates");
URL urlJersey = getClass().getResource("/fonts/Jersey25-Regular.ttf");
Map<String, Object> data = new HashMap<>();
data.put("jerseyUrl", urlJersey);
byte[] pdf = engine.renderPdf("someReport.ftl", data);

// if you do not want this behavior, but prefer to register fonts "manually"
// with the engine.addFont(urlJersey) globally, you can do 
byte[] pdf2 = engine.renderPdf("someReport.ftl", data, false);
```

Note that currently, declaring fonts in an external css will not result in them being automatically 
loaded. In those cases you must use `engine.addFont(fontPathOrUrl)` prior to calling renderPdf

## Javascript
if you need to use Javascript to manipulate the DOM you must run the html code in a browser (e.g. Javafx WebView)
before rendering the pdf (see [the gmd documentation](https://github.com/Alipsa/gmd/blob/main/README.md) for
an example of using a WebView to do just this). 

## Journo viewer
The [Journo Viewer](viewer/readme.md) is a simple but powerful gui tool to shorten the report creation lifecycle.
You need a JDK 17 or higher with javafx bundled to run it (e.g. the Bellsoft Full JDK distribution)

## License
The journo code is licensed under the MIT license.
Note that it heavily depends on Freemarker and Flying Saucer which are
licenced under the Apache License (Freemarker) and
LGPL (Flying Saucer) respectively.

## 3:rd party libraries used

### Freemarker (https://freemarker.apache.org/)
- Used to create the (x)html
- Apache License Version 2.0

### Flying Saucer (https://github.com/flyingsaucerproject/flyingsaucer)
- Used to create PDFs
- GNU Lesser General Public License, version 2.1 or later

### Jsoup (https://jsoup.org/)
- Used to convert html to xhtml
- MIT license

### Batik (https://xmlgraphics.apache.org/batik/)
- Used to convert SVG to bitmaps
- Apache License, Version 2.0

### SLF4J (https://www.slf4j.org/)
- Used for logging
- MIT license

## Test dependencies

### Junit Jupiter (https://junit.org/junit5/)
- Used for test assertions
- Eclipse Public License 1.0
