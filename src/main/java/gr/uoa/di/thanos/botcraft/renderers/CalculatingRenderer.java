package gr.uoa.di.thanos.botcraft.renderers;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLException;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

/**
 * Base class containing common implementation for renderers that use OpenCL to calculate data for some of their vertex buffers.
 * 
 * @author thanos
 */
public class CalculatingRenderer extends Renderer {
	/**
	 * Object oriented wrapper of an OpenGL vertex buffer whose data will be calculated using OpenCL.
	 * 
	 * @author thanos
	 * @param <T>
	 *            the type of the data of this calculating vertex buffer
	 */
	protected class CalculatingVertexBuffer<T extends Number> extends VertexBuffer<T> {
		private CLGLBuffer<?> buffer;

		/**
		 * Construct a new calculating vertex buffer.
		 * 
		 * @param name
		 *            the name of the GLSL attribute to bind this calculating vertex buffer to
		 * @param triangles
		 *            the number of triangles contained in this calculating vertex buffer
		 * @param elements
		 *            the number of elements per triangle vertex contained in this calculating vertex buffer
		 * @param type
		 *            the class corresponding to the type of the data of this calculating vertex buffer
		 */
		protected CalculatingVertexBuffer(final String name, final int triangles, final int elements, final Class<T> type) {
			super(name, triangles, elements, GL.GL_DYNAMIC_DRAW, type);
		}

		@Override
		protected void dispose(final GL3 gl) {
			buffer.release();
			super.dispose(gl);
		}

		@Override
		protected void initialize(final GL3 gl) {
			super.initialize(gl);
			buffer = context.createFromGLBuffer(vertexBuffer, calculateTotalElements(triangles, elements), CLMemory.Mem.WRITE_ONLY);
		}
	}

	private static final String DEFINITION = "#define %1$s %2$s\n";
	private static final int BUFFER_SIZE = 1024;
	private static final Logger LOGGER = Logger.getLogger(CalculatingRenderer.class.getName());

	private final CLDevice device;
	private final CLGLContext context;
	private final CLProgram program;
	private final java.util.Map<String, CLKernel> kernels;
	private final CLCommandQueue queue;

	private static String loadProgram(final String program, final Map<String, String> definitions) throws RendererException {
		try (final InputStreamReader reader = new InputStreamReader(CalculatingRenderer.class.getResourceAsStream(program), StandardCharsets.UTF_8)) {
			final StringBuilder source = new StringBuilder();
			for (final Map.Entry<String, String> definition : definitions.entrySet()) {
				source.append(String.format(DEFINITION, definition.getKey(), definition.getValue()));
			}
			final char[] buffer = new char[BUFFER_SIZE];
			int read = 0;
			while ((read = reader.read(buffer)) != -1) {
				source.append(buffer, 0, read);
			}
			return source.toString();
		} catch (final IOException e) {
			throw new RendererException("Error loading CL program " + program, e);
		}
	}

	private static PointerBuffer intArray2PointerBuffer(final int[] data) {
		final PointerBuffer buffer = PointerBuffer.allocateDirect(data.length);
		for (int i = 0; i < data.length; i++) {
			buffer.put(i, data[i]);
		}
		return buffer;
	}

	private static long calculateWorkGroupSize(final PointerBuffer localWorkSizes) {
		long workGroupSize = 1L;
		for (int i = 0; i < localWorkSizes.capacity(); i++) {
			workGroupSize *= localWorkSizes.get(i);
		}
		return workGroupSize;
	}

	/**
	 * Construct a new calculating renderer.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @param vertexShader
	 *            the GLSL source code resource to use as vertex shader
	 * @param vertexShaderDefinitions
	 *            the definitions to include in the vertex shader source code (using <code>#define</code>)
	 * @param fragmentShader
	 *            the GLSL source code resource to use as fragment shader
	 * @param fragmentShaderDefinitions
	 *            the definitions to include in the fragment shader source code (using <code>#define</code>)
	 * @param configuration
	 *            the configuration to use
	 * @param program
	 *            the CL source code to use as CL program
	 * @param programDefinitions
	 *            the definitions to include in the program source code (using <code>#define</code>)
	 * @throws RendererException
	 *             if any errors occur
	 */
	protected CalculatingRenderer(final GL3 gl, final String vertexShader, final Map<String, String> vertexShaderDefinitions, final String fragmentShader, final Map<String, String> fragmentShaderDefinitions, final Configuration configuration, final String program, final Map<String, String> programDefinitions) throws RendererException {
		super(gl, vertexShader, vertexShaderDefinitions, fragmentShader, fragmentShaderDefinitions);
		Objects.requireNonNull(configuration, "Configuration must not be null");
		Objects.requireNonNull(program, "Program must not be null");
		if (program.isEmpty()) {
			throw new IllegalArgumentException("Program must not be empty");
		}
		Objects.requireNonNull(programDefinitions, "Program definitions must not be null");
		device = configuration.getClDevice();
		context = CLGLContext.create(gl.getContext(), device);
		this.program = context.createProgram(loadProgram(program, programDefinitions));
		LOGGER.info("Loaded CL program " + program);
		try {
			this.program.build(device);
			if (this.program.getBuildStatus(device) == CLProgram.Status.BUILD_ERROR) {
				final String buildLog = this.program.getBuildLog(device);
				this.program.release();
				context.release();
				throw new RendererException("Error compiling CL program " + program, buildLog);
			}
			LOGGER.info("Compiled CL program " + program);
			kernels = this.program.createCLKernels();
			queue = device.createCommandQueue();
		} catch (final CLException.CLBuildProgramFailureException e) {
			final String buildLog = this.program.getBuildLog(device);
			this.program.release();
			context.release();
			throw new RendererException("Error compiling CL program " + program, buildLog);
		}
	}

	@Override
	public void dispose(final GL3 gl) {
		Objects.requireNonNull(gl, "OpenGL must not be null");
		queue.release();
		for (final CLKernel kernel : kernels.values()) {
			kernel.release();
		}
		program.release();
		context.release();
		super.dispose(gl);
	}

	/**
	 * Allocate an OpenCL buffer.
	 * 
	 * @param size
	 *            the size of the buffer to allocate
	 * @param type
	 *            the class corresponding to the type of the elements of the buffer to allocate (must be either {@link Float}, {@link Integer} or a subclass of these types)
	 * @return the OpenCL buffer allocated
	 */
	protected CLBuffer<?> allocate(final int size, final Class<? extends Number> type) {
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be positive");
		}
		Objects.requireNonNull(type, "Type must not be null");
		if (Float.class.isAssignableFrom(type)) {
			return context.createFloatBuffer(size, CLMemory.Mem.USE_BUFFER, CLMemory.Mem.READ_WRITE);
		} else if (Integer.class.isAssignableFrom(type)) {
			return context.createIntBuffer(size, CLMemory.Mem.USE_BUFFER, CLMemory.Mem.READ_WRITE);
		} else {
			throw new IllegalStateException("Type must be either " + Float.class.getName() + ",  " + Integer.class.getName() + " or a subclass of these types");
		}
	}

	/**
	 * Wrap an array in an OpenCL buffer.
	 * 
	 * @param <T>
	 *            the type of the elements of the buffer to allocate
	 * @param data
	 *            the array to wrap (must be either an array of {@link Float}s, an array of {@link Integer}s or an array of a subclass of these types)
	 * @return an OpenCL buffer wrapping the array
	 */
	protected <T> CLBuffer<?> wrapInClBuffer(final T[] data) {
		Objects.requireNonNull(data, "Data must not be null");
		if (data.length == 0) {
			throw new IllegalArgumentException("Data must not be empty");
		}
		if (Float.class.isAssignableFrom(data.getClass().getComponentType())) {
			return context.createBuffer(Buffers.newDirectFloatBuffer(objectArray2PrimitiveArray(new Float[0].getClass().cast(data))), CLMemory.Mem.USE_BUFFER, CLMemory.Mem.READ_ONLY);
		} else if (Integer.class.isAssignableFrom(data.getClass().getComponentType())) {
			return context.createBuffer(Buffers.newDirectIntBuffer(objectArray2PrimitiveArray(new Integer[0].getClass().cast(data))), CLMemory.Mem.USE_BUFFER, CLMemory.Mem.READ_ONLY);
		} else {
			throw new IllegalStateException("Data must be either an array of " + Float.class.getName() + "s, an array of " + Integer.class.getName() + "s or an array of a subclass of these types");
		}
	}

	/**
	 * Execute an OpenCL kernel.
	 * 
	 * @param kernel
	 *            the kernel to execute
	 * @param dimensions
	 *            the dimensions of the kernel to execute
	 * @param offsets
	 *            the offsets used to calculate the global ID of a work item (one for each dimension)
	 * @param sizes
	 *            the numbers of work items that will execute the kernel (one for each dimension)
	 * @param arguments
	 *            the arguments to pass to the kernel (must be either a {@link Float}, an {@link Integer}, a {@link CLMemory} or a {@link CalculatingVertexBuffer})
	 */
	protected void executeKernel(final String kernel, final int dimensions, final int offsets[], final int sizes[], final Object... arguments) {
		Objects.requireNonNull(kernel, "Kernel must not be null");
		if (kernel.isEmpty()) {
			throw new IllegalArgumentException("Kernel must not be empty");
		}
		if (dimensions <= 0) {
			throw new IllegalArgumentException("Dimensions must be positive");
		}
		if (dimensions > device.getMaxWorkItemDimensions()) {
			throw new IllegalArgumentException("Dimensions must at most " + device.getMaxWorkItemDimensions());
		}
		Objects.requireNonNull(offsets, "Offsets must not be null");
		if (offsets.length < dimensions) {
			throw new IllegalArgumentException("Offsets size must be " + dimensions);
		}
		Objects.requireNonNull(sizes, "Sizes must not be null");
		if (sizes.length < dimensions) {
			throw new IllegalArgumentException("Sizes size must be " + dimensions);
		}
		for (int i = 0; i < arguments.length; i++) {
			if ((!Float.class.isInstance(arguments[i])) && (!Integer.class.isInstance(arguments[i])) && (!CLMemory.class.isInstance(arguments[i])) && (!CalculatingVertexBuffer.class.isInstance(arguments[i]))) {
				throw new IllegalStateException("Argument " + i + " must be either  a " + Float.class.getName() + ", an " + Integer.class.getName() + ", a " + CLMemory.class.getName() + " or a " + CalculatingVertexBuffer.class.getName());
			}
			if ((CLBuffer.class.isInstance(arguments[i])) && (CLBuffer.class.cast(arguments[i]).isReadOnly())) {
				queue.putWriteBuffer(CLBuffer.class.cast(arguments[i]), false);
			}
			if (CalculatingVertexBuffer.class.isInstance(arguments[i])) {
				queue.putAcquireGLObject(CalculatingVertexBuffer.class.cast(arguments[i]).buffer);
			}
		}
		kernels.get(kernel).rewind();
		for (final Object argument : arguments) {
			if (Float.class.isInstance(argument)) {
				kernels.get(kernel).putArg(Float.class.cast(argument));
			} else if (Integer.class.isInstance(argument)) {
				kernels.get(kernel).putArg(Integer.class.cast(argument));
			} else if (CLMemory.class.isInstance(argument)) {
				kernels.get(kernel).putArg(CLMemory.class.cast(argument));
			} else if (CalculatingVertexBuffer.class.isInstance(argument)) {
				kernels.get(kernel).putArg(CalculatingVertexBuffer.class.cast(argument).buffer);
			}
		}
		queue.putNDRangeKernel(kernels.get(kernel), dimensions, intArray2PointerBuffer(offsets), calculateGlobalWorkSizes(dimensions, sizes), calculateLocalWorkSizes(dimensions));
		for (final Object argument : arguments) {
			if (CalculatingVertexBuffer.class.isInstance(argument)) {
				queue.putReleaseGLObject(CalculatingVertexBuffer.class.cast(argument).buffer);
			}
		}
		queue.flush();
	}

	private PointerBuffer calculateLocalWorkSizes(final int dimensions) {
		final PointerBuffer localWorkSizes = PointerBuffer.allocateDirect(dimensions);
		for (int i = 0; i < dimensions; i++) {
			localWorkSizes.put(i, device.getMaxWorkItemSizes()[i]);
		}
		for (int i = 0; calculateWorkGroupSize(localWorkSizes) > device.getMaxWorkGroupSize(); i++) {
			localWorkSizes.put(i % dimensions, localWorkSizes.get(i % dimensions) / 2);
		}
		return localWorkSizes;
	}

	private PointerBuffer calculateGlobalWorkSizes(final int dimensions, final int[] sizes) {
		final PointerBuffer localWorkSizes = calculateLocalWorkSizes(dimensions);
		final PointerBuffer globalWorkSizes = PointerBuffer.allocateDirect(dimensions);
		for (int i = 0; i < dimensions; i++) {
			final long remainder = sizes[i] % localWorkSizes.get(i);
			globalWorkSizes.put(i, (remainder == 0L) ? sizes[i] : (sizes[i] - remainder + localWorkSizes.get(i)));
		}
		return globalWorkSizes;
	}
}
