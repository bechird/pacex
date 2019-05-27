package com.epac.cap.common;

/**
 * This exception class indicates a situation where an entity could not be retrieved and the entity is required to
 * proceed (i.e. not recoverable).
 * 
 */
public class EntityNotFoundException extends PersistenceException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3527648990423262044L;

	/**
	   * Constructs a new exception with an empty string as its detail message. The cause is not initialized, and may
	   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
	   */
	  public EntityNotFoundException() {
	    this("");
	  }


	  /**
	   * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
	   * initialized by a call to Throwable.initCause(java.lang.Throwable).
	   * 
	   * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage()
	   *          method.
	   */
	  public EntityNotFoundException(final String message) {
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
	  public EntityNotFoundException(final String message, final Throwable cause) {
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
	  public EntityNotFoundException(final Throwable cause) {
	    super(cause);
	  }
}
