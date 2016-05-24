package gr.uoa.di.thanos.botcraft.etc.configuration;

import java.util.Objects;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.util.Filter;

/**
 * Filter for OpenCL devices that support compilation and memory sharing with OpenGL.
 * 
 * @author thanos
 */
public class ClGlDeviceFilter implements Filter<CLDevice> {
	@Override
	public boolean accept(final CLDevice clDevice) {
		Objects.requireNonNull(clDevice, "OpenCL device must not be null");
		return clDevice.isCompilerAvailable() && clDevice.isGLMemorySharingSupported();
	}
}
