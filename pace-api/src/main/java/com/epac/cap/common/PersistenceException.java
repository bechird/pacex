package com.epac.cap.common;

/**
 * This exception class indicates a situation where an error occurred in the persistence layer. This could be during a
 * persistence call or during any call to the persistence layer.
 */
public class PersistenceException extends EpacException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 75794537331177018L;

	/**
	   * Constructs a new exception with an empty string as its detail message. The cause is not initialized, and may
	   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
	   */
	  public PersistenceException() {
	    this("");
	  }

	  /**
	   * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
	   * initialized by a call to Throwable.initCause(java.lang.Throwable).
	   * 
	   * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage()
	   *          method.
	   */
	  public PersistenceException(final String message) {
	    super(message);
	  }

	  /**
	   * Constructs a new exception with the specified detail message and cause.
	   * 
	   * Note that the detail message associated with cause is not automatically incorporated in this exception's detail
	   * message.
	   * 
	   * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
	   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is
	   *          permitted, and indicates that the cause is nonexistent or unknown.)
	   */
	  public PersistenceException(final String message, final Throwable cause) {
	    super(message, cause);
	  }

	  /**
	   * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString())
	   * (which typically contains the class and detail message of cause). This constructor is useful for exceptions that
	   * are little more than wrappers for other throwables (for example, PrivilegedActionException).
	   * 
	   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is
	   *          permitted, and indicates that the cause is nonexistent or unknown.)
	   */
	  public PersistenceException(final Throwable cause) {
	    super(cause);
	  }
}
