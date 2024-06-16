package se.alipsa.journo;

/**
 * Base checked exception class for journo operations
 */
public class JournoException extends Exception {
  public JournoException(String message) {
    super(message);
  }

  public JournoException(String message, Throwable cause) {
    super(message, cause);
  }

  public JournoException(Throwable cause) {
    super(cause);
  }
}
