package se.alipsa.reportengine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TimeZone;

public class ReportEngine {

  private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);
  private final Configuration cfg;
  private final ITextRenderer renderer = new ITextRenderer();

  /**
   *
   * @param caller an object of a class that is in the same jar as the report templates
   * @param templatesPath the path to the folder containing reports e.g. if reports are in src/main/resources/templates
   *                      then "/templates" is the templatesPath
   */
  public ReportEngine(Object caller, String templatesPath) {
    cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
    cfg.setClassForTemplateLoading(caller.getClass(), templatesPath);

    // Enable SVG handling
    ChainingReplacedElementFactory chainingReplacedElementFactory = new ChainingReplacedElementFactory();
    // Add the default factory that handles "normal" images to the chain
    chainingReplacedElementFactory.addReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory());
    chainingReplacedElementFactory.addReplacedElementFactory(new SVGReplacedElementFactory());
    renderer.getSharedContext().setReplacedElementFactory(chainingReplacedElementFactory);
  }

  public String renderHtml(String template, Map<String, Object> data) throws IOException, TemplateException {
    Template temp = cfg.getTemplate(template);
    StringWriter sw = new StringWriter();
    temp.process(data, sw);
    return sw.toString();
  }

  public byte[] renderPdf(String template, Map<String, Object> data) throws IOException, TemplateException {
    String html = renderHtml(template, data);
    String xhtml = htmlToXhtml(html);
    return xhtmlToPdf(xhtml);
  }

  public void renderPdf(String template, Map<String, Object> data, Path path) throws IOException, TemplateException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(renderPdf(template, data));
      log.debug("Wrote " + path.toAbsolutePath());
    }
  }

  public void renderPdf(String xhtml, Path path) throws IOException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(xhtmlToPdf(xhtml));
      log.debug("Wrote " + path.toAbsolutePath());
    }
  }

  public void addFont(String fontPath) throws IOException {
    addFont(fontPath, true);
  }

  public void addFont(String fontPath, boolean embedded) throws IOException {
    FontResolver resolver = renderer.getFontResolver();
    // "MyFont.ttf"
    renderer.getFontResolver().addFont(fontPath, embedded);
  }

  public byte[] xhtmlToPdf(String xhtml) throws IOException {
    renderer.setDocumentFromString(xhtml);
    renderer.layout();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      renderer.createPDF(baos);
      return baos.toByteArray();
    }
  }

  private static String htmlToXhtml(String html) {
    Document document = Jsoup.parse(html);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    return document.html();
  }
}
