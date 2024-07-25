package test.alipsa.journo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.ImageUtil;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.ReportEngine;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class OutputTest {

  static ReportEngine engine = new ReportEngine(OutputTest.class, "/templates");

  @BeforeAll
  public static void warmup() {
    Map<String, Object> data = new HashMap<>();
    data.put("user", "");
    Product prod = new Product("", "");
    data.put("latestProduct", prod);
    data.put("image", "");
    try {
      engine.renderPdf("test.ftlh", data);
    } catch (JournoException e) {
      fail("Warmup failed");
    }
  }

  @Test
  public void TestRenderToHtml() throws JournoException {
    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));

    String html = engine.renderHtml("test.ftlh", data);
    //System.out.println(html);
    assertTrue(html.contains("Per"));
    assertTrue(html.contains("Fancy stuff"));
  }

  @Test
  public void testRenderToFile() throws JournoException {
    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));

    Path path = Paths.get("test.pdf");
    engine.renderPdf("test.ftlh", data, path);

    File file = path.toFile();
    assertTrue(file.exists(), "File not found");
    assertTrue(file.length() > 0, "File length should be greater than 0");
    file.deleteOnExit();
  }

  @Test
  public void testRenderToByteArray() throws JournoException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));
    byte[] bytes = engine.renderPdf("test.ftlh", data);
    assertTrue(bytes.length > 0, "Byte array length should be greater than 0");
  }

  @Test
  public void testRenderToStream() throws JournoException, IOException {
    ReportEngine engine = new ReportEngine(this, "/templates");

    Map<String, Object> data = new HashMap<>();
    data.put("user", "Per");
    Product prod = new Product("http://some.url.se/", "Fancy stuff");
    data.put("latestProduct", prod);

    data.put("image", ImageUtil.asDataUrl("/images/1e.jpg"));
    try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      engine.renderPdf("test.ftlh", data, out);
      assertTrue(out.toByteArray().length > 0, "Byte array length should be greater than 0");
    }
  }
}
