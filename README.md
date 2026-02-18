# Journo - a pdf creation library converting Freemarker markup to pdf
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo-runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.alipsa/journo-runtime)
[![javadoc](https://javadoc.io/badge2/se.alipsa/journo-runtime/javadoc.svg)](https://javadoc.io/doc/se.alipsa/journo-runtime)

Journo is a tool to generate PDF from Freemarker templates i.e. it
creates PDF documents from Freemarker markup.

Usage of this could be as a reporting engine in an application server (Spring Boot, Play, Quarkus etc.) or a
java gui (Swing, JavaFx or SWT).

This library uses [OpenHTMLtoPDF](https://github.com/openhtmltopdf/openhtmltopdf) to generate the PDF so the html produced by the Freemarker
template needs to take into account the xhtml requirements posed.
[See the disclaimer section of OpenHTMLtoPDF](https://github.com/openhtmltopdf/openhtmltopdf?tab=readme-ov-file#disclaimer) for details.

Journo runtime requires JDK 17 or later.

Below is a short introduction, for more comprehensive documentation, see [the wiki](https://github.com/Alipsa/journo/wiki/Journo-Wiki-Home)

Example usage:

```groovy
import se.alipsa.journo.JournoEngine;

JournoEngine engine = new JournoEngine(this, "/templates");

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
    <artifactId>journo-runtime</artifactId>
    <version>0.7.3</version>
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
import se.alipsa.journo.JournoEngine;
import se.alipsa.journo.ImageUtil;
import java.util.HashMap;
import java.util.Map;

JournoEngine engine = new JournoEngine(this, "/templates");
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
import se.alipsa.journo.JournoEngine;
import java.util.HashMap;
import java.util.Map;

JournoEngine engine = new JournoEngine(this, "/templates");
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
Journo detects and adds declared fonts if you specify it. I.e. if you do:
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
import se.alipsa.journo.JournoEngine;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

JournoEngine engine = new JournoEngine(this, "/templates");
URL urlJersey = getClass().getResource("/fonts/Jersey25-Regular.ttf");
Map<String, Object> data = new HashMap<>();
data.put("jerseyUrl", urlJersey);
byte[] pdf = engine.renderPdf("someReport.ftl", data);
```

### Google fonts
The woff2 format, which many of the Google fonts default to is not supported in OpenHTMLtoPDF. 
Ttf fonts works just fine though, so what you can do after finding a nice font you want to use 
is to look up the ttf location of that font e.g. 
[here](https://gist.githubusercontent.com/karimnaaji/b6c9c9e819204113e9cabf290d580551/raw/ed71595a691320ba63e48335c7c77818336cb1c2/GoogleFonts.txt)
and search for the font family. E.g: if google fonts advices you to 
```html
<link href="https://fonts.googleapis.com/css2?family=Sofia&display=swap" rel="stylesheet">
```
You can navigate to the css `https://fonts.googleapis.com/css2?family=Sofia` and you will see that this is a woff2 font
`src: url(https://fonts.gstatic.com/s/sofia/v14/8QIHdirahM3j_su5uI0Orbjl.woff2) format('woff2');`. When looking it up in 
the list of [google ttf fonts](https://gist.githubusercontent.com/karimnaaji/b6c9c9e819204113e9cabf290d580551/raw/ed71595a691320ba63e48335c7c77818336cb1c2/GoogleFonts.txt)
you will find that there is a ttf version here: http://fonts.gstatic.com/s/sofia/v5/Imnvx0Ag9r6iDBFUY5_RaQ.ttf. Armed with that you can declare the font as follows:
```css
        @font-face {
          font-family: "Sofia";
          src: url(http://fonts.gstatic.com/s/sofia/v5/Imnvx0Ag9r6iDBFUY5_RaQ.ttf);
          font-weight: normal;
          font-style: normal;
        }
```

## Javascript
if you need to use Javascript to manipulate the DOM you must run the html code in a browser (e.g. Javafx WebView)
before rendering the pdf (see [the gmd documentation](https://github.com/Alipsa/gmd/blob/main/README.md) for
an example of using a WebView to do just this). 

## Journo viewer
The [Journo Viewer](viewer/readme.md) is a simple but powerful gui tool to create reports. It can significantly shorten the report creation lifecycle.

## Documentation etc.
For more information please see [the wiki](https://github.com/Alipsa/journo/wiki/Journo-Wiki-Home)

## License
The journo code is licensed under the MIT license.
Note that it heavily depends on Freemarker and OpenHTMLtoPDF which are
licenced under the Apache License (Freemarker) and
LGPL (OpenHTMLtoPDF) respectively.

## 3:rd party libraries used

### Freemarker (https://freemarker.apache.org/)
- Used to create the (x)html
- Apache License Version 2.0

### OpenHTMLtoPDF (https://github.com/openhtmltopdf/openhtmltopdf)
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
