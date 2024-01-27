package test.alipsa.reportengine;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import se.alipsa.reportengine.ReportEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SvgImageTest {

  @Test
  public void testSVGIMage() throws TemplateException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();

    data.put("svgImage", """
            <h2>A big blue circle</h2>
            <svg xmlns="http://www.w3.org/2000/svg">
                <circle cx="150" cy="90" r="80" stroke="black" stroke-width="3"
                        fill="blue" />
            </svg>
        """);

    // Render the html using the template and the data
    String html = engine.renderHtml("svgImage.ftlh", data);
    System.out.println(html);

    // Create a pdf file from the html
    Path path = Paths.get("svgImage.pdf");
    engine.renderPdf("svgImage.ftlh", data, path);
    System.out.println("Wrote " + path.toAbsolutePath());
  }
}
