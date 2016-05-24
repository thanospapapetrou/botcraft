package gr.uoa.di.thanos.botcraft.gui.applications;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * An application for launching other BotCraft applications.
 * 
 * @author thanos
 */
public class Launcher extends Application implements ActionListener {
	private static final long serialVersionUID = 0L;
	private static final String BACKGROUND = "/gr/uoa/di/thanos/botcraft/botcraft.png";
	private static final String ABOUT = "about";
	private static final String BOTCRAFT_LAUNCHER = "botcraftLauncher";
	private static final String ERROR_LOADING_ABOUT = "errorLoadingAbout";
	private static final String ERROR_LOADING_ABOUT_MESSAGE = "errorLoadingAbout_";
	private static final String EXIT = "exit";
	private static final String GAME_EDITOR = "gameEditor";
	private static final String MAP_EDITOR = "mapEditor";
	private static final String NEW_GAME = "newGame";
	private static final String SETTINGS = "settings";

	private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	private final JButton newGame;
	private final JButton settings;
	private final JButton mapEditor;
	private final JButton gameEditor;
	private final JButton about;
	private final JButton exit;

	/**
	 * Start a new launcher in standalone mode.
	 * 
	 * @param arguments
	 *            ignored
	 */
	public static void main(final String[] arguments) {
		final Launcher launcher = new Launcher(new Configuration());
		launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(launcher);
	}

	/**
	 * Construct a new launcher.
	 * 
	 * @param configuration
	 *            the configuration to use
	 */
	public Launcher(final Configuration configuration) {
		super(configuration, configuration.format(BOTCRAFT_LAUNCHER));
		setBackground();
		newGame = addButton(NEW_GAME);
		mapEditor = addButton(MAP_EDITOR);
		gameEditor = addButton(GAME_EDITOR);
		settings = addButton(SETTINGS);
		about = addButton(ABOUT);
		exit = addButton(EXIT);
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		Objects.requireNonNull(event, "Event must not be null");
		if (event.getSource() == newGame) {
			// TODO
		} else if (event.getSource() == mapEditor) {
			SwingUtilities.invokeLater(new MapEditor(configuration));
		} else if (event.getSource() == gameEditor) {
			// TODO
		} else if (event.getSource() == settings) {
			SwingUtilities.invokeLater(new Settings(configuration));
		} else if (event.getSource() == about) {
			try {
				SwingUtilities.invokeLater(new About(configuration));
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "Error loading about", e);
				JOptionPane.showMessageDialog(this, configuration.format(ERROR_LOADING_ABOUT_MESSAGE, e.getMessage()), configuration.format(ERROR_LOADING_ABOUT), JOptionPane.WARNING_MESSAGE);
			}
		} else if (event.getSource() == exit) {
			dispose();
			System.exit(0);
		}
	}

	@Override
	public void run() {
		super.run();
		newGame.requestFocus();
	}

	private void setBackground() {
		try {
			final Image background = ImageIO.read(getClass().getResource(BACKGROUND));
			final JPanel contentPane = new JPanel() {
				private static final long serialVersionUID = 0L;

				@Override
				protected void paintComponent(final Graphics graphics) {
					graphics.drawImage(background, 0, 0, Color.BLACK, null);
				}
			};
			final Dimension size = new Dimension(background.getWidth(null), background.getHeight(null));
			contentPane.setMinimumSize(size);
			contentPane.setMaximumSize(size);
			contentPane.setPreferredSize(size);
			setContentPane(contentPane);
			setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // resetting layout is necessary because content pane has changed
			setResizable(false);
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Error loading background image", e);
		}
	}

	private JButton addButton(final String key) {
		final JButton button = new JButton(configuration.format(key));
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.addActionListener(this);
		add(button);
		return button;
	}
}
