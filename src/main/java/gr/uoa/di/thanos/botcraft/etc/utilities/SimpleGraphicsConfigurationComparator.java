package gr.uoa.di.thanos.botcraft.etc.utilities;

import java.awt.BufferCapabilities;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.util.Comparator;
import java.util.Objects;

/**
 * Simple implementation of comparator for graphics configurations that sorts graphics configurations first by pixel size (larger pixel size first), then by front buffer acceleration (accelerated front buffer first) and finally by back buffer acceleration (accelerated back buffer first).
 * 
 * @author thanos
 */
public class SimpleGraphicsConfigurationComparator implements Comparator<GraphicsConfiguration> {
	private static int compare(final ImageCapabilities imageCapabilities1, final ImageCapabilities imageCapabilities2) {
		return imageCapabilities1.isAccelerated() ? (imageCapabilities2.isAccelerated() ? 0 : -1) : (imageCapabilities2.isAccelerated() ? 1 : 0);
	}

	@Override
	public int compare(final GraphicsConfiguration graphicsConfiguration1, final GraphicsConfiguration graphicsConfiguration2) {
		Objects.requireNonNull(graphicsConfiguration1, "Graphics configuration 1 must not be null");
		Objects.requireNonNull(graphicsConfiguration2, "Graphics configuration 2 must not be null");
		final int pixelSizeComparison = graphicsConfiguration2.getColorModel().getPixelSize() - graphicsConfiguration1.getColorModel().getPixelSize(); // reverse ordering
		final BufferCapabilities bufferCapabilities1 = graphicsConfiguration1.getBufferCapabilities();
		final BufferCapabilities bufferCapabilities2 = graphicsConfiguration2.getBufferCapabilities();
		final int frontBufferComparison = compare(bufferCapabilities1.getFrontBufferCapabilities(), bufferCapabilities2.getFrontBufferCapabilities());
		final int backBufferComparison = compare(bufferCapabilities1.getBackBufferCapabilities(), bufferCapabilities2.getBackBufferCapabilities());
		return (pixelSizeComparison == 0) ? ((frontBufferComparison == 0) ? backBufferComparison : frontBufferComparison) : pixelSizeComparison;
	}
}
