package test.alipsa.journo.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.jupiter.api.Test;
import se.alipsa.journo.viewer.SvgImageExporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SvgExporterTest {

  @Test
  public void testExportToPng() throws IOException, TranscoderException {
    File file128 = File.createTempFile("testExportToPng", ".png");
    String svg = """
        <svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
          <rect x="0" y="0" width="512" height="512" fill="none"/>
          <rect x="16" y="16" width="480" height="480" fill="white" stroke-width="1" stroke="white" rx="5" ry="5"/>
          <text x="90" y="110" fill="#4d4d4d" font-size="80">J o u r n o</text>
          <line x1="50" y1="100" x2="50" y2="480" stroke="#4d4d4d" stroke-width="3"/>
          <line x1="50" y1="480" x2="480" y2="480" stroke="#4d4d4d" stroke-width="3"/>
          <rect x="80" y="290" width="70" height="180" fill="#0474e1" rx="5" ry="5"/>
          <rect x="180" y="200" width="70" height="270" fill="#007f0e" rx="5" ry="5"/>
          <rect x="280" y="360" width="70" height="110" fill="#0474e1" rx="5" ry="5"/>
          <rect x="380" y="260" width="70" height="210" fill="#0474e1" rx="5" ry="5"/>
        </svg>
        """;
    SvgImageExporter.svgToPng(svg, 128,128, file128);
    BufferedImage bimg = ImageIO.read(file128);
    assertEquals(128, bimg.getWidth(), "Width is wrong in file " + file128);
    assertEquals(128, bimg.getHeight(), "Height is wrong in file " + file128);
    file128.deleteOnExit();
  }
}
