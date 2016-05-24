package gr.uoa.di.thanos.botcraft.etc.utilities;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Simple implementation of error handler that logs warnings and propagates errors and fatal errors.
 * 
 * @author thanos
 */
public class SimpleErrorHandler implements ErrorHandler {
	private static final String WARNING = "%1$s (public ID: %2$s, system ID: %3%s, line: %4$s, column: %5$s)";
	private static final String UNKNONW = "unknown";
	private static final Logger LOGGER = Logger.getLogger(SimpleErrorHandler.class.getName());

	@Override
	public void error(final SAXParseException e) throws SAXParseException {
		Objects.requireNonNull(e, "Exception must not be null");
		throw e;
	}

	@Override
	public void fatalError(final SAXParseException e) throws SAXParseException {
		Objects.requireNonNull(e, "Exception must not be null");
		throw e;
	}

	@Override
	public void warning(final SAXParseException e) throws SAXException {
		Objects.requireNonNull(e, "Exception must not be null");
		LOGGER.log(Level.WARNING, String.format(WARNING, (e.getMessage() == null) ? UNKNONW : e.getMessage(), (e.getPublicId() == null) ? UNKNONW : e.getPublicId(), (e.getSystemId() == null) ? UNKNONW : e.getSystemId(), (e.getLineNumber() == -1) ? UNKNONW : Integer.toString(e.getLineNumber()), (e.getColumnNumber() == -1) ? UNKNONW : Integer.toString(e.getColumnNumber())), e);
	}
}
