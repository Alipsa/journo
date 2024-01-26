package se.alipsa.reportengine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.TimeZone;

public class ReportEngine {

  private final Configuration cfg;
  public ReportEngine() {
    cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
    cfg.setClassForTemplateLoading(this.getClass(), "/templates");
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
    ITextRenderer renderer = new ITextRenderer();
    // Add fonts:
    //FontResolver resolver = iTextRenderer.getFontResolver();
    //renderer.getFontResolver().addFont("MyFont.ttf", true);

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
