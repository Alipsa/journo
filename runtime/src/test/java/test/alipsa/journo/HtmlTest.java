package test.alipsa.journo;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.ImageUtil;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.ReportEngine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HtmlTest {

  @Test
  public void testTest() throws JournoException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));

    String html = engine.renderHtml("test.ftlh", data);
    //System.out.println(html);
    assertTrue(html.contains("Per"));
    assertTrue(html.contains("Fancy stuff"));

    Path path = Paths.get("test.pdf");
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(engine.renderPdf("test.ftlh", data));
      //System.out.println("Wrote " + path.toAbsolutePath());
    }
    File file = path.toFile();
    assertTrue(file.exists());
    file.deleteOnExit();
  }

  public static class Product {
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
