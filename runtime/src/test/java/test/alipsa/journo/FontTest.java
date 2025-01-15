package test.alipsa.journo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.JournoEngine;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FontTest {

  @Test
  public void testLoadFont() throws JournoException, IOException {
    JournoEngine engine = new JournoEngine(this, "/templates");
    URL urlJersey = getClass().getResource("/fonts/Jersey25-Regular.ttf");
    URL urlJacquard = getClass().getResource("/fonts/Jacquard24-Regular.ttf");

    Map<String, Object> data = new HashMap<>();
    data.put("jerseyUrl", urlJersey);
    data.put("jacquardUrl", urlJacquard);
    String xhtml = engine.renderHtml("font.ftl", data);
    assertNotNull(xhtml);
    byte[] pdf = engine.renderPdf(xhtml);
    assertNotNull(pdf);
    //File htmlFile = File.createTempFile("font", ".html");
    //FileUtils.writeStringToFile(htmlFile, xhtml);
    //System.out.println("Wrote " + htmlFile.getAbsolutePath());
    File file = new File("target/testLoadFont.pdf");
    engine.renderPdf(xhtml, file);
    System.out.println("Wrote " + file.getAbsolutePath());

    // Make sure the fonts are embedded in the PDF
    Map<String, FontInfo> fonts = new HashMap<>();
    try(PDDocument pdDocument = Loader.loadPDF(file)) {
      var page = pdDocument.getPage(0);
      PDResources pageResources = page.getResources();
      for (var fontName : page.getResources().getFontNames()) {
        PDFont font = pageResources.getFont(fontName);
        //System.out.println(font.getName() + ", embedded: " + font.isEmbedded());
        if (font.getName().contains("Sofia")) {
          fonts.put("Sofia", new FontInfo(font.getName(), font.isEmbedded()));
        } else if (font.getName().contains("Jersey25")) {
          fonts.put("Jersey25", new FontInfo(font.getName(), font.isEmbedded()));
        } else if (font.getName().contains("Jacquard24")) {
          fonts.put("Jacquard24", new FontInfo(font.getName(), font.isEmbedded()));
        }
      }
    }
    assertTrue(fonts.containsKey("Sofia"), "Sofia Font is missing");
    assertTrue(fonts.get("Sofia").embedded, "Sofia font is not embedded");
    assertTrue(fonts.containsKey("Jersey25"), "Jersey25 Font is missing");
    assertTrue(fonts.get("Jersey25").embedded, "Jersey25 font is not embedded");
    assertTrue(fonts.containsKey("Jacquard24"), "Jacquard24 Font is missing");
    assertTrue(fonts.get("Jacquard24").embedded, "Jacquard24 font is not embedded");
  }

  class FontInfo {
    public String name;
    public Boolean embedded;

    public FontInfo(String name, Boolean isEmbedded) {
      this.name = name;
      this.embedded = isEmbedded;
    }
  }
}
