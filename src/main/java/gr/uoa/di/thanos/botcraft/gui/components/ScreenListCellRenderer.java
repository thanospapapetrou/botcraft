package gr.uoa.di.thanos.botcraft.gui.components;

import java.awt.GraphicsDevice;
import java.util.Objects;

/**
 * Simple list cell renderer for screens.
 * 
 * @author thanos
 */
public class ScreenListCellRenderer extends SimpleListCellRenderer<GraphicsDevice> {
	@Override
	protected String item2String(final GraphicsDevice screen) {
		Objects.requireNonNull(screen, "Screen must not be null");
		return screen.getIDstring();
	}
}
