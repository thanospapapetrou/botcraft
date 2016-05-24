package gr.uoa.di.thanos.botcraft.gui.components;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.util.Objects;

import com.jogamp.opencl.CLDevice;

/**
 * Simple list cell renderer for OpenCL devices.
 * 
 * @author thanos
 */
public class ClDeviceListCellRenderer extends SimpleListCellRenderer<CLDevice> {
	private static final int MHZ = 1000 * 1000;
	private static final int GHZ = 1000 * MHZ;
	private static final String CL_DEVICE = "CL_DEVICE";
	private static final String FREQUENCY_MHZ = "FREQUENCY_MHZ";
	private static final String FREQUENCY_GHZ = "FREQUENCY_GHZ";

	private final Configuration configuration;

	/**
	 * Construct a new OpenCL device list cell renderer.
	 * 
	 * @param configuration
	 *            the configuration to use for localization
	 */
	public ClDeviceListCellRenderer(final Configuration configuration) {
		Objects.requireNonNull(configuration, "Configuration must not be null");
		this.configuration = configuration;
	}

	@Override
	protected String item2String(final CLDevice clDevice) {
		Objects.requireNonNull(clDevice, "OpenCL device must not be null");
		return configuration.format(CL_DEVICE, clDevice.getName().trim(), configuration.format(clDevice.getType().toString()), clDevice.getMaxComputeUnits(), formatFrequency(clDevice.getMaxClockFrequency()), clDevice.getVendor().trim());
	}

	private String formatFrequency(final int frequency) {
		return configuration.format((frequency > GHZ / MHZ) ? FREQUENCY_GHZ : FREQUENCY_MHZ, (frequency > GHZ / MHZ) ? frequency * ((float) MHZ) / GHZ : frequency);
	}
}
