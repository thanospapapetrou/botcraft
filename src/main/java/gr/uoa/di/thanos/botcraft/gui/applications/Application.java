package gr.uoa.di.thanos.botcraft.gui.applications;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

/**
 * A BotCraft application that can be started either in standalone mode or via launcher.
 * 
 * @author thanos
 */
public class Application extends JFrame implements Runnable {
	private static final long serialVersionUID = 0L;

	/**
	 * The configuration to use.
	 */
	protected final Configuration configuration;

	/**
	 * Construct a new application.
	 * 
	 * @param configuration
	 *            the configuration to use
	 * @param title
	 *            the title of the window
	 */
	public Application(final Configuration configuration, final String title) {
		super();
		Objects.requireNonNull(configuration, "Configuration must not be null");
		Objects.requireNonNull(title, "Title must not be null");
		if (title.isEmpty()) {
			throw new IllegalArgumentException("Title must not be empty");
		}
		this.configuration = configuration;
		setTitle(title);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); TODO do not close on exit?
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
	}

	@Override
	public void run() {
		pack();
		setVisible(true);
	}
}
