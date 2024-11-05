package se.alipsa.journo;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

/**
 * The replacement for the svg with a bitmap image
 */
public class SVGReplacedElement implements ITextReplacedElement {

  private Point location = new Point(0, 0);
  private Document svg;
  private int cssWidth;
  private int cssHeight;

  /**
   * creates a SVGReplacedElement
   *
   * @param svg the svg document
   * @param cssWidth the width of the image
   * @param cssHeight the height of the image
   */
  public SVGReplacedElement(Document svg, int cssWidth, int cssHeight) {
    this.cssWidth = cssWidth;
    this.cssHeight = cssHeight;
    this.svg = svg;
  }

  @Override
  public void detach(LayoutContext c) {
  }

  @Override
  public int getBaseline() {
    return 0;
  }

  @Override
  public int getIntrinsicWidth() {
    return cssWidth;
  }

  @Override
  public int getIntrinsicHeight() {
    return cssHeight;
  }

  @Override
  public boolean hasBaseline() {
    return false;
  }

  @Override
  public boolean isRequiresInteractivePaint() {
    return false;
  }

  @Override
  public Point getLocation() {
    return location;
  }

  @Override
  public void setLocation(int x, int y) {
    this.location.x = x;
    this.location.y = y;
  }

  @Override
  public void paint(RenderingContext renderingContext, ITextOutputDevice outputDevice,
                    BlockBox blockBox) {
    PdfContentByte cb = outputDevice.getWriter().getDirectContent();
    float width = cssWidth / outputDevice.getDotsPerPoint();
    float height = cssHeight / outputDevice.getDotsPerPoint();

    PdfTemplate template = cb.createTemplate(width, height);
    Graphics2D g2d = template.createGraphics(width, height);
    PrintTranscoder prm = new PrintTranscoder();
    TranscoderInput ti = new TranscoderInput(svg);
    prm.transcode(ti, null);
    PageFormat pg = new PageFormat();
    Paper pp = new Paper();
    pp.setSize(width, height);
    pp.setImageableArea(0, 0, width, height);
    pg.setPaper(pp);
    prm.print(g2d, pg, 0);
    g2d.dispose();

    PageBox page = renderingContext.getPage();
    float x = blockBox.getAbsX() + page.getMarginBorderPadding(renderingContext, CalculatedStyle.Edge.LEFT);
    float y = (page.getBottom() - (blockBox.getAbsY() + cssHeight)) + page.getMarginBorderPadding(
        renderingContext, CalculatedStyle.Edge.BOTTOM);
    x /= outputDevice.getDotsPerPoint();
    y /= outputDevice.getDotsPerPoint();

    cb.addTemplate(template, x, y);
  }
}
