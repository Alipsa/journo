package se.alipsa.journo;

/**
 * Base checked exception class for journo operations
 */
public class JournoException extends Exception {

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the description of the issue
   */
  public JournoException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the description of the issue
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   */
  public JournoException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   */
  public JournoException(Throwable cause) {
    super(cause);
  }
}
