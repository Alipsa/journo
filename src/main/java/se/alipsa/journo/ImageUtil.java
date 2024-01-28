package se.alipsa.journo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Image utilities
 */
public class ImageUtil {

  private ImageUtil() {
    // Static methods only
  }

  /**
   * Convert the resource into a data url (base 64 encoded string)
   *
   * @param resource the resource to convert
   * @return a data url (base 64 encoded string)
   * @throws IOException if the resource cannot be read
   */
  public static String asDataUrl(String resource) throws IOException {
    return asDataUrl(resource, getMediaType(resource));
  }

  /**
   * Convert the resource into a data url (base 64 encoded string)
   *
   * @param resource the resource to convert
   * @param mediaType the mime type of the image (e.g. image/png)
   * @return a data url (base 64 encoded string)
   * @throws IOException if the resource cannot be read
   */
  public static String asDataUrl(String resource, String mediaType) throws IOException {
    return asDataUrl(resource, mediaType, ImageUtil.class);
  }

  /**
   * Convert the resource into a data url (base 64 encoded string)
   * @param resource the resource to convert
   * @param mediaType the mime type of the image (e.g. image/png)
   * @param caller the calling class where the classloader can reach the resource specified
   * @return a data url (base 64 encoded string)
   * @throws IOException if the resource cannot be read
   */
  public static String asDataUrl(String resource, String mediaType, Class<?> caller) throws IOException {
    byte[] bytes = readBytes(resource, caller);
    String content = Base64.getEncoder().encodeToString(bytes);
    return "data:" + mediaType + ";base64," + content;
  }

  private static byte[] readBytes(String resource, Class<?> clazz) throws IOException {
    try(InputStream is = clazz.getResourceAsStream(resource)) {
      if (is == null) {
        throw new IOException("Failed to create input stream from " + resource);
      }
      return is.readAllBytes();
    }
  }

  private static String getMediaType(String resource) {
    String res = resource.toLowerCase();
    if (res.endsWith("png")) {
      return "image/png";
    }
    if (res.endsWith("gif")) {
      return "image/gif";
    }
    if (res.endsWith("jpg") || res.endsWith("jpeg")) {
      return "image/jpeg";
    }
    if (res.endsWith("svg")) {
      return "image/svg+xml";
    }
    if (res.endsWith("bmp")) {
      return "image/bmp";
    }
    if (res.endsWith("tif") || res.endsWith("tiff")) {
      return "image/tiff";
    }
    throw new IllegalArgumentException("Unknown file type: " + resource);
  }
}
