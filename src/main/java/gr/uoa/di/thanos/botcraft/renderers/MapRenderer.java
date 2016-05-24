package gr.uoa.di.thanos.botcraft.renderers;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;
import gr.uoa.di.thanos.botcraft.game.Map;
import gr.uoa.di.thanos.botcraft.game.Terrain;
import gr.uoa.di.thanos.botcraft.geometry.Matrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opengl.GL3;

/**
 * A renderer rendering a map.
 * 
 * @author thanos
 */
public class MapRenderer extends CalculatingRenderer {
	private static enum Coordinate {
		LATITUDE, LONGITUDE
	}

	private static enum Direction {
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
	}

	private static final String VERTEX_SHADER = "/gr/uoa/di/thanos/botcraft/shaders/Map.vert";
	private static final String FRAGMENT_SHADER = "/gr/uoa/di/thanos/botcraft/shaders/Map.frag";
	private static final java.util.Map<String, String> FRAGMENT_SHADER_DEFINITIONS = Collections.singletonMap("TERRAINS", Integer.toString(Terrain.values().length));
	private static final String PROGRAM = "/gr/uoa/di/thanos/botcraft/kernels/MapRenderer.cl";
	private static final java.util.Map<String, String> PROGRAM_DEFINITIONS = new HashMap<String, String>() {
		private static final long serialVersionUID = 0L;

		{
			for (final Coordinate coordinate : Coordinate.values()) {
				put(coordinate.name(), Integer.toString(coordinate.ordinal()));
			}
			for (final Direction direction : Direction.values()) {
				put(direction.name(), Integer.toString(direction.ordinal()));
			}
			put("DIRECTIONS", Integer.toString(Direction.values().length));
			for (final TriangleVertex vertex : TriangleVertex.values()) {
				put(vertex.name(), Integer.toString(vertex.ordinal()));
			}
			put("TRIANGLE_VERTICES", Integer.toString(TriangleVertex.values().length));
		}
	};
	private static final String POSITION = "position";
	private static final String NORMAL = "normal";
	private static final String TEXTURE = "texture";
	private static final String TRANSFORMATION = "transformation";
	private static final String TERRAINS = "terrains";
	private static final String CALCULATE_POSITIONS_AND_NORMALS = "calculatePositionsAndNormals";
	private static final String LOAD_VERTEX_BUFFERS = "loadVertexBuffers";

	private final Map map;
	private final CalculatingVertexBuffer<Float> position;
	private final CalculatingVertexBuffer<Float> normal;
	private final CalculatingVertexBuffer<Float> texture;
	private final VertexArray vertexArray;
	private final Texture[] terrains;
	private final CLBuffer<?> altitudes;
	private final CLBuffer<?> positions;
	private final CLBuffer<?> normals;

	/**
	 * Construct a new cube renderer.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @param configuration
	 *            the configuration to use
	 * @param map
	 *            the map to render
	 * @throws RendererException
	 *             if any errors occur
	 */
	public MapRenderer(final GL3 gl, final Configuration configuration, final Map map) throws RendererException {
		super(gl, VERTEX_SHADER, Collections.<String, String> emptyMap(), FRAGMENT_SHADER, FRAGMENT_SHADER_DEFINITIONS, configuration, PROGRAM, PROGRAM_DEFINITIONS);
		Objects.requireNonNull(map, "Map must not be null");
		this.map = map;
		final int triangles = map.getLatitudinalSize() * map.getLongitudinalSize() * Direction.values().length;
		position = new CalculatingVertexBuffer<Float>(POSITION, triangles, PositionComponent.values().length, Float.class);
		normal = new CalculatingVertexBuffer<Float>(NORMAL, triangles, NormalComponent.values().length, Float.class);
		texture = new CalculatingVertexBuffer<>(TEXTURE, triangles, TextureCoordinatesComponent.values().length, Float.class);
		vertexArray = new VertexArray(gl, triangles, new VertexBuffer<?>[] {position, normal, texture});
		terrains = new Texture[Terrain.values().length];
		for (int i = 0; i < terrains.length; i++) {
			terrains[i] = new Texture(gl, Terrain.values()[i].getTexture());
		}
		final Float[] altitudes = new Float[map.getLatitudinalSize() * map.getLongitudinalSize()];
		for (int latitude = 0; latitude < map.getLatitudinalSize(); latitude++) {
			for (int longitude = 0; longitude < map.getLongitudinalSize(); longitude++) {
				altitudes[latitude * map.getLongitudinalSize() + longitude] = map.getTile(latitude, longitude).getAltitude();
			}
		}
		this.altitudes = wrapInClBuffer(altitudes);
		final int latitudinalSize = 2 * map.getLatitudinalSize() + 1;
		final int longitudinalSize = 2 * map.getLongitudinalSize() + 1;
		positions = allocate(latitudinalSize * longitudinalSize * PositionComponent.values().length, Float.class);
		normals = allocate(latitudinalSize * longitudinalSize * NormalComponent.values().length, Float.class);
		calculatePositionsAndNormals(0, 0, latitudinalSize, longitudinalSize);
		loadVertexBuffers(0, 0, map.getLatitudinalSize(), map.getLongitudinalSize());
	}

	@Override
	public void dispose(final GL3 gl) {
		Objects.requireNonNull(gl, "OpenGL must not be null");
		altitudes.release();
		positions.release();
		normals.release();
		vertexArray.dispose(gl);
		for (final Texture terrain : terrains) {
			terrain.dispose(gl);
		}
		super.dispose(gl);
	}

	/**
	 * Render the map.
	 * 
	 * @param gl
	 *            the OpenGL context to use
	 * @param transformation
	 *            the combined projection, view and model transformation to use
	 */
	public void render(final GL3 gl, final Matrix transformation) {
		Objects.requireNonNull(gl, "OpenGL must not be null");
		Objects.requireNonNull(transformation, "Transformation must not be null");
		render(gl);
		bind(gl, TRANSFORMATION, transformation);
		bind(gl, TERRAINS, terrains);
		vertexArray.render(gl);
	}

	public float getAltitude(final float latitude, final float longitude) {
		return 0.0f; // TODO
	}

	private void calculatePositionsAndNormals(final int latitudinalOffset, final int longitudinalOffset, final int latitudinalSize, final int longitudinalSize) {
		final int[] offsets = new int[Coordinate.values().length];
		offsets[Coordinate.LATITUDE.ordinal()] = latitudinalOffset;
		offsets[Coordinate.LONGITUDE.ordinal()] = longitudinalOffset;
		final int[] sizes = new int[Coordinate.values().length];
		sizes[Coordinate.LATITUDE.ordinal()] = latitudinalSize;
		sizes[Coordinate.LONGITUDE.ordinal()] = longitudinalSize;
		executeKernel(CALCULATE_POSITIONS_AND_NORMALS, Coordinate.values().length, offsets, sizes, map.getLatitudinalSize(), map.getLongitudinalSize(), altitudes, positions, normals);
	}

	private void loadVertexBuffers(final int latitudinalOffset, final int longitudinalOffset, final int latitudinalSize, final int longitudinalSize) {
		final int[] offsets = new int[Coordinate.values().length];
		offsets[Coordinate.LATITUDE.ordinal()] = latitudinalOffset;
		offsets[Coordinate.LONGITUDE.ordinal()] = longitudinalOffset;
		final int[] sizes = new int[Coordinate.values().length];
		sizes[Coordinate.LATITUDE.ordinal()] = latitudinalSize;
		sizes[Coordinate.LONGITUDE.ordinal()] = longitudinalSize;
		executeKernel(LOAD_VERTEX_BUFFERS, Coordinate.values().length, offsets, sizes, map.getLatitudinalSize(), map.getLongitudinalSize(), positions, position);
	}
}
