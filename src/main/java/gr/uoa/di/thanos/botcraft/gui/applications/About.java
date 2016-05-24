package gr.uoa.di.thanos.botcraft.gui.applications;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * An application displaying information about BotCraft.
 * 
 * @author thanos
 */
public class About extends Application {
	private static final long serialVersionUID = 0L;
	private static final String ERROR_LOADING_ABOUT = "errorLoadingAbout";
	private static final String ERROR_LOADING_ABOUT_MESSAGE = "errorLoadingAbout_";
	private static final String ABOUT_BOTCRAFT = "aboutBotcraft_";
	private static final String VERSION = "VERSION";
	private static final Logger LOGGER = Logger.getLogger(About.class.getName());

	private final JEditorPane about;

	/**
	 * Start a new about in standalone mode.
	 * 
	 * @param arguments
	 *            ignored
	 */
	public static void main(final String[] arguments) {
		final Configuration configuration = new Configuration();
		try {
			final About about = new About(configuration);
			about.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			SwingUtilities.invokeLater(about);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error loading about", e);
			JOptionPane.showMessageDialog(null, configuration.format(ERROR_LOADING_ABOUT_MESSAGE, e.getMessage()), configuration.format(ERROR_LOADING_ABOUT), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Construct a new about
	 * 
	 * @param configuration
	 *            the configuration to use
	 * @throws IOException
	 *             if the content can not be loaded
	 */
	public About(final Configuration configuration) throws IOException {
		super(configuration, configuration.format(ABOUT_BOTCRAFT, configuration.format(VERSION)));
		final Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		final Dimension size = new Dimension(Double.valueOf(bounds.getWidth()).intValue(), Double.valueOf(bounds.getHeight()).intValue());
		setMaximumSize(size);
		setPreferredSize(size);
		about = new JEditorPane(configuration.getAbout());
		about.setEditable(false);
		add(new JScrollPane(about, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	}

	@Override
	public void run() {
		super.run();
		about.requestFocus();
	}
}
