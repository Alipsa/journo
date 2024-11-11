package se.alipsa.journo;

import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This is the core class of the Journo library, used to create html or pdf output
 */
public class JournoEngine {

  private static final Logger log = LoggerFactory.getLogger(JournoEngine.class);
  private Configuration templateEngineCfg;
  private final Map<String, File> fontsCache = new HashMap<>();
  private final File cacheDir = new File(".journoCache");

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
    cacheDir.mkdirs();
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
    cacheDir.mkdirs();
  }

  /**
   * Creates a ReportEngine
   *
   * @param templateLoader a custom TemplateLoader that can handle access to your freemarker templates
   */
  public JournoEngine(TemplateLoader templateLoader) {
    createGenericFreemarkerConfig();
    templateEngineCfg.setTemplateLoader(templateLoader);
    cacheDir.mkdirs();
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
      log.debug("Wrote " + path.toAbsolutePath());
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
      log.debug("renderPdf: Wrote " + path.toAbsolutePath());
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
      log.debug("renderPdf: Wrote " + file.getAbsolutePath());
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
   * @throws JournoException if parsing goes wrong
   * @return the font paths that were added
   */
  public void addHtmlFonts(String html) throws JournoException {
    try {
      Document doc = Jsoup.parse(html);
      Elements styles = doc.select("style");
      for (Element style : styles) {
        for (Map.Entry<String, String> entry : parseStyle(style.html()).entrySet()) {
          URL fontUrl = new URL(entry.getValue());
          addFont(fontUrl, entry.getKey());
        }
      }
    } catch (MalformedURLException e) {
      throw new JournoException(e);
    }
  }

  /**
   * Add a font
   *
   * @param fontPath the url to the font to add
   * @throws JournoException if the font cannot be found
   */
  public void addFont(URL fontPath, String fontFamily) throws JournoException {
      addFont(getOrDownload(fontPath), fontFamily);
  }

  /**
   * Add a font
   * @param fontPath the path to the font to add
   * @param fontFamily the named alias of the font
   * @throws JournoException if the font cannot be found
   */
  public void addFont(String fontPath, String fontFamily) throws JournoException {
    File file = new File(fontPath);
    if (!file.exists()) {
      throw new JournoException("The file " + fontPath + " does not exist");
    }
    addFont(file, fontFamily);
  }

  /**
   * Add a font
   * @param fontFile the font file to add
   * @param fontFamily the named alias of the font
   * @throws JournoException if the font cannot be found
   */
  public void addFont(File fontFile, String fontFamily) {
      fontsCache.put(fontFamily, fontFile);
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
          .useSVGDrawer(new BatikSVGDrawer())
          .useMathMLDrawer(new MathMLDrawer())
          .toStream(os);
      if (!fontsCache.isEmpty()) {
        fontsCache.forEach((k,v) -> {
          builder.useFont(v, k);
        });
      }
      builder.run();
    } catch (IOException e) {
      throw new JournoException(e);
    }
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

  /**
   * Extract the font urls declared in the style section of the html
   *
   * @param style the style section content
   * @return a Map of fontFamily and url for each font declaration
   * @throws JournoException if parsing failed
   */
  private Map<String, String> parseStyle(String style) throws JournoException {
    try {
      org.w3c.css.sac.InputSource source = new org.w3c.css.sac.InputSource(new java.io.StringReader(style));
      CSSOMParser parser = new CSSOMParser(new SACParserCSS3());

      CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
      Map<String, String> fonts = new HashMap<>();
      CSSRuleList rules = sheet.getCssRules();
      for (int i = 0; i < rules.getLength(); i++) {
        final CSSRule rule = rules.item(i);
        if (CSSRule.FONT_FACE_RULE == rule.getType()) {
          String fontFace = rule.getCssText();
          String fontUrl = null;
          String fontFamily = null;
          if (fontFace.contains("url")) {
            String urlString = fontFace.substring(fontFace.indexOf("url") + 3);
            if (urlString.contains("(") && urlString.contains(")")) {
              urlString = urlString
                  .substring(urlString.indexOf("(") + 1, urlString.indexOf(")"))
                  .trim();
              fontUrl = urlString;
            }
          }
          if (fontFace.contains("font-family")) {
            fontFamily = fontFace.substring(fontFace.indexOf("font-family") + 9);
            fontFamily = fontFamily.replace('"', ' ').trim();
          }
          if (fontFamily != null && fontUrl != null) {
            fonts.put(fontFamily, fontUrl);
          }
        }
      }
      return fonts;
    } catch (IOException | CSSException e) {
      throw new JournoException(e);
    }
  }

  File getOrDownload(URL fontPath) throws JournoException {
    try {
      String fileName = fontPath.getFile();
      if (fileName.contains("/")) {
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      File file = new File(cacheDir, fileName);
      if (!file.exists()) {
        IOUtils.copy(fontPath, file);
      }
      return file;
    } catch (IOException e) {
      throw new JournoException(e);
    }
  }
}
