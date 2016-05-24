package gr.uoa.di.thanos.botcraft.renderers;

import gr.uoa.di.thanos.botcraft.geometry.Matrix;

import java.util.Collections;

import com.jogamp.opengl.GL3;

/**
 * A renderer rendering a cube for debugging purposes.
 * 
 * @author thanos
 */
public class CubeRenderer extends Renderer {
	private static final String VERTEX_SHADER = "/gr/uoa/di/thanos/botcraft/shaders/Cube.vert";
	private static final String FRAGMENT_SHADER = "/gr/uoa/di/thanos/botcraft/shaders/Cube.frag";
	private static final int TRIANGLES = 10;
	private static final String POSITION = "position";
	private static final Float[] POSITION_DATA = new Float[] {-5.0f, -5.0f, 5.0f, 5.0f, -5.0f, 5.0f, -5.0f, 5.0f, 5.0f, -5.0f, 5.0f, 5.0f, 5.0f, -5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, -5.0f, 5.0f, 5.0f, -5.0f, -5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, -5.0f, -5.0f, 5.0f, 5.0f, -5.0f, 5.0f, -5.0f, -5.0f, -5.0f, -5.0f, -5.0f, 5.0f, 5.0f, -5.0f, 5.0f, 5.0f, -5.0f, -5.0f, -5.0f, -5.0f, -5.0f, 5.0f, -5.0f, -5.0f, -5.0f, -5.0f, -5.0f, -5.0f, 5.0f, -5.0f, 5.0f, -5.0f, -5.0f, 5.0f, -5.0f, -5.0f, -5.0f, 5.0f, -5.0f, 5.0f, 5.0f, -5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, -5.0f, 5.0f, -5.0f, -5.0f, 5.0f, -5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, -5.0f};
	private static final String COLOR = "color";
	private static final Float[] COLOR_DATA = new Float[] {1.0f, 0.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 0.0f, 1.0f, 0.5f, 1.0f, 0.0f, 1.0f, 0.5f, 1.0f, 0.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 1.0f, 0.5f};
	private static final String TRANSFORMATION = "transformation";

	private final VertexArray vertexArray;

	/**
	 * Construct a new cube renderer.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @throws RendererException
	 *             if any errors occur
	 */
	public CubeRenderer(final GL3 gl) throws RendererException {
		super(gl, VERTEX_SHADER, Collections.<String, String> emptyMap(), FRAGMENT_SHADER, Collections.<String, String> emptyMap());
		vertexArray = new VertexArray(gl, TRIANGLES, new VertexBuffer<Float>(POSITION, PositionComponent.values().length, POSITION_DATA), new VertexBuffer<Float>(COLOR, ColorComponent.values().length, COLOR_DATA));
	}

	@Override
	public void dispose(final GL3 gl) {
		vertexArray.dispose(gl);
		super.dispose(gl);
	}

	/**
	 * Render the cube.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @param transformation
	 *            the combined projection, view and model transformation to use
	 */
	public void render(final GL3 gl, final Matrix transformation) {
		render(gl);
		bind(gl, TRANSFORMATION, transformation);
		vertexArray.render(gl);
	}
}
