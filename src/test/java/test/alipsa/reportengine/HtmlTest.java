package test.alipsa.reportengine;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import se.alipsa.reportengine.ReportEngine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HtmlTest {

  @Test
  public void testTest() throws TemplateException, IOException {
    ReportEngine engine = new ReportEngine();

    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    String html = engine.renderHtml("test.ftlh", data);
    System.out.println(html);

    Path path = Paths.get("test.pdf");
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(engine.renderPdf("test.ftlh", data));
      System.out.println("Wrote " + path.toAbsolutePath());
    }

  }

  public class Product {
    private String url;
    private String name;

    public Product() {}
    public Product(String url, String name) {
      this.url = url;
      this.name = name;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
