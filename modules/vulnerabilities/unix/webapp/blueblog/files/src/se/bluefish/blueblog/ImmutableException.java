/*
 * Created on 2003-jul-10
 *
 */
package se.bluefish.blueblog;

/**
 * Thrown when a manipulating method is being called on an
 * immutable object. As an example, see 
 * {@link se.bluefish.blueblog.blog.Category#createSubCategory(java.lang.String,java.lang.String)}.
 * 
 * @author Robert Burén
 *
 */
public class ImmutableException extends BBException {

	/**
	 * 
	 */
	public ImmutableException() {
		super();
	}

	/**
	 * @param message
	 */
	public ImmutableException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ImmutableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ImmutableException(Throwable cause) {
		super(cause);
	}
}
