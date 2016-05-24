package gr.uoa.di.thanos.botcraft.renderers;

import gr.uoa.di.thanos.botcraft.geometry.Matrix;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

/**
 * Base class containing common implementation for for renderers. Each renderer corresponds to a GLSL program consisting of a GLSL vertex shader and a GLSL fragment shader.
 * 
 * @author thanos
 */
public abstract class Renderer {
	private static class Shader implements AutoCloseable {
		private static final String VERSION = "#version 140\n";
		private static final String DEFINITION = "#define %1$s %2$s\n";
		private static final int BUFFER_SIZE = 1024;

		final int shader;
		private final GL3 gl;
		private final String name;

		private Shader(final GL3 gl, final int type, final String shader, final Map<String, String> definitions) throws RendererException {
			try (final InputStreamReader reader = new InputStreamReader(Renderer.class.getResourceAsStream(shader), StandardCharsets.UTF_8)) {
				final StringBuilder source = new StringBuilder(VERSION);
				for (final Map.Entry<String, String> definition : definitions.entrySet()) {
					source.append(String.format(DEFINITION, definition.getKey(), definition.getValue()));
				}
				final char[] buffer = new char[BUFFER_SIZE];
				int read = 0;
				while ((read = reader.read(buffer)) != -1) {
					source.append(buffer, 0, read);
				}
				this.shader = gl.glCreateShader(type);
				gl.glShaderSource(this.shader, 1, new String[] {source.toString()}, IntBuffer.wrap(new int[] {source.length()}));
				LOGGER.info("Loaded GLSL shader " + shader);
				this.gl = gl;
				this.name = shader;
				compile();
			} catch (final IOException e) {
				throw new RendererException("Error loading GLSL shader " + shader, e);
			}
		}

		@Override
		public void close() {
			gl.glDeleteShader(shader);
		}

		private void compile() throws RendererException {
			gl.glCompileShader(shader);
			final IntBuffer compileStatus = IntBuffer.allocate(1);
			gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compileStatus);
			if (compileStatus.get(0) == GL3.GL_FALSE) {
				final IntBuffer infoLogLength = IntBuffer.allocate(1);
				gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, infoLogLength);
				final ByteBuffer infoLog = ByteBuffer.allocate(infoLogLength.get(0));
				gl.glGetShaderInfoLog(shader, infoLogLength.get(0), null, infoLog);
				close();
				throw new RendererException("Error compiling GLSL shader " + name, new String(infoLog.array(), StandardCharsets.UTF_8));
			}
			LOGGER.info("Compiled GLSL shader " + name);
		}
	}

	private static class VertexShader extends Shader {
		private VertexShader(final GL3 gl, final String shader, final Map<String, String> definitions) throws RendererException {
			super(gl, GL3.GL_VERTEX_SHADER, shader, definitions);
		}
	}

	private static class FragmentShader extends Shader {
		private FragmentShader(final GL3 gl, final String shader, final Map<String, String> definitions) throws RendererException {
			super(gl, GL3.GL_FRAGMENT_SHADER, shader, definitions);
		}
	}

	/**
	 * Object oriented wrapper of an OpenGL vertex array.
	 * 
	 * @author thanos
	 */
	protected class VertexArray {
		private final int vertexArray;
		private final int triangles;
		private final VertexBuffer<?>[] vertexBuffers;

		/**
		 * Construct a new vertex array.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 * @param triangles
		 *            the number of triangles represented by this vertex array
		 * @param vertexBuffers
		 *            the vertex buffers to bind to this vertex array
		 */
		protected VertexArray(final GL3 gl, final int triangles, final VertexBuffer<?>... vertexBuffers) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			if (triangles <= 0) {
				throw new IllegalArgumentException("Triangles mut be positive");
			}
			Objects.requireNonNull(vertexBuffers, "Vertex buffers must not be null");
			if (vertexBuffers.length == 0) {
				throw new IllegalArgumentException("Vertex buffers must not be empty");
			}
			for (int i = 0; i < vertexBuffers.length; i++) {
				Objects.requireNonNull(vertexBuffers[i], "Vertex buffer " + i + " must not be null");
				if (vertexBuffers[i].triangles < triangles) {
					throw new IllegalArgumentException("Vertex buffer " + i + " triangles must be at least " + triangles);
				}
			}
			final IntBuffer vertexArrayBuffer = IntBuffer.allocate(1);
			gl.glGenVertexArrays(1, vertexArrayBuffer);
			gl.glBindVertexArray(vertexArrayBuffer.get(0));
			vertexArray = vertexArrayBuffer.get(0);
			this.triangles = triangles;
			this.vertexBuffers = vertexBuffers;
			for (int i = 0; i < vertexBuffers.length; i++) {
				vertexBuffers[i].initialize(gl);
			}
			gl.glBindVertexArray(0);
		}

		/**
		 * Dispose this vertex array.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 */
		protected void dispose(final GL3 gl) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			for (final VertexBuffer<?> vertexBuffer : vertexBuffers) {
				vertexBuffer.dispose(gl);
			}
			gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[] {vertexArray}));
		}

		/**
		 * Render this vertex array.
		 * 
		 * @param gl
		 *            the OpenGL texture to use
		 */
		protected void render(final GL3 gl) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			gl.glBindVertexArray(vertexArray);
			for (final VertexBuffer<?> vertexBuffer : vertexBuffers) {
				vertexBuffer.enable(gl);
			}
			// TODO draw less triangles?
			gl.glDrawArrays(GL3.GL_TRIANGLES, 0, triangles * TriangleVertex.values().length);
			for (final VertexBuffer<?> vertexBuffer : vertexBuffers) {
				vertexBuffer.disable(gl);
			}
			gl.glBindVertexArray(0);
		}
	}

	/**
	 * Object oriented wrapper of an OpenGL vertex buffer.
	 * 
	 * @author thanos
	 * @param <T>
	 *            the type of the data of this vertex buffer
	 */
	protected class VertexBuffer<T extends Number> {
		/**
		 * the number of triangles contained in this vertex buffer
		 */
		protected final int triangles;
		/**
		 * the number of elements per triangle vertex contained in this vertex buffer
		 */
		protected final int elements;
		/**
		 * the OpenGL identifier of this vertex buffer
		 */
		protected int vertexBuffer;
		private final String name;
		private final long size;
		private final Buffer buffer;
		private final int usage;
		private final int type;

		/**
		 * Construct a new vertex buffer.
		 * 
		 * @param name
		 *            the name of the GLSL attribute to bind this vertex buffer to
		 * @param elements
		 *            the number of elements per triangle vertex contained in this vertex buffer
		 * @param data
		 *            the data of this vertex buffer
		 */
		protected VertexBuffer(final String name, final int elements, final T[] data) {
			this(name, triangles(elements, data), elements, calculateSize(triangles(elements, data), elements, data), wrap(data), GL.GL_STATIC_DRAW, javaType2GlType((data == null) ? null : data.getClass().getComponentType().asSubclass(Number.class)));
		}

		/**
		 * Construct a vertex buffer.
		 * 
		 * @param name
		 *            the name of the GLSL attribute to bind this vertex buffer to
		 * @param triangles
		 *            the number of triangles contained in this vertex buffer
		 * @param elements
		 *            the number of elements per triangle vertex contained in this vertex buffer
		 * @param usage
		 *            the OpenGL usage of this vertex buffer
		 * @param type
		 *            the class corresponding to the type of the data of this vertex buffer
		 */
		protected VertexBuffer(final String name, final int triangles, final int elements, final int usage, final Class<T> type) {
			this(name, triangles, elements, calculateSize(triangles, elements, type), null, usage, javaType2GlType(type));
		}

		private VertexBuffer(final String name, final int triangles, final int elements, final long size, final Buffer buffer, final int usage, final int type) {
			Objects.requireNonNull(name, "Name must not be null");
			if (name.isEmpty()) {
				throw new IllegalArgumentException("Name must not be empty");
			}
			if (triangles <= 0) {
				throw new IllegalArgumentException("Triangles must be positive");
			}
			if (elements <= 0) {
				throw new IllegalArgumentException("Elements must be positive");
			}
			if (size <= 0L) {
				throw new IllegalArgumentException("Size must be positive");
			}
			if ((buffer != null) && (buffer.capacity() != calculateTotalElements(triangles, elements))) {
				throw new IllegalArgumentException("Buffer must have capacity " + calculateTotalElements(triangles, elements));
			}
			vertexBuffer = 0;
			this.name = name;
			this.triangles = triangles;
			this.elements = elements;
			this.size = size;
			this.buffer = buffer;
			this.usage = usage;
			this.type = type;
		}

		/**
		 * Dispose this vertex buffer.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 */
		protected void dispose(final GL3 gl) {
			if (vertexBuffer == 0) {
				throw new IllegalStateException("Vertex buffer has not been initialized with an OpenGL context");
			}
			gl.glDeleteBuffers(1, IntBuffer.wrap(new int[] {vertexBuffer}));
		}

		/**
		 * Initialize this vertex buffer.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 */
		protected void initialize(final GL3 gl) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			final IntBuffer vertexBufferBuffer = IntBuffer.allocate(1);
			gl.glGenBuffers(1, vertexBufferBuffer);
			vertexBuffer = vertexBufferBuffer.get(0);
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBuffer);
			gl.glBufferData(GL3.GL_ARRAY_BUFFER, size, buffer, usage);
			gl.glVertexAttribPointer(gl.glGetAttribLocation(program, name), elements, type, false, 0, 0L);
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		}

		private void disable(final GL3 gl) {
			if (vertexBuffer == 0) {
				throw new IllegalStateException("Vertex buffer has not been initialized with an OpenGL context");
			}
			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(program, name));
		}

		private void enable(final GL3 gl) {
			if (vertexBuffer == 0) {
				throw new IllegalStateException("Vertex buffer has not been initialized with an OpenGL context");
			}
			gl.glEnableVertexAttribArray(gl.glGetAttribLocation(program, name));
		}
	}

	/**
	 * Object oriented wrapper of an OpenGL texture.
	 * 
	 * @author thanos
	 */
	protected class Texture {
		private final int texture;

		/**
		 * Construct a new texture.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 * @param texture
		 *            the image resource to use as texture
		 * @throws RendererException
		 *             if any errors occur
		 */
		protected Texture(final GL3 gl, final String texture) throws RendererException {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			Objects.requireNonNull(texture, "Texture must not be null");
			if (texture.isEmpty()) {
				throw new IllegalArgumentException("Texture must not be empty");
			}
			try (final InputStream inputStream = getClass().getResourceAsStream(texture)) {
				final BufferedImage image = ImageIO.read(inputStream);
				final ByteBuffer pixels = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * ColorComponent.values().length);
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						final Color color = new Color(image.getRGB(x, y), true);
						pixels.put((y * image.getWidth() + x) * ColorComponent.values().length + ColorComponent.RED.ordinal(), (byte) (color.getRed() & 0xFF));
						pixels.put((y * image.getWidth() + x) * ColorComponent.values().length + ColorComponent.GREEN.ordinal(), (byte) (color.getGreen() & 0xFF));
						pixels.put((y * image.getWidth() + x) * ColorComponent.values().length + ColorComponent.BLUE.ordinal(), (byte) (color.getBlue() & 0xFF));
						pixels.put((y * image.getWidth() + x) * ColorComponent.values().length + ColorComponent.ALPHA.ordinal(), (byte) (color.getAlpha() & 0xFF));
					}
				}
				final IntBuffer textureBuffer = IntBuffer.allocate(1);
				gl.glGenTextures(1, textureBuffer);
				this.texture = textureBuffer.get(0);
				gl.glBindTexture(GL.GL_TEXTURE_2D, this.texture);
				gl.glTexStorage2D(GL.GL_TEXTURE_2D, Math.max(Integer.SIZE - Integer.numberOfLeadingZeros(image.getWidth()), Integer.SIZE - Integer.numberOfLeadingZeros(image.getHeight())), GL.GL_RGBA8, image.getWidth(), image.getHeight()); // max(log2(width), log2(height))
				gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, image.getWidth(), image.getHeight(), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixels);
				gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
				// TODO set default parameters?
				// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
				gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
				LOGGER.info("Loaded OpenGL texture " + texture);
			} catch (final IOException e) {
				throw new RendererException("Error loading OpenGL texture " + texture, e);
			}
		}

		/**
		 * Bind this texture to a GLSL uniform.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 * @param name
		 *            the name of the GLSL uniform to bind this texture to
		 */
		protected void bind(final GL3 gl, final String name) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			Objects.requireNonNull(name, "Name must not be null");
			if (name.isEmpty()) {
				throw new IllegalArgumentException("Name must not be empty");
			}
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
			gl.glUniform1i(gl.glGetUniformLocation(program, name), 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		}

		/**
		 * Dispose this texture.
		 * 
		 * @param gl
		 *            the OpenGL context to use
		 */
		protected void dispose(final GL3 gl) {
			Objects.requireNonNull(gl, "OpenGL must not be null");
			gl.glDeleteTextures(1, IntBuffer.wrap(new int[] {texture}));
		}
	}

	private static final Logger LOGGER = Logger.getLogger(Renderer.class.getName());

	private final int program;

	/**
	 * Convert an array of {@link Float}s to an array of <code>float</code>s.
	 * 
	 * @param data
	 *            the array to convert
	 * @return an array of <code>float</code>s containing the same values
	 */
	public static float[] objectArray2PrimitiveArray(final Float[] data) {
		Objects.requireNonNull(data, "Data must not be null");
		final float[] result = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			Objects.requireNonNull(data[i], "Data[" + i + "] must not be null");
			result[i] = data[i];
		}
		return result;
	}

	/**
	 * Convert an array of {@link Integer}s to an array of <code>ints</code>s.
	 * 
	 * @param data
	 *            the array to convert
	 * @return an array of <code>int</code>s containing the same values
	 */
	public static int[] objectArray2PrimitiveArray(final Integer[] data) {
		Objects.requireNonNull(data, "Data must not be null");
		final int[] result = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			Objects.requireNonNull(data[i], "Data[" + i + "] must not be null");
			result[i] = data[i];
		}
		return result;
	}

	/**
	 * Calculate the total elements of an OpenGL vertex buffer.
	 * 
	 * @param triangles
	 *            the number of triangles contained in the vertex buffer
	 * @param elements
	 *            the number of elements per triangle vertex contained in the vertex buffer
	 * @return the total number of elements contained in the vertex buffer
	 */
	protected static int calculateTotalElements(final int triangles, final int elements) {
		if (triangles <= 0) {
			throw new IllegalArgumentException("Triangles must be positive");
		}
		if (elements <= 0) {
			throw new IllegalArgumentException("Elements must be positive");
		}
		return triangles * TriangleVertex.values().length * elements;
	}

	/**
	 * Calculate the size of an OpenGL vertex buffer.
	 * 
	 * @param <T>
	 *            the type of elements of the vertex buffer
	 * @param triangles
	 *            the number of triangles contained in the vertex buffer
	 * @param elements
	 *            the number of elements per triangle vertex contained in the vertex buffer
	 * @param type
	 *            the class corresponding to the type of the elements of the vertex buffer (must be either {@link Float}, {@link Integer} or a subclass of these types)
	 * @return the size of the vertex buffer in bytes
	 */
	protected static <T extends Number> long calculateSize(final int triangles, final int elements, final Class<T> type) {
		if (triangles <= 0) {
			throw new IllegalArgumentException("Triangles must be positive");
		}
		if (elements <= 0) {
			throw new IllegalArgumentException("Elements must be positive");
		}
		Objects.requireNonNull(type, "Type must not be null");
		if (Float.class.isAssignableFrom(type)) {
			return calculateTotalElements(triangles, elements) * Float.SIZE / Byte.SIZE;
		} else if (Integer.class.isAssignableFrom(type)) {
			return calculateTotalElements(triangles, elements) * Integer.SIZE / Byte.SIZE;
		} else {
			throw new IllegalArgumentException("Type must be either " + Float.class.getName() + ", " + Integer.class.getName() + " or a subclass of these types");
		}
	}

	/**
	 * Calculate the size of an OpenGL vertex buffer.
	 * 
	 * @param <T>
	 *            the type of the elements of the vertex buffer
	 * @param triangles
	 *            the number of triangles contained in the vertex buffer
	 * @param elements
	 *            the number of elements per triangle vertex contained in the vertex buffer
	 * @param data
	 *            the data of the vertex buffer (must be either an array of {@link Float}s, of {@link Integer}s or an array of a subclass of these types)
	 * @return the size of the vertex buffer in bytes
	 */
	protected static <T extends Number> long calculateSize(final int triangles, final int elements, final T[] data) {
		if (triangles <= 0) {
			throw new IllegalArgumentException("Triangles must be positive");
		}
		if (elements <= 0) {
			throw new IllegalArgumentException("Elements must be positive");
		}
		Objects.requireNonNull(data, "Data must not be null");
		if (Float.class.isAssignableFrom(data.getClass().getComponentType())) {
			return data.length * Float.SIZE / Byte.SIZE;
		} else if (Integer.class.isAssignableFrom(data.getClass().getComponentType())) {
			return data.length * Integer.SIZE / Byte.SIZE;
		} else {
			throw new IllegalArgumentException("Data must be either an array of " + Float.class.getName() + "s, an array of " + Integer.class.getName() + "s or an array of a subclass of these types");
		}
	}

	/**
	 * Wrap an array in a buffer to be used by an OpenGL vertex buffer.
	 * 
	 * @param <T>
	 *            the type of the elements of the vertex buffer
	 * @param data
	 *            the array to wrap (must be either an array of {@link Float}s, an array of {@link Integer}s or an array of a subclass of these types)
	 * @return a buffer wrapping the array
	 */
	protected static <T extends Number> Buffer wrap(final T[] data) {
		Objects.requireNonNull(data, "Data must not be null");
		if (data.length == 0) {
			throw new IllegalArgumentException("Data must not be empty");
		}
		if (Float.class.isAssignableFrom(data.getClass().getComponentType())) {
			return Buffers.newDirectFloatBuffer(objectArray2PrimitiveArray(new Float[0].getClass().cast(data)));
		} else if (Integer.class.isAssignableFrom(data.getClass().getComponentType())) {
			return Buffers.newDirectIntBuffer(objectArray2PrimitiveArray(new Integer[0].getClass().cast(data)));
		} else {
			throw new IllegalArgumentException("Data must be either an array of " + Float.class.getName() + "s, an array of " + Integer.class.getName() + "s or an array of a subclass of these types");
		}
	}

	/**
	 * Convert a Java type to the corresponding OpenGL type.
	 * 
	 * @param <T>
	 *            the Java type to convert
	 * @param type
	 *            the class corresponding to the Java type to convert
	 * @return {@link GL3#GL_FLOAT} if Java type is {@link Float} (or a subclass of it) or {@link GL3#GL_INT} if type is {@link Integer} (or a subclass of it)
	 */
	protected static <T extends Number> int javaType2GlType(final Class<T> type) {
		Objects.requireNonNull(type, "Type must not be null");
		if (Float.class.isAssignableFrom(type)) {
			return GL3.GL_FLOAT;
		} else if (Integer.class.isAssignableFrom(type)) {
			return GL3.GL_INT;
		} else {
			throw new IllegalArgumentException("Type must be either " + Float.class.getName() + ", " + Integer.class.getName() + " or a subclass of these types");
		}
	}

	private static void glslVersion(final GL3 gl) throws RendererException {
		LOGGER.info("Supported GLSL version is " + gl.glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));
		final IntBuffer shaderCompiler = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL3.GL_SHADER_COMPILER, shaderCompiler);
		if (shaderCompiler.get(0) == GL3.GL_FALSE) {
			throw new RendererException("GLSL compiler is not available");
		}
	}

	private static int linkProgram(final GL3 gl, final String vertexShader, final Map<String, String> vertexShaderDefinitions, final String fragmentShader, final Map<String, String> fragmentShaderDefinitions) throws RendererException {
		glslVersion(gl);
		final int program = gl.glCreateProgram();
		try {
			try (final VertexShader vert = new VertexShader(gl, vertexShader, vertexShaderDefinitions)) {
				try (final FragmentShader frag = new FragmentShader(gl, fragmentShader, fragmentShaderDefinitions)) {
					gl.glAttachShader(program, vert.shader);
					gl.glAttachShader(program, frag.shader);
					try {
						gl.glLinkProgram(program);
						final IntBuffer linkStatus = IntBuffer.allocate(1);
						gl.glGetProgramiv(program, GL3.GL_LINK_STATUS, linkStatus);
						if (linkStatus.get(0) == GL3.GL_FALSE) {
							final IntBuffer infoLogLength = IntBuffer.allocate(1);
							gl.glGetProgramiv(program, GL3.GL_INFO_LOG_LENGTH, infoLogLength);
							final ByteBuffer infoLog = ByteBuffer.allocate(infoLogLength.get(0));
							gl.glGetProgramInfoLog(program, infoLogLength.get(0), null, infoLog);
							throw new RendererException("Error linking GLSL program with vertex shader " + vertexShader + " and fragment shader " + fragmentShader, new String(infoLog.array(), StandardCharsets.UTF_8));
						}
						LOGGER.info("Linked GLSL program with vertex shader " + vertexShader + " and fragment shader " + fragmentShader);
						return program;
					} finally {
						gl.glDetachShader(program, vert.shader);
						gl.glDetachShader(program, frag.shader);
					}
				}
			}
		} catch (RendererException e) {
			gl.glDeleteProgram(program);
			throw e;
		}
	}

	private static <T extends Number> int triangles(final int elements, final T[] data) {
		return data.length / TriangleVertex.values().length / elements;
	}

	/**
	 * Construct a new renderer.
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
	 * @throws RendererException
	 *             if any errors occur
	 */
	protected Renderer(final GL3 gl, final String vertexShader, final Map<String, String> vertexShaderDefinitions, final String fragmentShader, final Map<String, String> fragmentShaderDefinitions) throws RendererException {
		Objects.requireNonNull(gl, "OpenGL must not be null");
		Objects.requireNonNull(vertexShader, "Vertex shader must not be null");
		if (vertexShader.isEmpty()) {
			throw new IllegalArgumentException("Vertex shader must not be empty");
		}
		Objects.requireNonNull(vertexShaderDefinitions, "Vertex shader definitions must not be null");
		Objects.requireNonNull(fragmentShader, "Fragment shader must not be null");
		if (fragmentShader.isEmpty()) {
			throw new IllegalArgumentException("Fragment shader must not be null");
		}
		Objects.requireNonNull(fragmentShaderDefinitions, "Fragment shader definitions must not be null");
		program = linkProgram(gl, vertexShader, vertexShaderDefinitions, fragmentShader, fragmentShaderDefinitions);
	}

	/**
	 * Dispose this renderer.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 */
	protected void dispose(final GL3 gl) {
		gl.glDeleteProgram(program);
	}

	/**
	 * Bind a matrix to a GLSL uniform.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @param name
	 *            the name of the GLSL uniform to bind the matrix to
	 * @param matrix
	 *            the matrix to bind
	 */
	protected void bind(final GL3 gl, final String name, final Matrix matrix) {
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(program, name), 1, true, matrix.getBuffer());
	}

	/**
	 * Bind an array of textures to an array of GLSL uniforms.
	 * 
	 * @param gl
	 *            the OpenGL texture to use
	 * @param name
	 *            the name of the array of GLSL uniforms to bind the array of textures to
	 * @param textures
	 *            the array of textures to bind
	 */
	protected void bind(final GL3 gl, final String name, final Texture[] textures) {
		final IntBuffer textureBuffer = IntBuffer.allocate(textures.length);
		for (int i = 0; i < textures.length; i++) {
			gl.glActiveTexture(GL.GL_TEXTURE0 + i);
			gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i].texture);
			textureBuffer.put(i, i);
		}
		gl.glUniform1iv(gl.glGetUniformLocation(program, name), textures.length, textureBuffer);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
	}

	/**
	 * Render this renderer.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 */
	protected void render(final GL3 gl) {
		gl.glUseProgram(program);
	}
}

// for Cube TODO remove
// uniform transformation
// in positions
// in normals
// in colors

// for Map
// uniform textures[]
// uniform transformation
// in positions
// in normals
// in textureIndices[]
// in texture coordinates[]

// for Units
// uniform color
// uniform texture
// uniform transformation
// in positions
// in normals
// in texture coordinates
