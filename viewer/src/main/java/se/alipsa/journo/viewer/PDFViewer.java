package se.alipsa.journo.viewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PDFViewer extends Pagination {

  private byte[] content;
  private final JournoViewer gui;

  public PDFViewer(JournoViewer gui) {
    this.gui = gui;
  }

  public void load(File pdfFile) throws IOException {
    load(Files.readAllBytes(pdfFile.toPath()));
  }

  public void load(byte[] content) throws IOException {
    PDDocument document = Loader.loadPDF(content);
    PDFRenderer renderer = new PDFRenderer(document);
    load(document, renderer);
    this.content = content;
  }

  private void load(PDDocument document, PDFRenderer renderer) {
    this.setPageCount(document.getNumberOfPages());
    this.setMaxPageIndicatorCount(3);
    this.setPageFactory((pageIndex) -> {
      BufferedImage image;
      ImageView imageView;
      try {
        image = renderer.renderImage(pageIndex);
        imageView = new ImageView(SwingFXUtils.toFXImage(image, null));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return imageView;
    });
  }

  public byte[] getContent() {
    return content;
  }
}
