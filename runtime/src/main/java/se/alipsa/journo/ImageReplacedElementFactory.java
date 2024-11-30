package se.alipsa.journo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Replace the svg tags
 */
public class ImageReplacedElementFactory implements ReplacedElementFactory {

  /**
   * default ctor
   */
  public ImageReplacedElementFactory() {
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
                                               UserAgentCallback uac, int cssWidth, int cssHeight) {
    Element element = box.getElement();
    if (element != null) {
      System.out.println("Checking " + element.getNodeName());
      if ("svg".equals(element.getNodeName())) {
        Document svgDocument = createDocumentFromPart(element);
        return new SVGReplacedElement(svgDocument, cssWidth, cssHeight);
      }
      if ("math".equals(element.getNodeName())) {
        Document mathDocument = createDocumentFromPart(element);
        return new MathReplacedElement(mathDocument, cssWidth, cssHeight);
      }
    }
    return null;
  }

  private static Document createDocumentFromPart(Element element) {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder;

    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    Document document = documentBuilder.newDocument();
    Element svgElement = (Element) document.importNode(element, true);
    document.appendChild(svgElement);
    return document;
  }

  @Override
  public void reset() {
  }

  @Override
  public void remove(Element e) {
  }

  @Override
  public void setFormSubmissionListener(FormSubmissionListener listener) {
  }
}