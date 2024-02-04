package se.alipsa.journo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TimeZone;

/**
 * THis is the core class of the Journo library, used to create output
 */
public class ReportEngine {

  private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);
  private Configuration cfg;
  private final ITextRenderer renderer = new ITextRenderer();

  /**
   * Creates a ReportEngine
   *
   * @param caller an object of a class that is in the same jar as the report templates
   * @param templatesPath the path to the folder containing reports e.g. if reports are in src/main/resources/templates
   *                      then "/templates" is the templatesPath
   */
  public ReportEngine(Object caller, String templatesPath) {
    createGenericConfig();
    cfg.setClassForTemplateLoading(caller.getClass(), templatesPath);
    configureRenderers();
  }

  public ReportEngine(File templateDir) throws IOException {
    createGenericConfig();
    cfg.setDirectoryForTemplateLoading(templateDir);
    configureRenderers();
  }

  private void configureRenderers() {
    // Enable SVG handling
    ChainingReplacedElementFactory chainingReplacedElementFactory = new ChainingReplacedElementFactory();
    // Add the default factory that handles "normal" images to the chain
    chainingReplacedElementFactory.addReplacedElementFactory(renderer.getSharedContext().getReplacedElementFactory());
    chainingReplacedElementFactory.addReplacedElementFactory(new SVGReplacedElementFactory());
    renderer.getSharedContext().setReplacedElementFactory(chainingReplacedElementFactory);
  }

  private void createGenericConfig() {
    cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
  }

                      /**
   * Render the Freemarker template to a html string
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @return html in a string format
   * @throws IOException if creating the html String fails
   * @throws TemplateException it there is some syntax issue in the template
   */
  public String renderHtml(String template, Map<String, Object> data) throws IOException, TemplateException {
    Template temp = cfg.getTemplate(template);
    StringWriter sw = new StringWriter();
    temp.process(data, sw);
    return sw.toString();
  }

  /**
   * Render the Freemarker template to a pdf file
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @return a PDF in the form of a byte array
   * @throws IOException if creating the pdf byte array fails
   * @throws TemplateException it there is some syntax issue in the template
   */
  public byte[] renderPdf(String template, Map<String, Object> data) throws IOException, TemplateException {
    String html = renderHtml(template, data);
    String xhtml = htmlToXhtml(html);
    return xhtmlToPdf(xhtml);
  }

  /**
   * Render the Freemarker template to a pdf file
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @param path the path to the pdf file to create
   * @throws IOException if creating the byte array or writing the file fails
   * @throws TemplateException it there is some syntax issue in the template
   */
  public void renderPdf(String template, Map<String, Object> data, Path path) throws IOException, TemplateException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(renderPdf(template, data));
      log.debug("Wrote " + path.toAbsolutePath());
    }
  }

  /**
   * Render xhtml to a pdf file
   *
   * @param xhtml the xhtml string to render
   * @param path the path to the pdf file to create
   * @throws IOException if creating the byte array or writing the file fails
   */
  public void renderPdf(String xhtml, Path path) throws IOException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(xhtmlToPdf(xhtml));
      log.debug("Wrote " + path.toAbsolutePath());
    }
  }

  /**
   * Add a font
   *
   * @param fontPath the path to the font to add
   * @throws IOException if the font cannot be found
   */
  public void addFont(String fontPath) throws IOException {
    addFont(fontPath, true);
  }

  /**
   * Add a font
   * @param fontPath the path to the font to add
   * @param embedded whether to embed the font in the pdf or not
   * @throws IOException if the font cannot be found
   */
  public void addFont(String fontPath, boolean embedded) throws IOException {
    // "MyFont.ttf"
    renderer.getFontResolver().addFont(fontPath, embedded);
  }

  /**
   * Convert a xhtml string into a pdf byte array
   *
   * @param xhtml the xhtml string to render
   * @return a PDF in the form of a byte array
   * @throws IOException if creating the pdf byte array fails
   */
  public byte[] xhtmlToPdf(String xhtml) throws IOException {
    renderer.setDocumentFromString(xhtml);
    renderer.layout();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      renderer.createPDF(baos);
      return baos.toByteArray();
    }
  }

  /**
   *
   * @param html the html string to convert to xhtml
   * @return a xhtml version of the html provided
   */
  private static String htmlToXhtml(String html) {
    Document document = Jsoup.parse(html);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    return document.html();
  }
}
