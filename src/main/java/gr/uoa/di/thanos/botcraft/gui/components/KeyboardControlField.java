package gr.uoa.di.thanos.botcraft.gui.components;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.JTextField;

/**
 * Field for editing keyboard controls.
 * 
 * @author thanos
 */
public class KeyboardControlField extends JTextField implements KeyListener {
	private static final long serialVersionUID = 0L;
	private static final Pattern MODIFIERS_DELIMITER = Pattern.compile(Pattern.quote("+"));
	private static final Map<Integer, String> LOCATIONS = new HashMap<Integer, String>();
	private static final Pattern TEXT_LOCATION = Pattern.compile("(NumPad )|(NumPad\\-)");
	private static final String KEYBOARD_CONTROL_DELIMITER = "KEYBOARD_CONTROL_DELIMITER";
	private static final String KEYBOADR_CONTROL_LOCATION = "KEYBOARD_CONTROL_LOCATION";

	private final Configuration configuration;
	private KeyEvent keyEvent;

	static {
		LOCATIONS.put(KeyEvent.KEY_LOCATION_LEFT, "KEY_LOCATION_LEFT");
		LOCATIONS.put(KeyEvent.KEY_LOCATION_RIGHT, "KEY_LOCATION_RIGHT");
		LOCATIONS.put(KeyEvent.KEY_LOCATION_NUMPAD, "KEY_LOCATION_NUMPAD");
	}

	/**
	 * Construct a new keyboard control field.
	 * 
	 * @param configuration
	 *            the configuration to use for localization
	 * @param keyEvent
	 *            the key event to bind to this keyboard control field or <code>null</code> to leave it unbound
	 */
	public KeyboardControlField(final Configuration configuration, final KeyEvent keyEvent) {
		super();
		Objects.requireNonNull(configuration, "Configuration must not be null");
		this.configuration = configuration;
		this.keyEvent = keyEvent;
		setEditable(false);
		setFocusTraversalKeysEnabled(false);
		setText(keyEvent2String(keyEvent));
		addKeyListener(this);
	}

	/**
	 * Get key event.
	 * 
	 * @return the key event bound to this keyboard control field or <code>null</code> if it is currently unbound
	 */
	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	/**
	 * Set key event.
	 * 
	 * @param event
	 *            the key event to bind to this keyboard control field or <code>null</code> to leave it unbound
	 */
	public void setKeyEvent(final KeyEvent event) {
		keyPressed(event);
	}

	@Override
	public void keyPressed(final KeyEvent event) {
		this.keyEvent = event;
		this.setText(keyEvent2String(event));
	}

	@Override
	public void keyReleased(final KeyEvent event) {
	}

	@Override
	public void keyTyped(final KeyEvent event) {
	}

	private String keyEvent2String(final KeyEvent event) {
		if (event == null) {
			return null;
		}
		final List<String> modifiers = Arrays.asList(MODIFIERS_DELIMITER.split(KeyEvent.getModifiersExText(event.getModifiersEx())));
		final String location = LOCATIONS.containsKey(event.getKeyLocation()) ? configuration.format(LOCATIONS.get(event.getKeyLocation())) : null;
		final String text = TEXT_LOCATION.matcher(KeyEvent.getKeyText(event.getKeyCode())).replaceAll(""); // remove location from text
		final StringBuilder stringBuilder = new StringBuilder();
		final String keyboardControlDelimiter = configuration.format(KEYBOARD_CONTROL_DELIMITER);
		for (final String modifier : modifiers) {
			stringBuilder.append((stringBuilder.length() > 0) ? keyboardControlDelimiter : "").append(modifier);
		}
		return stringBuilder.append((modifiers.contains(text) || (stringBuilder.length() == 0)) ? "" : keyboardControlDelimiter).append(modifiers.contains(text) ? "" : ((location == null) ? text : configuration.format(KEYBOADR_CONTROL_LOCATION, location, text))).toString();
	}
}
