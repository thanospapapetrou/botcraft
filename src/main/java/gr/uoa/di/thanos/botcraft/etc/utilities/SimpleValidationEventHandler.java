package gr.uoa.di.thanos.botcraft.etc.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.w3c.dom.Node;

/**
 * Simple implementation of validation event handler that logs warnings and propagates errors and fatal errors.
 * 
 * @author thanos
 */
public class SimpleValidationEventHandler implements ValidationEventHandler {
	private static final String WARNING = "%1$s (URL: %2$s, line: %3$s, column: %4$s, byte offset: %5$s, node: %6$s, object: %7$s)";
	private static final String UNKNOWN = "unknown";
	private static final Map<Short, String> NODE_TYPES = new HashMap<>();
	private static final Logger LOGGER = Logger.getLogger(SimpleValidationEventHandler.class.getName());

	static {
		NODE_TYPES.put(Node.ATTRIBUTE_NODE, "attribute");
		NODE_TYPES.put(Node.CDATA_SECTION_NODE, "CDATA section");
		NODE_TYPES.put(Node.COMMENT_NODE, "comment");
		NODE_TYPES.put(Node.DOCUMENT_NODE, "document");
		NODE_TYPES.put(Node.DOCUMENT_FRAGMENT_NODE, "document fragment");
		NODE_TYPES.put(Node.DOCUMENT_TYPE_NODE, "document type");
		NODE_TYPES.put(Node.ELEMENT_NODE, "element");
		NODE_TYPES.put(Node.ENTITY_NODE, "entity");
		NODE_TYPES.put(Node.ENTITY_REFERENCE_NODE, "entity reference");
		NODE_TYPES.put(Node.NOTATION_NODE, "notation");
		NODE_TYPES.put(Node.PROCESSING_INSTRUCTION_NODE, "processing instruction");
		NODE_TYPES.put(Node.TEXT_NODE, "text");
	}

	private static String node2String(final Node node) {
		final String type = nodeType2String(node.getNodeType());
		return (type == null) ? UNKNOWN : (type + " " + node.getNodeName() + ((node.getNodeValue() == null) ? "" : (" " + node.getNodeValue())));
	}

	private static String nodeType2String(final short nodeType) {
		return NODE_TYPES.containsKey(nodeType) ? NODE_TYPES.get(nodeType) : null;
	}

	@Override
	public boolean handleEvent(final ValidationEvent event) {
		Objects.requireNonNull(event, "Event must not be null");
		if (event.getSeverity() == ValidationEvent.WARNING) {
			LOGGER.log(Level.WARNING, String.format(WARNING, (event.getMessage() == null) ? UNKNOWN : event.getMessage(), (event.getLocator().getURL() == null) ? UNKNOWN : event.getLocator().getURL().toString(), (event.getLocator().getLineNumber() == -1) ? UNKNOWN : Integer.toString(event.getLocator().getLineNumber()), (event.getLocator().getColumnNumber() == -1) ? UNKNOWN : Integer.toString(event.getLocator().getColumnNumber()), (event.getLocator().getOffset() == -1) ? UNKNOWN : Integer.toString(event.getLocator().getOffset()), (event.getLocator().getNode() == null) ? UNKNOWN : node2String(event.getLocator().getNode()), (event.getLocator().getObject() == null) ? UNKNOWN : event.getLocator().getObject().toString()), event.getLinkedException());
			return true;
		}
		return false;
	}
}
