package test.alipsa.journo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.JournoEngine;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FontTest {

  @Test
  public void testLoadFont() throws JournoException {
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
  }
}
