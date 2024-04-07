package se.alipsa.journo;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
   * Note: The media type is derived from the resource name (e.g. *.png is interpreted as image/png etc.)
   * If you don't know the media type and the resource does not end with a typical image extension,
   * consider using something like Apache Tika to detect it and then specify the mediaType using
   * <code>asDataUrl(resource, mediaType)</code>
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

  public static String asDataUrl(URL resource, String mediaType) throws IOException {
    return asDataUrl(readBytes(resource), mediaType);
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
    return asDataUrl(readBytes(resource, caller), mediaType);
  }

  public static String asDataUrl(byte[] bytes, String mediaType) {
    String content = Base64.getEncoder().encodeToString(bytes);
    return "data:" + mediaType + ";base64," + content;
  }

  private static byte[] readBytes(URL resource) throws IOException {
    try(InputStream is = resource.openStream()) {
      if (is == null) {
        throw new IOException("Failed to create input stream from " + resource);
      }
      return IOUtils.toByteArray(is);
    }
  }

  private static byte[] readBytes(String resource, Class<?> clazz) throws IOException {
    try(InputStream is = locateResource(resource, clazz)) {
      if (is == null) {
        throw new IOException("Failed to create input stream from " + resource);
      }
      return IOUtils.toByteArray(is);
    }
  }

  private static InputStream locateResource(String resource, Class<?> clazz) throws IOException {
    InputStream is = clazz.getResourceAsStream(resource);
    if (is == null) {
      File file = new File(System.getProperty("user.dir"), resource);
      if (file.exists()) {
          is = new FileInputStream(file);
      } else {
        is = clazz.getClassLoader().getResourceAsStream(resource);
      }
    }
    if (is == null) {
      Path path = Paths.get(resource);
      if (Files.exists(path)) {
        is = Files.newInputStream(path);
      }
    }
    return is;
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
