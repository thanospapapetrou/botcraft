package gr.uoa.di.thanos.botcraft.etc.configuration;

import gr.uoa.di.thanos.botcraft.etc.utilities.SimpleGraphicsConfigTemplate;
import gr.uoa.di.thanos.botcraft.renderers.ColorComponent;

import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.ColorModel;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

/**
 * Class implementing configuration storage.
 * 
 * @author thanos
 */
public class Configuration {
	/**
	 * Maximum depth buffer bits to use.
	 */
	public static final int MAX_DEPTH_BUFFER_BITS = 32;

	/**
	 * Maximum sample buffers to use.
	 */
	public static final int MAX_SAMPLE_BUFFERS = 16;

	private static final String LANGUAGE = "language";
	private static final String RESOURCE_BUNDLE = "gr.uoa.di.thanos.botcraft.i18n.botcraft";
	private static final String ABOUT = "/gr/uoa/di/thanos/botcraft/about/botcraft-%1$s.html";
	private static final String SCREEN = "screen";
	private static final String DISPLAY_MODE_WIDTH = "displayMode.width";
	private static final String DISPLAY_MODE_HEIGHT = "displayMode.height";
	private static final String DISPLAY_MODE_BIT_DEPTH = "displayMode.bitDepth";
	private static final String DISPLAY_MODE_REFRESH_RATE = "displayMode.refreshRate";
	private static final String FULL_SCREEN = "fullScreen";
	private static final String FRAMES_PER_SECOND = "framesPerSecond";
	private static final String KEYBOARD_CONTROL_CODE = "%1$s.code";
	private static final String KEYBOARD_CONTROL_LOCATION = "%1$s.location";
	private static final String KEYBOARD_CONTROL_MODIFIERS = "%1$s.modifiers";
	private static final String HARDWARE_ACCELERATED = "hardwareAccelerated";
	private static final String DOUBLE_BUFFERED = "doubleBuffered";
	private static final String FRAME_BUFFER_BITS_COLOR_COMPONENT = "frameBufferBits.%1$s";
	private static final String DEPTH_BUFFER_BITS = "depthBufferBits";
	private static final String SAMPLE_BUFFERS = "sampleBuffers";
	private static final String CL_PLATFORM = "clPlatform";
	private static final String CL_DEVICE = "clDevice";
	private static final int MAX_FRAMES_PER_SECOND = 120;

	private final Preferences preferences;

	/**
	 * Get the maximum frames per second supported by a display mode.
	 * 
	 * @param displayMode
	 *            the display mode
	 * @return the maximum frames per second supported by the given display mode (120 if the actual frames per second is unknown)
	 */
	public static int getMaxFramesPerSecond(final DisplayMode displayMode) {
		Objects.requireNonNull(displayMode, "Display mode must not be null");
		final int refreshRate = displayMode.getRefreshRate();
		return ((refreshRate != DisplayMode.REFRESH_RATE_UNKNOWN) && (refreshRate < MAX_FRAMES_PER_SECOND)) ? refreshRate : MAX_FRAMES_PER_SECOND;
	}

	/**
	 * Check if a screen supports hardware acceleration.
	 * 
	 * @param screen
	 *            the screen
	 * @return <code>true</code> if the given screen supports hardware acceleration, <code>false</code> otherwise
	 */
	public static boolean isHardwareAccelerated(final GraphicsDevice screen) {
		Objects.requireNonNull(screen, "Screen must not be null");
		return getBestConfiguration(screen).getImageCapabilities().isAccelerated();
	}

	/**
	 * Check if a screen supports double buffering.
	 * 
	 * @param screen
	 *            the screen
	 * @return <code>true</code> if the given screen supports double buffering, <code>false</code> otherwise
	 */
	public static boolean isDoubleBuffered(final GraphicsDevice screen) {
		Objects.requireNonNull(screen, "Screen must not be null");
		return getBestConfiguration(screen).getBufferCapabilities().isPageFlipping();
	}

	/**
	 * Get the maximum depth buffer bits supported by a screen for a specific color component.
	 * 
	 * @param screen
	 *            the screen
	 * @param colorComponent
	 *            the color component
	 * @return the maximum frame buffer bits supported by the given screen for the given color component
	 */
	public static int getMaxFrameBufferBits(final GraphicsDevice screen, final ColorComponent colorComponent) {
		Objects.requireNonNull(screen, "Screen must not be null");
		Objects.requireNonNull(colorComponent, "Color component must not be null");
		return getColorModel(screen).getComponentSize(colorComponent.ordinal());
	}

	private static ResourceBundle getResourceBundle(final Locale language) {
		final ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, language);
		return resourceBundle.getLocale().equals(language) ? resourceBundle : null;
	}

	private static URL getAbout(final Locale language) {
		return Configuration.class.getResource(String.format(ABOUT, language.toLanguageTag()));
	}

	private static boolean isLanguageSupported(final Locale language) {
		return ((getResourceBundle(language) != null) && (getAbout(language) != null));
	}

	private static KeyEvent codeLocationModifiers2KeyEvent(final int code, final int location, final int modifiers) {
		return ((code == KeyEvent.VK_UNDEFINED) || (location == KeyEvent.KEY_LOCATION_UNKNOWN)) ? null : new KeyEvent(new Container(), 0, 0L, modifiers, code, (char) 0, location);
	}

	private static GraphicsConfiguration getBestConfiguration(final GraphicsDevice screen) {
		return screen.getBestConfiguration(new SimpleGraphicsConfigTemplate());
	}

	private static ColorModel getColorModel(final GraphicsDevice screen) {
		return getBestConfiguration(screen).getColorModel(Transparency.TRANSLUCENT);
	}

	/**
	 * Construct a new configuration.
	 */
	public Configuration() {
		preferences = Preferences.userNodeForPackage(getClass());
	}

	/**
	 * Get the languages supported by this configuration.
	 * 
	 * @return an array containing the languages supported by this configuration
	 */
	public Locale[] getSupportedLanguages() {
		final List<Locale> languages = new ArrayList<Locale>();
		for (final Locale language : Locale.getAvailableLocales()) {
			if (isLanguageSupported(language)) {
				languages.add(language);
			}
		}
		return languages.toArray(new Locale[0]);
	}

	/**
	 * Format a string based on the currently selected language.
	 * 
	 * @param key
	 *            the key of the string to format
	 * @param arguments
	 *            the arguments to use
	 * @return the formatted string
	 */
	public String format(final String key, final Object... arguments) {
		return new MessageFormat(getResourceBundle(getLanguage()).getString(key), getLanguage()).format(arguments, new StringBuffer(), null).toString();
	}

	/**
	 * Get the about URL to use based on the currently selected language.
	 * 
	 * @return the about URL to use
	 */
	public URL getAbout() {
		return getAbout(getLanguage());
	}

	/**
	 * Get the language to use.
	 * 
	 * @return the language to use (english is the default)
	 */
	public Locale getLanguage() {
		final Locale language = Locale.forLanguageTag(preferences.get(LANGUAGE, Locale.getDefault().toLanguageTag()));
		return isLanguageSupported(language) ? language : Locale.ENGLISH;
	}

	/**
	 * Set the language to use.
	 * 
	 * @param language
	 *            the language to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setLanguage(final Locale language) throws BackingStoreException {
		Objects.requireNonNull(language, "Language must not be null");
		if (!isLanguageSupported(language)) {
			throw new IllegalArgumentException("Language is not supported");
		}
		preferences.put(LANGUAGE, language.toLanguageTag());
		preferences.flush();
	}

	/**
	 * Get the screen to use.
	 * 
	 * @return the screen to use
	 */
	public GraphicsDevice getScreen() {
		final GraphicsDevice defaultScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final GraphicsDevice screen = getScreen(preferences.get(SCREEN, defaultScreen.getIDstring()));
		return (screen == null) ? defaultScreen : screen;
	}

	/**
	 * Set the screen to use.
	 * 
	 * @param screen
	 *            the screen to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setScreen(final GraphicsDevice screen) throws BackingStoreException {
		Objects.requireNonNull(screen, "Screen must not be null");
		if (getScreen(screen.getIDstring()) == null) {
			throw new IllegalArgumentException("Screen is not supported");
		}
		preferences.put(SCREEN, screen.getIDstring());
		preferences.flush();
	}

	/**
	 * Get the display mode to use.
	 * 
	 * @return the display mode to use
	 */
	public DisplayMode getDisplayMode() {
		final DisplayMode currentDisplayMode = getScreen().getDisplayMode();
		final DisplayMode displayMode = getDisplayMode(preferences.getInt(DISPLAY_MODE_WIDTH, currentDisplayMode.getWidth()), preferences.getInt(DISPLAY_MODE_HEIGHT, currentDisplayMode.getHeight()), preferences.getInt(DISPLAY_MODE_BIT_DEPTH, currentDisplayMode.getBitDepth()), preferences.getInt(DISPLAY_MODE_REFRESH_RATE, currentDisplayMode.getRefreshRate()));
		return (displayMode == null) ? currentDisplayMode : displayMode;
	}

	/**
	 * Set the display mode to use.
	 * 
	 * @param displayMode
	 *            the display mode to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setDisplayMode(final DisplayMode displayMode) throws BackingStoreException {
		Objects.requireNonNull(displayMode, "Display mode must not be null");
		if (getDisplayMode(displayMode.getWidth(), displayMode.getHeight(), displayMode.getBitDepth(), displayMode.getRefreshRate()) == null) {
			throw new IllegalArgumentException("Display mode is not supported");
		}
		preferences.putInt(DISPLAY_MODE_WIDTH, displayMode.getWidth());
		preferences.putInt(DISPLAY_MODE_HEIGHT, displayMode.getHeight());
		preferences.putInt(DISPLAY_MODE_BIT_DEPTH, displayMode.getBitDepth());
		preferences.putInt(DISPLAY_MODE_REFRESH_RATE, displayMode.getRefreshRate());
		preferences.flush();
	}

	/**
	 * Check whether to use full screen mode.
	 * 
	 * @return <code>true</code> if full screen mode is to be used, <code>false</code> otherwise
	 */
	public boolean isFullScreen() {
		return getScreen().isFullScreenSupported() && preferences.getBoolean(FULL_SCREEN, false);
	}

	/**
	 * Set whether to use full screen mode.
	 * 
	 * @param fullScreen
	 *            <code>true</code> to use full screen mode, <code>false</code> otherwise
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setFullScreen(final boolean fullScreen) throws BackingStoreException {
		if ((!getScreen().isFullScreenSupported()) && fullScreen) {
			throw new IllegalArgumentException("Full screen is not supported");
		}
		preferences.putBoolean(FULL_SCREEN, fullScreen);
		preferences.flush();
	}

	/**
	 * Get the frames per second to use.
	 * 
	 * @return the frames per second to use
	 */
	public int getFramesPerSecond() {
		final int maxFramesPerSecond = getMaxFramesPerSecond(getDisplayMode());
		final int framesPerSecond = preferences.getInt(FRAMES_PER_SECOND, maxFramesPerSecond);
		return ((framesPerSecond < 1) || (framesPerSecond > maxFramesPerSecond)) ? maxFramesPerSecond : framesPerSecond;
	}

	/**
	 * Set the frames per second to use.
	 * 
	 * @param framesPerSecond
	 *            the frames per second to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setFramesPerSecond(final int framesPerSecond) throws BackingStoreException {
		final int maxFramesPerSecond = getMaxFramesPerSecond(getDisplayMode());
		if ((framesPerSecond < 1) || (framesPerSecond > maxFramesPerSecond)) {
			throw new IllegalArgumentException("Frames per second must be between 1 and " + maxFramesPerSecond + " inclusive");
		}
		preferences.putInt(FRAMES_PER_SECOND, framesPerSecond);
		preferences.flush();
	}

	/**
	 * Get the keyboard event to map to a specific keyboard control.
	 * 
	 * @param control
	 *            the keyboard control whose event to retrieve
	 * @return the keyboard event corresponding to the given keyboard control
	 */
	public KeyEvent getKeyEvent(final KeyboardControl control) {
		Objects.requireNonNull(control, "Control must not be null");
		final int code = preferences.getInt(String.format(KEYBOARD_CONTROL_CODE, control.toString()), control.getDefaultKeyEvent().getKeyCode());
		final int location = preferences.getInt(String.format(KEYBOARD_CONTROL_LOCATION, control.toString()), control.getDefaultKeyEvent().getKeyLocation());
		final int modifiers = preferences.getInt(String.format(KEYBOARD_CONTROL_MODIFIERS, control.toString()), control.getDefaultKeyEvent().getModifiers());
		return codeLocationModifiers2KeyEvent(code, location, modifiers);
	}

	/**
	 * Set the keyboard event to map to a specific keyboard control.
	 * 
	 * @param control
	 *            the keyboard control to map the event to
	 * @param event
	 *            the keyboard event to map
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setKeyEvent(final KeyboardControl control, final KeyEvent event) throws BackingStoreException {
		Objects.requireNonNull(control, "Control must not be null");
		preferences.putInt(String.format(KEYBOARD_CONTROL_CODE, control.toString()), (event == null) ? KeyEvent.VK_UNDEFINED : event.getKeyCode());
		preferences.putInt(String.format(KEYBOARD_CONTROL_LOCATION, control.toString()), (event == null) ? KeyEvent.KEY_LOCATION_UNKNOWN : event.getKeyLocation());
		preferences.putInt(String.format(KEYBOARD_CONTROL_MODIFIERS, control.toString()), (event == null) ? 0 : event.getModifiersEx());
		preferences.flush();
	}

	/**
	 * Check whether to use hardware acceleration.
	 * 
	 * @return <code>true</code> if hardware acceleration is to be used, <code>false</code> otherwise
	 */
	public boolean isHardwareAccelerated() {
		return isHardwareAccelerated(getScreen()) && preferences.getBoolean(HARDWARE_ACCELERATED, true);
	}

	/**
	 * Set whether to use hardware acceleration.
	 * 
	 * @param hardwareAccelerated
	 *            <code>true</code> to use hardware acceleration, <code>false</code> otherwise
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setHardwareAccelerated(final boolean hardwareAccelerated) throws BackingStoreException {
		if ((!isHardwareAccelerated(getScreen())) && hardwareAccelerated) {
			throw new IllegalArgumentException("Hardware acceleration is not supported");
		}
		preferences.putBoolean(HARDWARE_ACCELERATED, hardwareAccelerated);
		preferences.flush();
	}

	/**
	 * Check whether to use double buffering.
	 * 
	 * @return <code>true</code> to use double buffering, <code>false</code> otherwise
	 */
	public boolean isDoubleBuffered() {
		return isDoubleBuffered(getScreen()) && preferences.getBoolean(DOUBLE_BUFFERED, true);
	}

	/**
	 * Set whether to use double buffering.
	 * 
	 * @param doubleBuffered
	 *            <code>true</code> to use double buffering, <code>false</code> otherwise
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setDoubleBuffered(final boolean doubleBuffered) throws BackingStoreException {
		if ((!isDoubleBuffered(getScreen())) && doubleBuffered) {
			throw new IllegalArgumentException("Double buffering is not supported");
		}
		preferences.putBoolean(DOUBLE_BUFFERED, doubleBuffered);
		preferences.flush();
	}

	/**
	 * Get the buffer bits to use for a specific color component.
	 * 
	 * @param colorComponent
	 *            the color component
	 * @return the buffer bits to use for the given color component
	 */
	public int getFrameBufferBits(final ColorComponent colorComponent) {
		Objects.requireNonNull(colorComponent, "Color component must not be null");
		final int maxFrameBufferBits = getMaxFrameBufferBits(getScreen(), colorComponent);
		final int frameBufferBits = preferences.getInt(String.format(FRAME_BUFFER_BITS_COLOR_COMPONENT, colorComponent.toString()), maxFrameBufferBits);
		return ((frameBufferBits < 1) || (frameBufferBits > maxFrameBufferBits)) ? maxFrameBufferBits : frameBufferBits;
	}

	/**
	 * Set the buffer bits to use for a specific color component.
	 * 
	 * @param colorComponent
	 *            the color component
	 * @param frameBufferBits
	 *            the buffer bits to use for the given color component
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setFrameBufferBits(final ColorComponent colorComponent, final int frameBufferBits) throws BackingStoreException {
		Objects.requireNonNull(colorComponent, "Color component must not be null");
		final int maxFrameBufferBits = getMaxFrameBufferBits(getScreen(), colorComponent);
		if ((frameBufferBits < 1) || (frameBufferBits > maxFrameBufferBits)) {
			throw new IllegalArgumentException("Frame buffer bits must be between 1 and " + maxFrameBufferBits + " inclusive");
		}
		preferences.putInt(String.format(FRAME_BUFFER_BITS_COLOR_COMPONENT, colorComponent), frameBufferBits);
		preferences.flush();
	}

	/**
	 * Get the depth buffer bits to use.
	 * 
	 * @return the depth buffer bits to use
	 */
	public int getDepthBufferBits() {
		final int depthBufferBits = preferences.getInt(DEPTH_BUFFER_BITS, MAX_DEPTH_BUFFER_BITS);
		return ((depthBufferBits < 1) || (depthBufferBits > MAX_DEPTH_BUFFER_BITS)) ? MAX_DEPTH_BUFFER_BITS : depthBufferBits;
	}

	/**
	 * Set the depth buffer bits to use.
	 * 
	 * @param depthBufferBits
	 *            the depth buffer bits to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setDepthBufferBits(final int depthBufferBits) throws BackingStoreException {
		if ((depthBufferBits < 1) || (depthBufferBits > MAX_DEPTH_BUFFER_BITS)) {
			throw new IllegalArgumentException("Depth buffer bits must be between 1 and " + MAX_DEPTH_BUFFER_BITS + " inclusive");
		}
		preferences.putInt(DEPTH_BUFFER_BITS, depthBufferBits);
		preferences.flush();
	}

	/**
	 * Get the sample buffer to use.
	 * 
	 * @return the sample buffers to use
	 */
	public int getSampleBuffers() {
		final int sampleBuffers = preferences.getInt(SAMPLE_BUFFERS, MAX_SAMPLE_BUFFERS);
		return ((sampleBuffers < 0) || (sampleBuffers > MAX_SAMPLE_BUFFERS)) ? MAX_SAMPLE_BUFFERS : sampleBuffers;
	}

	/**
	 * Set the sample buffers to use.
	 * 
	 * @param sampleBuffers
	 *            the sample buffers to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setSampleBuffers(final int sampleBuffers) throws BackingStoreException {
		if ((sampleBuffers < 0) || (sampleBuffers > MAX_SAMPLE_BUFFERS)) {
			throw new IllegalArgumentException("Sample buffers must be between 0 and " + MAX_SAMPLE_BUFFERS + " inclusive");
		}
		preferences.putInt(SAMPLE_BUFFERS, sampleBuffers);
		preferences.flush();
	}

	/**
	 * Get the OpenCL platform to use.
	 * 
	 * @return the OpenCL platform to use
	 */
	public CLPlatform getClPlatform() {
		final CLPlatform clPlatform = getClPlatform(preferences.get(CL_PLATFORM, CLPlatform.getDefault().getName()));
		return (clPlatform == null) ? CLPlatform.getDefault() : clPlatform;
	}

	/**
	 * Set the OpenCL platform to use.
	 * 
	 * @param clPlatform
	 *            the OpenCL platform to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setClPlatform(final CLPlatform clPlatform) throws BackingStoreException {
		Objects.requireNonNull(clPlatform, "OpenCL platform must not be null");
		if (getClPlatform(clPlatform.getName()) == null) {
			throw new IllegalArgumentException("OpenCL platform is not supported");
		}
		preferences.put(CL_PLATFORM, clPlatform.getName());
		preferences.flush();
	}

	/**
	 * Get the OpenCL device to use.
	 * 
	 * @return the OpenCL device to use
	 */
	@SuppressWarnings("unchecked")
	public CLDevice getClDevice() {
		final CLDevice clDevice = getClDevice(preferences.get(CL_DEVICE, getClPlatform().getMaxFlopsDevice(new ClGlDeviceFilter()).getName()));
		return (clDevice == null) ? getClPlatform().getMaxFlopsDevice(new ClGlDeviceFilter()) : clDevice;
	}

	/**
	 * Set the OpenCL device to use.
	 * 
	 * @param clDevice
	 *            the OpenCL device to use
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void setClDevice(final CLDevice clDevice) throws BackingStoreException {
		Objects.requireNonNull(clDevice, "OpenCL device must not be null");
		if (getClDevice(clDevice.getName()) == null) {
			throw new IllegalArgumentException("OpenCL device is not supported");
		}
		preferences.put(CL_DEVICE, clDevice.getName());
		preferences.flush();
	}

	/**
	 * Reset this configuration to default values.
	 * 
	 * @throws BackingStoreException
	 *             if any errors occur
	 */
	public void reset() throws BackingStoreException {
		preferences.clear();
		preferences.flush();
	}

	private GraphicsDevice getScreen(final String id) {
		for (final GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (screen.getIDstring().equals(id)) {
				return screen;
			}
		}
		return null;
	}

	private DisplayMode getDisplayMode(final int width, final int height, final int bitDepth, final int refreshRate) {
		for (final DisplayMode displayMode : getScreen().getDisplayModes()) {
			if ((displayMode.getWidth() == width) && (displayMode.getHeight() == height) && (displayMode.getBitDepth() == bitDepth) && (displayMode.getRefreshRate() == refreshRate)) {
				return displayMode;
			}
		}
		return null;
	}

	private CLPlatform getClPlatform(final String name) {
		for (final CLPlatform clPlatform : CLPlatform.listCLPlatforms()) {
			if (clPlatform.getName().equals(name)) {
				return clPlatform;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private CLDevice getClDevice(final String name) {
		for (final CLDevice clDevice : getClPlatform().listCLDevices(new ClGlDeviceFilter())) {
			if (clDevice.getName().equals(name)) {
				return clDevice;
			}
		}
		return null;
	}
}
