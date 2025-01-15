package test.alipsa.journo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import se.alipsa.journo.ImageUtil;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.JournoEngine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ExternalCssTest {

  @Test
  public void testExternalCss() throws JournoException {
    JournoEngine engine = new JournoEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("https://some.url.se/", "Hello world");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));

    String externalCssPath = this.getClass().getResource("/templates/mystyle.css").toExternalForm();
    data.put("externalCssPath", externalCssPath);

    String html = engine.renderHtml("externalCss.ftlh", data);
    // System.out.println(html);

    Path pdfFilePath = Paths.get("target/externalCss.pdf");
    engine.renderPdf("externalCss.ftlh", data, pdfFilePath);
    assertTrue(pdfFilePath.toFile().exists());
  }
}
