package gr.uoa.di.thanos.botcraft.etc.configuration;

import java.awt.Container;
import java.awt.event.KeyEvent;

/**
 * Enumeration representing keyboard controls.
 * 
 * @author thanos
 */
public enum KeyboardControl {
	/**
	 * Move forward.
	 */
	MOVE_FORWARD(KeyEvent.VK_UP, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move backward.
	 */
	MOVE_BACKWARD(KeyEvent.VK_DOWN, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move left.
	 */
	MOVE_LEFT(KeyEvent.VK_LEFT, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move right.
	 */
	MOVE_RIGHT(KeyEvent.VK_RIGHT, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move forward left.
	 */
	MOVE_FORWARD_LEFT(KeyEvent.VK_HOME, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move forward right.
	 */
	MOVE_FORWARD_RIGHT(KeyEvent.VK_PAGE_UP, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move backward left.
	 */
	MOVE_BACKWARD_LEFT(KeyEvent.VK_END, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Move backward right.
	 */
	MOVE_BACKWARD_RIGHT(KeyEvent.VK_PAGE_DOWN, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Turn left.
	 */
	TURN_LEFT(KeyEvent.VK_LEFT, KeyEvent.KEY_LOCATION_STANDARD),

	/**
	 * Turn right.
	 */
	TURN_RIGHT(KeyEvent.VK_RIGHT, KeyEvent.KEY_LOCATION_STANDARD),

	/**
	 * Pitch up.
	 */
	PITCH_UP(KeyEvent.VK_UP, KeyEvent.KEY_LOCATION_STANDARD),

	/**
	 * Pitch down.
	 */
	PITCH_DOWN(KeyEvent.VK_DOWN, KeyEvent.KEY_LOCATION_STANDARD),

	/**
	 * Zoom in.
	 */
	ZOOM_IN(KeyEvent.VK_ADD, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Zoom out.
	 */
	ZOOM_OUT(KeyEvent.VK_SUBTRACT, KeyEvent.KEY_LOCATION_NUMPAD),

	/**
	 * Pause and resume.
	 */
	PAUSE_RESUME(KeyEvent.VK_PAUSE, KeyEvent.KEY_LOCATION_STANDARD);

	private final KeyEvent defaultKeyEvent;

	private KeyboardControl(final int defaultCode, final int defaultLocation) {
		defaultKeyEvent = new KeyEvent(new Container(), 0, 0L, 0, defaultCode, (char) 0, defaultLocation);
	}

	/**
	 * Get the default key event for this keyboard control.
	 * 
	 * @return the default event for this keyboard control
	 */
	public KeyEvent getDefaultKeyEvent() {
		return defaultKeyEvent;
	}
}
