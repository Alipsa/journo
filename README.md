# Journo - html to pdf
Journo is a tool to generate PDF from Freemarker templates i.e. it
creates PDF documents from Freemarker markup.

Usage of this could be as a reporting engine in an application server (Spring Boot, Play, Quarkus etc.) or a
java gui (Swing, JavaFx or SWT).

The freemarker markup should ideally generate xhtml. If it does not you need to convert
the html code into xhtml e.g. using `reportEngine.htmlToXhtml(html)`

This library uses Flying Saucer to generate the PDF so the html produced by the Freemarker
template needs to take into account the xhtml requirements posed by Flying Saucer.
[See the Flying Saucer user guide for details](https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html)

Example usage:
```groovy
import se.alipsa.reportengine.ReportEngine;
    
ReportEngine engine = new ReportEngine(this, "/templates");

Map<String, Object> data = new HashMap<>();
data.put("user", "Per");

// Render the html using the template and the data
String html = engine.renderHtml("test.ftlh", data);

// Create a pdf file from the html
Path path = Paths.get("test.pdf");
engine.renderPdf(html, path);
```

## Handling images

### Regular images
Regular images should be converted to base64 data url's that are passed in as data.

Assuming you have an image like this in your Freemarker template:
```xhtml
<img src="${alice2}" width="200px" height="300px"/>
```
You can then convert the image to a data url using the Image util:
```groovy
import se.alipsa.reportengine.ReportEngine;
import java.util.HashMap;
import java.util.Map;

ReportEngine engine = new ReportEngine(this, "/templates");
Map<String, Object> data = new HashMap<>();

// fetch the image from the classloader, in this case its in src/main/resources/alice2.png
// which is copied to the classes dir and to the root in the jar file
data.put("alice2", ImageUtil.asDataUrl("/alice2.png"));
// Render the html using the template and the data
String html = engine.renderHtml("headerFooter.ftlh", data);
```

### SVG images
Svg images are handled automatically using Batik, so you can just insert your
svg image into the Freemarker template and use it. However, you must associate the svg image with a block 
or inline block.

If you are dynamically inserting your svg image, you need to 
[disable autoescaping](https://freemarker.apache.org/docs/dgui_misc_autoescaping.html)

Assuming your Freemarker markup is as follows:

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
import se.alipsa.reportengine.ReportEngine;
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

## Page breaks
## Header and Footer

## License
The htmlToPdf code is licensed under the MIT license.
Note that it heavily depends on Freemarker and Flying Saucer which are
licenced under the Apache License (Freemarker) and
LGPL (Flying Saucer) respectively.

## 3:rd party libraries used:
