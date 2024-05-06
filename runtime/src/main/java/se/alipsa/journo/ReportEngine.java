package se.alipsa.journo;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This is the core class of the Journo library, used to create html or pdf output
 */
public class ReportEngine {

  private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);
  private Configuration cfg;
  private final ITextRenderer renderer = new ITextRenderer();
  Set<String> addedFontsCache = new HashSet<>();

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

  /**
   * Creates a ReportEngine
   *
   * @param templateDir the dir where your freemarker templates resides
   * @throws IOException if there was some problem accessing the directory
   */
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
    Version version = Configuration.VERSION_2_3_32;
    cfg = new Configuration(version);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
    BeansWrapper wrapper = new BeansWrapper(version);
    TemplateModel statics = wrapper.getStaticModels();
    cfg.setSharedVariable("statics", statics);
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
   * Render html as a pdf
   *
   * @param html the html (will be converted to xhtml by jsoup) to render
   * @return a PDF in the form of a byte array
   * @throws IOException if creating the pdf byte array fails
   */
  public byte[] renderPdf(String html) throws IOException {
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
   * Render xhtml to a pdf file
   *
   * @param xhtml the xhtml string to render
   * @param file the file of to the pdf file to create
   * @throws IOException if creating the byte array or writing the file fails
   */
  public void renderPdf(String xhtml, File file) throws IOException {
    try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
      fos.write(xhtmlToPdf(xhtml));
      log.debug("Wrote " + file.getAbsolutePath());
    }
  }

  /**
   * Parses the html and adds all declared fonts with an url specified, e.g:
   * <code>
   *    {@literal @}font-face {
   *      font-family: "Jersey 25";
   *      src: url(file:/usr/local/fonts/Jacquard24-Regular.ttf);
   *    }
   * </code>
   * This way, you do not have to add fonts explicitly to the engine but can rely on
   * declaring the in the template only. Note that external stylesheets are not (yet)
   * supported.
   * Note that you can achieve the same result using the -fs-pdf-font-embed: embed; property
   * in the xhtml font-face declaration, e.g:
   * <code>
   * {@literal @}font-face {
   *   font-family: "Jersey 25";
   *   src: url(file:/usr/local/fonts/Jacquard24-Regular.ttf);
   *   -fs-pdf-font-embed: embed;
   * }
   * </code>
   *
   * @param html the html to parse
   * @throws IOException if parsing goes wrong
   * @return the font paths that were added
   */
  public Set<String> addHtmlFonts(String html) throws IOException {
    Document doc = Jsoup.parse(html);
    Set<String> fontUrls = new HashSet<>();
    Elements styles = doc.select("style");
    for (Element style : styles) {
      for (String fontUrl : parseStyle(style.html())) {
        if (addedFontsCache.add(fontUrl)) {
          fontUrls.add(fontUrl);
        }
      }
    }
    for (String fontUrl : fontUrls) {
      addFont(fontUrl);
    }
    return fontUrls;
  }

  /**
   * Add a font
   *
   * @param fontPath the path to the font to add
   * @throws IOException if the font cannot be found
   */
  public void addFont(URL fontPath) throws IOException {
    addFont(fontPath.toExternalForm());
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
   * Allows you to look under the hood of the PDF generator
   *
   * @return the underlying ITextRenderer
   */
  public ITextRenderer getRenderer() {
    return renderer;
  }

  /**
   * Allows you to look under the hood of Freemarker
   *
   * @return the Freemarker Configuration
   *
   */
  public Configuration getFreemarkerConfiguration() {
    return cfg;
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

  /**
   * Extract the font urls declared in the style section of the html
   *
   * @param style the style section content
   * @return a list of urls for each font declaration
   * @throws IOException if parsing failed
   */
  private List<String> parseStyle(String style) throws IOException {
    org.w3c.css.sac.InputSource source = new org.w3c.css.sac.InputSource(new java.io.StringReader(style));
    CSSOMParser parser = new CSSOMParser(new SACParserCSS3());

    CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
    List<String> fontUrls = new ArrayList<>();
    CSSRuleList rules = sheet.getCssRules();
    for (int i = 0; i < rules.getLength(); i++) {
      final CSSRule rule = rules.item(i);
      if (CSSRule.FONT_FACE_RULE == rule.getType()) {
        String fontFace = rule.getCssText();
        if (fontFace.contains("url")) {
          String urlString = fontFace.substring(fontFace.indexOf("url") + 3);
          if (urlString.contains("(") && urlString.contains(")")) {
            urlString = urlString
                .substring(urlString.indexOf("(")+1, urlString.indexOf(")"))
                .trim();
            fontUrls.add(urlString);
          }
        }
      }
    }
    return fontUrls;
  }
}
