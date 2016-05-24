package gr.uoa.di.thanos.botcraft.gui.applications;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;
import gr.uoa.di.thanos.botcraft.etc.configuration.KeyboardControl;
import gr.uoa.di.thanos.botcraft.gui.components.ClDeviceListCellRenderer;
import gr.uoa.di.thanos.botcraft.gui.components.ClPlatformListCellRenderer;
import gr.uoa.di.thanos.botcraft.gui.components.DisplayModeListCellRenderer;
import gr.uoa.di.thanos.botcraft.gui.components.KeyboardControlField;
import gr.uoa.di.thanos.botcraft.gui.components.LanguageListCellRenderer;
import gr.uoa.di.thanos.botcraft.gui.components.ScreenListCellRenderer;
import gr.uoa.di.thanos.botcraft.gui.components.SimpleListCellRenderer;
import gr.uoa.di.thanos.botcraft.renderers.ColorComponent;

import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

/**
 * An application for editing configuration.
 * 
 * @author thanos
 */
public class Settings extends Application implements ActionListener, ChangeListener, KeyListener {
	private static final long serialVersionUID = 0L;
	private static final String FRAME_BUFFER_BITS = "frameBuffer_%1$s_Bits";
	private static final String ADVANCED = "advanced";
	private static final String ARE_YOU_SURE_YOU_WANT_TO_RESET_SETTINGS_ANY_UNSAVED_CHANGES_WILL_BE_LOST = "areYouSureYouWantToResetSettingsAnyUnsavedChangesWillBeLost";
	private static final String ARE_YOU_SURE_YOU_WANT_TO_RESTORE_DEFAULT_SETTINGS = "areYouSureYouWantToRestoreDefaultSettings";
	private static final String BOTCRAFT_SETTINGS = "botcraftSettings";
	private static final String CHANGING_LANGUAGE_REQUIRES_RESTART_TO_TAKE_EFFECT_DO_YOU_WANT_TO_RESTART_NOW = "changingLanguageRequiresRestartToTakeEffectDoYouWantToRestartNow";
	private static final String CONTROLS = "controls";
	private static final String DEPTH_BUFFER_BITS = "depthBufferBits";
	private static final String DISPLAY_FULL_SCREEN = "displayFullScreen";
	private static final String DISPLAY_MODE = "displayMode";
	private static final String ENABLE_DOUBLE_BUFFERING = "enableDoubleBuffering";
	private static final String ENABLE_HARDWARE_ACCELERATION = "enableHardwareAcceleration";
	private static final String ERROR_RESTORING_DEFAULT_SETTINGS = "errorRestoringDefaultSettings";
	private static final String ERROR_RESTORING_DEFAULT_SETTINGS_MESSAGE = "errorRestoringDefaultSettings";
	private static final String ERROR_SAVING_SETTINGS = "errorSavingSettings";
	private static final String ERROR_SAVING_SETTINGS_MESSAGE = "errorSavingSettings_";
	private static final String EXIT = "exit";
	private static final String FRAMES_PER_SECOND = "framesPerSecond";
	private static final String GENERAL = "general";
	private static final String LANGUAGE = "language";
	private static final String OPEN_CL_DEVICE = "openClDevice";
	private static final String OPEN_CL_PLATFORM = "openClPlatform";
	private static final String RESET = "reset";
	private static final String RESET_SETTINGS = "resetSettings";
	private static final String RESTART_REQUIRED = "restartRequired";
	private static final String RESTORE_DEFAULTS = "restoreDefaults";
	private static final String RESTORE_DEFAULT_SETTINGS = "restoreDefaultSettings";
	private static final String SAMPLE_BUFFERS = "sampleBuffers";
	private static final String SAVE = "save";
	private static final String SAVE_SETTINGS = "saveSettings";
	private static final String SCREEN = "screen";
	private static final String VIDEO = "video";
	private static final String WOULD_YOU_LIKE_TO_SAVE_YOUR_CHANGES_BEFORE_EXITING = "wouldYouLikeToSaveYourChangesBeforeExiting";
	private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());

	private final JComboBox<Locale> languages;
	private final JComboBox<GraphicsDevice> screens;
	private final JComboBox<DisplayMode> displayModes;
	private final JSpinner framesPerSecond;
	private final JCheckBox fullScreen;
	private final KeyboardControlField[] keyboardControls;
	private final JCheckBox hardwareAccelerated;
	private final JCheckBox doubleBuffered;
	private final JSpinner[] frameBufferBits;
	private final JSpinner depthBufferBits;
	private final JSpinner sampleBuffers;
	private final JComboBox<CLPlatform> clPlatforms;
	private final JComboBox<CLDevice> clDevices;
	private final JButton save;
	private final JButton reset;
	private final JButton restoreDefaults;
	private final JButton exit;

	/**
	 * Start a new settings in standalone mode.
	 * 
	 * @param arguments
	 *            ignored
	 */
	public static void main(final String[] arguments) {
		final Settings settings = new Settings(new Configuration());
		settings.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(settings);
	}

	/**
	 * Construct a new settings.
	 * 
	 * @param configuration
	 *            the configuration to use
	 */
	public Settings(final Configuration configuration) {
		super(configuration, configuration.format(BOTCRAFT_SETTINGS));
		final JTabbedPane tabbedPane = new JTabbedPane();
		final JPanel general = addPanel(tabbedPane, GENERAL);
		languages = addComboBox(general, LANGUAGE, configuration.getSupportedLanguages(), configuration.getLanguage(), true, new LanguageListCellRenderer());
		final JPanel video = addPanel(tabbedPane, VIDEO);
		screens = addComboBox(video, SCREEN, GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices(), configuration.getScreen(), true, new ScreenListCellRenderer());
		displayModes = addComboBox(video, DISPLAY_MODE, configuration.getScreen().getDisplayModes(), configuration.getDisplayMode(), configuration.getScreen().isDisplayChangeSupported(), new DisplayModeListCellRenderer(configuration));
		framesPerSecond = addSpinner(video, FRAMES_PER_SECOND, 1, Configuration.getMaxFramesPerSecond(configuration.getDisplayMode()), configuration.getFramesPerSecond());
		fullScreen = addCheckBox(video, DISPLAY_FULL_SCREEN, configuration.isFullScreen(), configuration.getScreen().isFullScreenSupported());
		final JPanel controls = addPanel(tabbedPane, CONTROLS);
		keyboardControls = new KeyboardControlField[KeyboardControl.values().length];
		for (final KeyboardControl control : KeyboardControl.values()) {
			keyboardControls[control.ordinal()] = addKeyboardControlField(controls, control.toString(), configuration.getKeyEvent(control));
		}
		final JPanel advanced = addPanel(tabbedPane, ADVANCED);
		hardwareAccelerated = addCheckBox(advanced, ENABLE_HARDWARE_ACCELERATION, configuration.isHardwareAccelerated(), Configuration.isHardwareAccelerated(configuration.getScreen()));
		doubleBuffered = addCheckBox(advanced, ENABLE_DOUBLE_BUFFERING, configuration.isDoubleBuffered(), Configuration.isDoubleBuffered(configuration.getScreen()));
		frameBufferBits = new JSpinner[ColorComponent.values().length];
		for (final ColorComponent colorComponent : ColorComponent.values()) {
			frameBufferBits[colorComponent.ordinal()] = addSpinner(advanced, String.format(FRAME_BUFFER_BITS, colorComponent.toString()), 1, Configuration.getMaxFrameBufferBits(configuration.getScreen(), colorComponent), configuration.getFrameBufferBits(colorComponent));
		}
		depthBufferBits = addSpinner(advanced, DEPTH_BUFFER_BITS, 1, Configuration.MAX_DEPTH_BUFFER_BITS, configuration.getDepthBufferBits());
		sampleBuffers = addSpinner(advanced, SAMPLE_BUFFERS, 0, Configuration.MAX_SAMPLE_BUFFERS, configuration.getSampleBuffers());
		clPlatforms = addComboBox(advanced, OPEN_CL_PLATFORM, CLPlatform.listCLPlatforms(), configuration.getClPlatform(), true, new ClPlatformListCellRenderer(configuration));
		clDevices = addComboBox(advanced, OPEN_CL_DEVICE, configuration.getClPlatform().listCLDevices(), configuration.getClDevice(), true, new ClDeviceListCellRenderer(configuration));
		add(tabbedPane);
		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		save = addButton(buttons, SAVE, false);
		reset = addButton(buttons, RESET, false);
		restoreDefaults = addButton(buttons, RESTORE_DEFAULTS, true);
		exit = addButton(buttons, EXIT, true);
		add(buttons);
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		stateChanged(null); // update save and reset
		Objects.requireNonNull(event, "Event must not be null");
		if (event.getSource() == screens) { // screen has changed, update display modes, frames per second, full screen, hardware accelerated, double buffered and frame buffer bits that depend on it
			updateDisplayModes(get(screens)); // frames per second will be updated as well
			updateFullScreen(get(screens));
			updateHardwareAccelerated(get(screens));
			updateDoubleBuffered(get(screens));
			updateFrameBufferBits(get(screens));
		} else if (event.getSource() == displayModes) { // display mode has changed, update frames per second that depend on it
			updateFramesPerSecond(get(displayModes));
		} else if (event.getSource() == clPlatforms) { // OpenCL platform has changed, update OpenCL devices that depend on it
			updateClDevices(get(clPlatforms));
		} else if ((event.getSource() == save) && save() && (JOptionPane.showConfirmDialog(Settings.this, configuration.format(CHANGING_LANGUAGE_REQUIRES_RESTART_TO_TAKE_EFFECT_DO_YOU_WANT_TO_RESTART_NOW), configuration.format(RESTART_REQUIRED), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) { // save configuration and restart if needed after user confirmation
			dispose();
			new Settings(configuration).run();
		} else if ((event.getSource() == reset) && isSaveRequired() && (JOptionPane.showConfirmDialog(Settings.this, configuration.format(ARE_YOU_SURE_YOU_WANT_TO_RESET_SETTINGS_ANY_UNSAVED_CHANGES_WILL_BE_LOST), configuration.format(RESET_SETTINGS), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) { // reset configuration after user confirmation
			reset();
		} else if ((event.getSource() == restoreDefaults) && (JOptionPane.showConfirmDialog(Settings.this, configuration.format(ARE_YOU_SURE_YOU_WANT_TO_RESTORE_DEFAULT_SETTINGS), configuration.format(RESTORE_DEFAULT_SETTINGS), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) { // restore defaults after user confirmation
			try {
				configuration.reset();
				reset();
			} catch (final BackingStoreException e) {
				LOGGER.log(Level.WARNING, "Error restoring default settings", e);
				JOptionPane.showMessageDialog(Settings.this, configuration.format(ERROR_RESTORING_DEFAULT_SETTINGS_MESSAGE, e.getMessage()), configuration.format(ERROR_RESTORING_DEFAULT_SETTINGS), JOptionPane.WARNING_MESSAGE);
			}
		} else if (event.getSource() == exit) { // exit
			if (isSaveRequired()) {
				switch (JOptionPane.showConfirmDialog(Settings.this, configuration.format(WOULD_YOU_LIKE_TO_SAVE_YOUR_CHANGES_BEFORE_EXITING), configuration.format(SAVE_SETTINGS), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				case JOptionPane.YES_OPTION:
					save();
				case JOptionPane.NO_OPTION:
					dispose(); // dispose anyway
				}
			} else {
				dispose();
			}
		}
	}

	@Override
	public void keyTyped(final KeyEvent event) {
	}

	@Override
	public void keyPressed(final KeyEvent event) {
		for (final KeyboardControl control : KeyboardControl.values()) {
			final KeyboardControlField keyboardControl = keyboardControls[control.ordinal()];
			if ((keyboardControl != event.getSource()) && controlMatches(control, event)) { // other control with same binding, unbound it
				keyboardControl.setKeyEvent(null);
			}
		}
		stateChanged(null); // update save and reset
	}

	@Override
	public void keyReleased(final KeyEvent event) {
	}

	@Override
	public void run() {
		super.run();
		if (languages.isEnabled()) {
			languages.requestFocus();
		}
	}

	@Override
	public void stateChanged(final ChangeEvent event) {
		save.setEnabled(isSaveRequired());
		reset.setEnabled(isSaveRequired());
	}

	private JPanel addPanel(final JTabbedPane tabbedPane, final String key) {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		tabbedPane.addTab(configuration.format(key), panel);
		return panel;
	}

	private void addLabel(final JPanel panel, final String key) {
		panel.add(new JLabel(configuration.format(key), JLabel.RIGHT));
	}

	private <T> JComboBox<T> addComboBox(final JPanel panel, final String labelKey, final T[] items, final T selected, final boolean enabled, final SimpleListCellRenderer<T> renderer) {
		addLabel(panel, labelKey);
		final JComboBox<T> comboBox = new JComboBox<T>();
		for (final T item : items) {
			comboBox.addItem(item);
		}
		comboBox.setSelectedItem(selected);
		comboBox.setEnabled((comboBox.getItemCount() > 1) && enabled);
		comboBox.setRenderer(renderer);
		comboBox.addActionListener(this);
		panel.add(comboBox);
		return comboBox;
	}

	private JSpinner addSpinner(final JPanel panel, final String labelKey, final int min, final int max, final int value) {
		addLabel(panel, labelKey);
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
		spinner.addChangeListener(this);
		panel.add(spinner);
		return spinner;
	}

	private JCheckBox addCheckBox(final JPanel panel, final String labelKey, final boolean value, final boolean enabled) {
		panel.add(new JLabel(configuration.format(labelKey), JLabel.RIGHT));
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(value);
		checkBox.setEnabled(enabled);
		checkBox.addActionListener(this);
		panel.add(checkBox);
		return checkBox;
	}

	private KeyboardControlField addKeyboardControlField(final JPanel panel, final String labelKey, final KeyEvent value) {
		addLabel(panel, labelKey);
		final KeyboardControlField keyboardControlField = new KeyboardControlField(configuration, value);
		keyboardControlField.addKeyListener(this);
		panel.add(keyboardControlField);
		return keyboardControlField;
	}

	private JButton addButton(final JPanel panel, final String key, final boolean enabled) {
		final JButton button = new JButton(configuration.format(key));
		button.setEnabled(enabled);
		button.addActionListener(this);
		panel.add(button);
		return button;
	}

	private <T> T get(final JComboBox<T> spinner) {
		return spinner.getItemAt(spinner.getSelectedIndex());
	}

	private int get(final JSpinner spinner) {
		return ((SpinnerNumberModel) spinner.getModel()).getNumber().intValue();
	}

	private boolean isSaveRequired() {
		if (!(get(languages).equals(configuration.getLanguage()) && get(screens).equals(configuration.getScreen()) && get(displayModes).equals(configuration.getDisplayMode()) && (get(framesPerSecond) == configuration.getFramesPerSecond()) && (fullScreen.isSelected() == configuration.isFullScreen()) && (hardwareAccelerated.isSelected() == configuration.isHardwareAccelerated()) && (doubleBuffered.isSelected() == configuration.isDoubleBuffered()) && (get(depthBufferBits) == configuration.getDepthBufferBits()) && (get(sampleBuffers) == configuration.getSampleBuffers()) && get(clPlatforms).equals(configuration.getClPlatform()) && get(clDevices).equals(configuration.getClDevice()))) { // any non-control setting has been modified
			return true;
		}
		for (final KeyboardControl control : KeyboardControl.values()) {
			if (!controlMatches(control, configuration.getKeyEvent(control))) { // control setting has been modified
				return true;
			}
		}
		for (final ColorComponent colorComponent : ColorComponent.values()) {
			if (get(frameBufferBits[colorComponent.ordinal()]) != configuration.getFrameBufferBits(colorComponent)) {
				return true;
			}
		}
		return false;
	}

	private boolean controlMatches(final KeyboardControl control, final KeyEvent event) {
		final KeyEvent controlEvent = keyboardControls[control.ordinal()].getKeyEvent();
		return (controlEvent == null) ? (event == null) : ((event != null) && (controlEvent.getKeyCode() == event.getKeyCode()) && (controlEvent.getKeyLocation() == event.getKeyLocation()) && (controlEvent.getModifiersEx() == event.getModifiersEx()));
	}

	private <T> void update(final JComboBox<T> comboBox, final T[] items, final T selected, final boolean enabled) {
		comboBox.removeActionListener(this);
		comboBox.removeAllItems();
		for (final T item : items) {
			comboBox.addItem(item);
		}
		comboBox.setSelectedItem(selected);
		comboBox.setEditable((comboBox.getItemCount() > 1) && enabled);
		comboBox.addActionListener(this);
	}

	private void update(final JSpinner spinner, final int value, final int max) {
		spinner.removeChangeListener(this);
		final SpinnerNumberModel model = (SpinnerNumberModel) framesPerSecond.getModel();
		final int v = model.getNumber().intValue();
		model.setValue((v > max) ? max : v);
		model.setMaximum(max);
		spinner.addChangeListener(this);
	}

	private void update(final JCheckBox checkBox, final boolean selected, final boolean enabled) {
		checkBox.removeActionListener(this);
		checkBox.setSelected(selected);
		checkBox.setEnabled(enabled);
		checkBox.addActionListener(this);
	}

	private void updateDisplayModes(final GraphicsDevice screen) {
		// if saved screen is selected then select saved display mode as well
		update(displayModes, screen.getDisplayModes(), screen.equals(configuration.getScreen()) ? configuration.getDisplayMode() : screen.getDisplayMode(), screen.isDisplayChangeSupported());
		updateFramesPerSecond(get(displayModes));
	}

	private void updateFramesPerSecond(final DisplayMode displayMode) {
		// if saved display mode is selected then select saved frames per second as well
		update(framesPerSecond, displayMode.equals(configuration.getDisplayMode()) ? configuration.getFramesPerSecond() : get(framesPerSecond), Configuration.getMaxFramesPerSecond(displayMode));
	}

	private void updateFullScreen(final GraphicsDevice screen) {
		// if saved screen is selected then select saved full screen as well
		update(fullScreen, screen.equals(configuration.getScreen()) ? configuration.isFullScreen() : false, screen.isFullScreenSupported());
	}

	private void updateHardwareAccelerated(final GraphicsDevice screen) {
		// if saved screen is selected then select saved hardware accelerated as well
		update(hardwareAccelerated, screen.equals(configuration.getScreen()) ? configuration.isHardwareAccelerated() : Configuration.isHardwareAccelerated(screen), Configuration.isHardwareAccelerated(screen));
	}

	private void updateDoubleBuffered(final GraphicsDevice screen) {
		// if saved screen is selected then select saved double buffered as well
		update(doubleBuffered, screen.equals(configuration.getScreen()) ? configuration.isDoubleBuffered() : Configuration.isDoubleBuffered(screen), Configuration.isDoubleBuffered(screen));
	}

	private void updateFrameBufferBits(final GraphicsDevice screen) {
		for (final ColorComponent colorComponent : ColorComponent.values()) {
			// if saved screen is selected then select saved frame buffer bits as well
			update(frameBufferBits[colorComponent.ordinal()], screen.equals(configuration.getScreen()) ? configuration.getFrameBufferBits(colorComponent) : get(frameBufferBits[colorComponent.ordinal()]), Configuration.getMaxFrameBufferBits(screen, colorComponent));
		}
	}

	private void updateClDevices(final CLPlatform clPlatform) {
		// if saved OpenCL platform is selected then select saved OpenCL device as well
		update(clDevices, clPlatform.listCLDevices(), clPlatform.equals(configuration.getClPlatform()) ? configuration.getClDevice() : clPlatform.getMaxFlopsDevice(), true);
	}

	private boolean save() {
		try {
			final boolean restartRequired = !get(languages).equals(configuration.getLanguage());
			configuration.setLanguage(get(languages));
			configuration.setScreen(get(screens));
			configuration.setDisplayMode(get(displayModes));
			configuration.setFramesPerSecond(get(framesPerSecond));
			configuration.setFullScreen(fullScreen.isSelected());
			for (final KeyboardControl control : KeyboardControl.values()) {
				configuration.setKeyEvent(control, keyboardControls[control.ordinal()].getKeyEvent());
			}
			configuration.setHardwareAccelerated(hardwareAccelerated.isSelected());
			configuration.setDoubleBuffered(doubleBuffered.isSelected());
			for (final ColorComponent colorComponent : ColorComponent.values()) {
				configuration.setFrameBufferBits(colorComponent, get(frameBufferBits[colorComponent.ordinal()]));
			}
			configuration.setDepthBufferBits(get(depthBufferBits));
			configuration.setSampleBuffers(get(sampleBuffers));
			configuration.setClPlatform(get(clPlatforms));
			configuration.setClDevice(get(clDevices));
			stateChanged(null); // update save and reset
			return restartRequired;
		} catch (final BackingStoreException e) {
			LOGGER.log(Level.WARNING, "Error saving settings", e);
			JOptionPane.showMessageDialog(Settings.this, configuration.format(ERROR_SAVING_SETTINGS_MESSAGE, e.getMessage()), configuration.format(ERROR_SAVING_SETTINGS), JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	private void reset() {
		languages.setSelectedItem(configuration.getLanguage());
		screens.setSelectedItem(configuration.getScreen());
		updateDisplayModes(get(screens)); // saved display mode will be selected and frames per second will be updated as well
		updateFullScreen(get(screens)); // saved full screen will be selected
		for (final KeyboardControl control : KeyboardControl.values()) {
			keyboardControls[control.ordinal()].setKeyEvent(configuration.getKeyEvent(control));
		}
		updateHardwareAccelerated(configuration.getScreen());
		updateDoubleBuffered(configuration.getScreen());
		updateFrameBufferBits(configuration.getScreen());
		depthBufferBits.setValue(configuration.getDepthBufferBits());
		sampleBuffers.setValue(configuration.getSampleBuffers());
		clPlatforms.setSelectedItem(configuration.getClPlatform());
		updateClDevices(get(clPlatforms)); // saved OpenCL device will be selected
	}
}
