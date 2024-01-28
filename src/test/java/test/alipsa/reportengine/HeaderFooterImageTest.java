package test.alipsa.reportengine;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.ImageUtil;
import se.alipsa.journo.ReportEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeaderFooterImageTest {

  @Test
  public void testHeaderFooterImage() throws TemplateException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    String base64Image = ImageUtil.asDataUrl("/images/alice2.png");
    data.put("alice2", base64Image);
    // Render the html using the template and the data
    String html = engine.renderHtml("headerFooter.ftlh", data);
    assertTrue(html.contains(base64Image));
    //System.out.println(html);

    // Create a pdf file from the html
    Path path = Paths.get("headerFooter.pdf");
    engine.renderPdf(html, path);
    File file = path.toFile();
    assertTrue(file.exists());
    file.deleteOnExit();

  }
}
