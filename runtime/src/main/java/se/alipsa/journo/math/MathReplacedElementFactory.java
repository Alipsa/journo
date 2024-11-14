package se.alipsa.journo.math;

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
public class MathReplacedElementFactory implements ReplacedElementFactory {

  /**
   * default ctor
   */
  public MathReplacedElementFactory() {
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
                                               UserAgentCallback uac, int cssWidth, int cssHeight) {
    Element element = box.getElement();
    if (element != null) {
      System.out.println("checking " + element.getNodeName());
      if ("math".equals(element.getNodeName())) {
        System.out.println("Rendering math ml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;

        try {
          documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          throw new RuntimeException(e);
        }
        Document mathDocument = documentBuilder.newDocument();
        Element mathElement = (Element) mathDocument.importNode(element, true);
        mathDocument.appendChild(mathElement);
        return new MathReplacedElement(mathDocument, cssWidth, cssHeight);
      }
    }
    return null;
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