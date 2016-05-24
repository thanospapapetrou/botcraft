package gr.uoa.di.thanos.botcraft.gui.components;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.util.Objects;

import com.jogamp.opencl.CLPlatform;

/**
 * Simple list cell renderer for OpenCL platforms.
 * 
 * @author thanos
 */
public class ClPlatformListCellRenderer extends SimpleListCellRenderer<CLPlatform> {
	private static final String CL_PLATFORM = "CL_PLATFORM";

	private final Configuration configuration;

	/**
	 * Construct a new OpenCL platform list cell renderer.
	 * 
	 * @param configuration
	 *            the configuration to use for localization
	 */
	public ClPlatformListCellRenderer(final Configuration configuration) {
		Objects.requireNonNull(configuration, "Configuration must not be null");
		this.configuration = configuration;
	}

	@Override
	protected String item2String(final CLPlatform clPlatform) {
		Objects.requireNonNull(clPlatform, "OpenCL platform must not be null");
		return configuration.format(CL_PLATFORM, clPlatform.getName().trim(), clPlatform.getVersion().toString(), configuration.format(clPlatform.getProfile()), clPlatform.getVendor().trim());
	}
}
