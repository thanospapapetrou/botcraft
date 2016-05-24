package gr.uoa.di.thanos.botcraft.gui.applications;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;
import gr.uoa.di.thanos.botcraft.game.Map;
import gr.uoa.di.thanos.botcraft.game.Terrain;
import gr.uoa.di.thanos.botcraft.game.Tile;
import gr.uoa.di.thanos.botcraft.gui.components.MapPanel;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.SwingUtilities;

/**
 * An application for editing maps.
 * 
 * @author thanos
 */
public class MapEditor extends Application implements Runnable {
	private static final long serialVersionUID = 0L;
	private static final String BOTCRAFT_MAP_EDITOR = "botcraftMapEditor";

	private final MapPanel mapPanel;

	/**
	 * Start a new map editor in standalone mode.
	 * 
	 * @param arguments
	 *            ignored
	 */
	public static void main(final String[] arguments) {
		SwingUtilities.invokeLater(new MapEditor(new Configuration()));
	}

	/**
	 * Construct a new map editor.
	 * 
	 * @param configuration
	 *            the configuration to use
	 */
	public MapEditor(final Configuration configuration) {
		super(configuration, configuration.format(BOTCRAFT_MAP_EDITOR));
		final Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		final Dimension size = new Dimension(Double.valueOf(bounds.getWidth()).intValue(), Double.valueOf(bounds.getHeight()).intValue());
		setMaximumSize(size);
		setPreferredSize(size);
		final Map map = new Map(200, 200, Terrain.ROCK);
		for (int latitude = 0; latitude < map.getLatitudinalSize(); latitude++) {
			for (int longitude = 0; longitude < map.getLongitudinalSize(); longitude++) {
				if (latitude % 9 == 0) {
					if (longitude % 11 == 0) {
						final int n = latitude * map.getLongitudinalSize() + longitude;
						map.setTile(latitude, longitude, new Tile(n % 8.0f, Terrain.values()[n % Terrain.values().length]));
					}
				}
			}
		}
		mapPanel = new MapPanel(configuration, map);
		add(mapPanel);
	}

	@Override
	public void run() {
		super.run();
		mapPanel.requestFocus();
	}
}
