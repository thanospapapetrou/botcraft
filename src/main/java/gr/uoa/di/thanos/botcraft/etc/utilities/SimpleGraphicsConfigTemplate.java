package gr.uoa.di.thanos.botcraft.etc.utilities;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.Objects;

/**
 * A simple implementations of graphics configuration template that filters graphics configurations that support translucency and have an RGBA color model and then uses a {@link SimpleGraphicsConfigurationComparator} to choose the best.
 * 
 * @author thanos
 */
public class SimpleGraphicsConfigTemplate extends GraphicsConfigTemplate {
	private static final long serialVersionUID = 0L;

	@Override
	public GraphicsConfiguration getBestConfiguration(final GraphicsConfiguration[] graphicsConfigurations) {
		Objects.requireNonNull(graphicsConfigurations, "Graphics configurations must not be null");
		if (graphicsConfigurations.length == 0) {
			throw new IllegalArgumentException("Graphics configurations must not be empty");
		}
		Arrays.sort(graphicsConfigurations, 0, graphicsConfigurations.length, new SimpleGraphicsConfigurationComparator());
		return graphicsConfigurations[0];
	}

	@Override
	public boolean isGraphicsConfigSupported(final GraphicsConfiguration graphicsConfiguration) {
		Objects.requireNonNull(graphicsConfiguration, "Graphics configuration must not be null");
		if (graphicsConfiguration.isTranslucencyCapable()) {
			final ColorModel colorModel = graphicsConfiguration.getColorModel(Transparency.TRANSLUCENT);
			if (colorModel != null) {
				return colorModel.hasAlpha() && (colorModel.getTransparency() == Transparency.TRANSLUCENT) && (colorModel.getColorSpace().getType() == ColorSpace.TYPE_RGB);
			}
		}
		return false;
	}
}
