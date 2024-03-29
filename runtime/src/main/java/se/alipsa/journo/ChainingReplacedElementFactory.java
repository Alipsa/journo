package se.alipsa.journo;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A ReplacedElementFactory that contains a chain of ReplacedElementFactory objects
 */
public class ChainingReplacedElementFactory implements ReplacedElementFactory {

  /**
   * Default ctor
   */
  public ChainingReplacedElementFactory() {
    // Default ctor
  }
  private List<ReplacedElementFactory> replacedElementFactories = new ArrayList<>();

  /**
   * Add a ReplacedElementFactory to the chain
   *
   * @param replacedElementFactory the ReplacedElementFactory to add
   */
  public void addReplacedElementFactory(ReplacedElementFactory replacedElementFactory) {
    replacedElementFactories.add(0, replacedElementFactory);
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
                                               UserAgentCallback uac, int cssWidth, int cssHeight) {
    for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
      ReplacedElement element = replacedElementFactory
          .createReplacedElement(c, box, uac, cssWidth, cssHeight);
      if(element != null) {
        return element;
      }
    }
    return null;
  }

  @Override
  public void reset() {
    for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
      replacedElementFactory.reset();
    }
  }

  @Override
  public void remove(Element e) {
    for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
      replacedElementFactory.remove(e);
    }
  }

  @Override
  public void setFormSubmissionListener(FormSubmissionListener listener) {
    for(ReplacedElementFactory replacedElementFactory : replacedElementFactories) {
      replacedElementFactory.setFormSubmissionListener(listener);
    }
  }
}
