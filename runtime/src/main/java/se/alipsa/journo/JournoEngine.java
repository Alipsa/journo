package se.alipsa.journo;

import com.openhtmltopdf.extend.SVGDrawer;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TimeZone;

/**
 * This is the core class of the Journo library, used to create html or pdf output
 */
public class JournoEngine {

  private static final Logger log = LoggerFactory.getLogger(JournoEngine.class);
  private Configuration templateEngineCfg;
  private BatikSVGDrawer svgDrawer;
  private MathMLDrawer mathMLDrawer;

  /**
   * Creates a ReportEngine
   *
   * @param caller an object of a class that is in the same jar as the report templates
   * @param templatesPath the path to the folder containing reports e.g. if reports are in src/main/resources/templates
   *                      then "/templates" is the templatesPath
   */
  public JournoEngine(Object caller, String templatesPath) {
    this(caller.getClass(), templatesPath);
  }

  /**
   * Creates a ReportEngine
   *
   * @param caller a class that is in the same jar as the report templates
   * @param templatesPath the path to the folder containing reports e.g. if reports are in src/main/resources/templates
   *                      then "/templates" is the templatesPath
   */
  public JournoEngine(Class<?> caller, String templatesPath) {
    createGenericFreemarkerConfig();
    templateEngineCfg.setClassForTemplateLoading(caller, templatesPath);
  }

  /**
   * Creates a ReportEngine
   *
   * @param templateDir the dir where your freemarker templates resides
   * @throws IOException if there was some problem accessing the directory
   */
  public JournoEngine(File templateDir) throws IOException {
    createGenericFreemarkerConfig();
    templateEngineCfg.setDirectoryForTemplateLoading(templateDir);
  }

  /**
   * Creates a ReportEngine
   *
   * @param templateLoader a custom TemplateLoader that can handle access to your freemarker templates
   */
  public JournoEngine(TemplateLoader templateLoader) {
    createGenericFreemarkerConfig();
    templateEngineCfg.setTemplateLoader(templateLoader);
  }

  private void createGenericFreemarkerConfig() {
    Version version = Configuration.VERSION_2_3_32;
    templateEngineCfg = new Configuration(version);
    templateEngineCfg.setDefaultEncoding("UTF-8");
    templateEngineCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    templateEngineCfg.setLogTemplateExceptions(false);
    templateEngineCfg.setWrapUncheckedExceptions(true);
    templateEngineCfg.setFallbackOnNullLoopVariable(false);
    templateEngineCfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
    templateEngineCfg.setLocalizedLookup(false);
    templateEngineCfg.setIncompatibleImprovements(version);
    BeansWrapper wrapper = new BeansWrapper(version);
    TemplateModel statics = wrapper.getStaticModels();
    templateEngineCfg.setSharedVariable("statics", statics);
  }

  /**
   * Render the Freemarker template to a html string
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @return html in a string format
   * @throws JournoException if creating the html String fails
   *  or there is some syntax issue in the template
   */
  public String renderHtml(String template, Map<String, Object> data) throws JournoException {
    try {
      Template temp = templateEngineCfg.getTemplate(template);
      StringWriter sw = new StringWriter();
      temp.process(data, sw);
      return sw.toString();
    } catch (IOException | TemplateException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Render the Freemarker template to a pdf file
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @return a PDF in the form of a byte array
   * @throws JournoException if creating the pdf byte array fails or there is some syntax issue in the template
   */
  public byte[] renderPdf(String template, Map<String, Object> data) throws JournoException {
    String html = renderHtml(template, data);
    String xhtml = htmlToXhtml(html);
    return xhtmlToPdf(xhtml);
  }

  /**
   * Render html as a pdf
   *
   * @param html the html (will be converted to xhtml by jsoup) to render
   * @return a PDF in the form of a byte array
   * @throws JournoException if creating the pdf byte array fails
   */
  public byte[] renderPdf(String html) throws JournoException {
    String xhtml = htmlToXhtml(html);
    return xhtmlToPdf(xhtml);
  }

  /**
   * Render the Freemarker template to a pdf file
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @param path the path to the pdf file to create
   * @throws JournoException if creating the byte array or writing the file fails or
   *  there is some syntax issue in the template
   */
  public void renderPdf(String template, Map<String, Object> data, Path path) throws JournoException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(renderPdf(template, data));
      log.debug("Wrote {}", path.toAbsolutePath());
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Render xhtml to a pdf file
   *
   * @param xhtml the xhtml string to render
   * @param path the path to the pdf file to create
   * @throws JournoException if creating the byte array or writing the file fails
   */
  public void renderPdf(String xhtml, Path path) throws JournoException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path))) {
      fos.write(xhtmlToPdf(xhtml));
      log.debug("renderPdf: Wrote {}", path.toAbsolutePath());
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Render xhtml to a pdf file
   *
   * @param xhtml the xhtml string to render
   * @param file the file of to the pdf file to create
   * @throws JournoException if creating the byte array or writing the file fails
   */
  public void renderPdf(String xhtml, File file) throws JournoException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
      fos.write(xhtmlToPdf(xhtml));
      log.debug("renderPdf: Wrote {}", file.getAbsolutePath());
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Render the Freemarker template into a pdf sent to the outputstream specified
   *
   * @param template the Freemarker template relative file path
   * @param data the data to bind to the template when rendering the html
   * @param out the outputstream to render to, closing the stream is the responsibility of the caller
   * @throws JournoException if creating the pdf fails or there is some syntax issue in the template
   */
  public void renderPdf(String template, Map<String, Object> data, OutputStream out) throws JournoException {
    String html = renderHtml(template, data);
    String xhtml = htmlToXhtml(html);
    xhtmlToPdf(xhtml, out);
  }

  /**
   * Convert a xhtml string into a pdf byte array
   *
   * @param xhtml the xhtml string to render
   * @return a PDF in the form of a byte array
   * @throws JournoException if creating the pdf byte array fails
   */
  public synchronized byte[] xhtmlToPdf(String xhtml) throws JournoException {
    try {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        xhtmlToPdf(xhtml, baos);
        return baos.toByteArray();
      }
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Convert a xhtml string into a pdf byte array that will be streamed to the outputstream specified
   *
   * @param xhtml the xhtml string to render
   * @param os the outputstream to write to
   * @throws JournoException if creating the pdf byte array fails
   */
  public synchronized void xhtmlToPdf(String xhtml, OutputStream os) throws JournoException {
    try {
      var jsDoc = Jsoup.parse(xhtml);
      org.w3c.dom.Document doc = new W3CDom().fromJsoup(jsDoc);
      PdfRendererBuilder builder = new PdfRendererBuilder()
          .withW3cDocument(doc, new File(".").toURI().toString())
          .useSVGDrawer(getSvgDrawer())
          .useMathMLDrawer(getMathMLDrawer())
          .toStream(os);
      builder.run();
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }

  private SVGDrawer getMathMLDrawer() {
    if (mathMLDrawer == null) {
      mathMLDrawer = new MathMLDrawer();
    }
    return mathMLDrawer;
  }

  private SVGDrawer getSvgDrawer() {
    if (svgDrawer == null) {
      svgDrawer = new BatikSVGDrawer();
    }
    return svgDrawer;
  }

  /**
   * Allows you to look under the hood of Freemarker
   *
   * @return the Freemarker Configuration
   *
   */
  public Configuration getTemplateEngineConfiguration() {
    return templateEngineCfg;
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
