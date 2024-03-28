package se.alipsa.journo.viewer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import javax.imageio.IIOException;

public class SvgImageExporter {

  public static byte[] svgToPng(String svg) throws TranscoderException {
    PNGTranscoder pngTranscoder = new PNGTranscoder();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StringReader reader = new StringReader(svg)) {
      TranscoderInput input = new TranscoderInput(reader);
      TranscoderOutput output = new TranscoderOutput(baos);
      pngTranscoder.transcode(input, output);
      return baos.toByteArray();
    }
  }

  public static byte[] svgToPng(String svg, int width, int height) throws TranscoderException {
    PNGTranscoder pngTranscoder = new PNGTranscoder();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StringReader reader = new StringReader(svg)) {
      TranscoderInput input = new TranscoderInput(reader);
      TranscoderOutput output = new TranscoderOutput(baos);
      pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
      pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);
      pngTranscoder.transcode(input, output);
      return baos.toByteArray();
    }
  }

  public static void svgToPng(String svg, int width, int height, File toFile) throws IOException, TranscoderException {
    try (FileOutputStream fos = new FileOutputStream(toFile)) {
      fos.write(svgToPng(svg, width, height));
    }
  }

  public static void svgToPng(String svg, File toFile) throws IOException, TranscoderException {
    try (FileOutputStream fos = new FileOutputStream(toFile)) {
      fos.write(svgToPng(svg));
    }
  }
}
