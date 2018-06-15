package fabuco.impl.exception;

/**
 * An exception that is thrown when developing FabucoProcess incorrectly.
 */
public class InvalidProcessException extends RuntimeException {

  private static final long serialVersionUID = 3810067304570563755L;

  /**
   * Creates an exception with no message and no cause.
   */
  public InvalidProcessException() {}

  /**
   * Creates an exception with the given message and no cause.
   *
   * @param message
   *        The message for the exception.
   */
  public InvalidProcessException(String message) {
    super(message);
  }

  /**
   * Creates an exception with the given cause and no message.
   *
   * @param cause
   *        The <tt>Throwable</tt> that caused this exception.
   */
  public InvalidProcessException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates an exception with the given message and cause.
   *
   * @param message
   *        The message for the exception.
   * @param cause
   *        The <tt>Throwable</tt> that caused this exception.
   */
  public InvalidProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
