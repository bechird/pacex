package com.epac.cap.common;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * The parent exception for all EPAC checked exceptions. This is a generic exception wrapper that can be extended to
 * provide better context details and meaning.
 */
public class EpacException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8586482458839995808L;


	  /**
	   * Constructs a new exception with an empty string as its detail message. The cause is not initialized, and may
	   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
	   */
	  public EpacException() {
	    this("");
	  }

	  /**
	   * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
	   * initialized by a call to Throwable.initCause(java.lang.Throwable).
	   * 
	   * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage()
	   *          method.
	   */
	  public EpacException(final String message) {
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
	  public EpacException(final String message, final Throwable cause) {
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
	  public EpacException(final Throwable cause) {
	    super(cause);
	  }

	  /**
	   * Returns the root cause of this exception or null if no cause was provided.
	   * 
	   * @return
	   * @see ExceptionUtils#getRootCause(Throwable)
	   */
	  public Throwable getRootCause() {
	    return ExceptionUtils.getRootCause(this);
	  }
}
