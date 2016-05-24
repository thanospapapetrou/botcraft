package gr.uoa.di.thanos.botcraft.gui.components;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.awt.DisplayMode;
import java.util.Objects;

/**
 * Simple list cell renderer for display modes.
 * 
 * @author thanos
 */
public class DisplayModeListCellRenderer extends SimpleListCellRenderer<DisplayMode> {
	private static final String DISPLAY_MODE_PIXELS = "DISPLAY_MODE_PIXELS";
	private static final String DISPLAY_MODE_PIXELS_COLORS = "DISPLAY_MODE_PIXELS_COLORS";
	private static final String DISPLAY_MODE_PIXELS_COLORS_HZ = "DISPLAY_MODE_PIXELS_COLORS_HZ";
	private static final String DISPLAY_MODE_PIXELS_HZ = "DISPLAY_MODE_PIXELS_HZ";

	private final Configuration configuration;

	/**
	 * Construct a new display mode list cell renderer.
	 * 
	 * @param configuration
	 *            the configuration to use for localization
	 */
	public DisplayModeListCellRenderer(final Configuration configuration) {
		Objects.requireNonNull(configuration, "Configuration must not be null");
		this.configuration = configuration;
	}

	@Override
	protected String item2String(final DisplayMode displayMode) {
		Objects.requireNonNull(displayMode, "Display mode must not be null");
		if ((displayMode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) && (displayMode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN)) {
			return configuration.format(DISPLAY_MODE_PIXELS, displayMode.getWidth(), displayMode.getHeight());
		} else if (displayMode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) {
			return configuration.format(DISPLAY_MODE_PIXELS_HZ, displayMode.getWidth(), displayMode.getHeight(), displayMode.getRefreshRate());
		} else if (displayMode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN) {
			return configuration.format(DISPLAY_MODE_PIXELS_COLORS, displayMode.getWidth(), displayMode.getHeight(), 1L << displayMode.getBitDepth());
		} else {
			return configuration.format(DISPLAY_MODE_PIXELS_COLORS_HZ, displayMode.getWidth(), displayMode.getHeight(), 1L << displayMode.getBitDepth(), displayMode.getRefreshRate());
		}
	}
}
