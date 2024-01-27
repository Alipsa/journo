package test.alipsa.reportengine;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import se.alipsa.reportengine.ImageUtil;
import se.alipsa.reportengine.ReportEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HeaderFooterImageTest {

  @Test
  public void testHeaderFooterImage() throws TemplateException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    data.put("alice2", ImageUtil.asDataUrl("/images/alice2.png"));
    // Render the html using the template and the data
    String html = engine.renderHtml("headerFooter.ftlh", data);
    System.out.println(html);

    // Create a pdf file from the html
    Path path = Paths.get("headerFooter.pdf");
    engine.renderPdf(html, path);
    System.out.println("Wrote " + path.toAbsolutePath());
  }
}
