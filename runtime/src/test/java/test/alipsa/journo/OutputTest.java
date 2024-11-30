package test.alipsa.journo;

import net.sourceforge.jeuclid.DOMBuilder;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;
import net.sourceforge.jeuclid.layout.JEuclidView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import se.alipsa.journo.ImageUtil;
import se.alipsa.journo.JournoException;
import se.alipsa.journo.JournoEngine;
import se.alipsa.journo.MathReplacedElement;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class OutputTest {

  static JournoEngine engine = new JournoEngine(OutputTest.class, "/templates");

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
    JournoEngine engine = new JournoEngine(this, "/templates");

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
    JournoEngine engine = new JournoEngine(this, "/templates");

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

  @Test
  void testMathMlToPdf() throws JournoException {
    var pdfFile = Paths.get("target/mathml.pdf");
    String html = engine.renderHtml("mathml.ftlh", new HashMap<>());
    System.out.println(html);
    engine.renderPdf(html, pdfFile);
    assertTrue(pdfFile.toFile().exists());
    System.out.println("Wrote " + pdfFile.toFile().getAbsolutePath());
  }

  @Test
  void testMathMlRendering() throws JournoException, ParserConfigurationException, IOException, SAXException {
    String ml = """
        <math>
        	<apply>
        		<plus/>
        		<apply>
        			<times/>
        			<ci>a</ci>
        			<apply>
        				<power/>
        				<ci>x</ci>
        				<cn>2</cn>
        			</apply>
        		</apply>
        		<apply>
        			<times/>
        			<ci>b</ci>
        			<ci>x</ci>
        		</apply>
        		<ci>c</ci>
        	</apply>
        </math>
        """;
    var documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setValidating(false);
    Document doc = documentBuilderFactory.newDocumentBuilder().parse(
        new InputSource(new StringReader(ml)));

    var jMathDoc = DOMBuilder.getInstance().createJeuclidDom(doc);
    var context = new MathReplacedElement.MathLayoutContext();
    var view = new JEuclidView(jMathDoc, context, null);
    File file = new File("target/mathml.png");
    Converter.getInstance().convert(doc.getDocumentElement(), file,  "image/png", context);
    /*BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    view.draw(g, 0, 0);

    ImageIO.write(img, "png", file);*/

  }
}
