package gr.uoa.di.thanos.botcraft.renderers;

/**
 * Exception thrown by renderers if any errors occur.
 * 
 * @author thanos
 */
public class RendererException extends Exception {
	private static final long serialVersionUID = 0L;

	/**
	 * Construct a new render exception.
	 * 
	 * @param message
	 *            the message of this render exception
	 * @param cause
	 *            the cause of this render exception
	 */
	public RendererException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a new render exception.
	 * 
	 * @param message
	 *            the message of this render exception
	 * @param cause
	 *            the cause of this render exception
	 */
	public RendererException(final String message, final String cause) {
		super(message, new RendererException(cause));
	}

	/**
	 * Construct a new render exception.
	 * 
	 * @param message
	 *            the message of this render exception
	 */
	public RendererException(final String message) {
		super(message);
	}
}
