package gr.uoa.di.thanos.botcraft.gui.components;

import gr.uoa.di.thanos.botcraft.etc.configuration.Configuration;
import gr.uoa.di.thanos.botcraft.etc.configuration.KeyboardControl;
import gr.uoa.di.thanos.botcraft.game.Map;
import gr.uoa.di.thanos.botcraft.geometry.Matrix;
import gr.uoa.di.thanos.botcraft.geometry.Vector;
import gr.uoa.di.thanos.botcraft.renderers.ColorComponent;
import gr.uoa.di.thanos.botcraft.renderers.CubeRenderer;
import gr.uoa.di.thanos.botcraft.renderers.MapRenderer;
import gr.uoa.di.thanos.botcraft.renderers.RendererException;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class MapPanel extends GLCanvas implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 0L;
	private static final float AZIMUTH_MIN = 0.0f;
	private static final float AZIMUTH_MAX = 2.0f * ((float) Math.PI);
	private static final float ELEVATION_MIN = 0.0f;
	private static final float ELEVATION_MAX = ((float) Math.PI) / 2.0f;
	private static final float DISTANCE_MIN = 1.0f; // TODO use configuration
	private static final float DISTANCE_MAX = 100.0f; // TODO use configuration
	private static final float SCROLLING_SPEED = 5.0f; // TODO use configuration
	private static final float ROTATION_SPEED = ((float) Math.PI) / 4.0f; // TODO use configuration
	private static final float ZOOMING_SPEED = 5.0f; // TODO use configuration
	private static final int SCROLL_PADDING = 10; // TODO use configuration
	private static final float FIELD_OF_VIEW_X = 114.0f * (float) Math.PI / 180.0f; // 114 degrees in rads
	private static final float FIELD_OF_VIEW_Y = 135.0f * (float) Math.PI / 180.0f; // 135 degrees in rads
	private static final Logger LOGGER = Logger.getLogger(MapPanel.class.getName());

	private final Configuration configuration;
	private final Map map;
	private final FPSAnimator animator;
	private CubeRenderer cubeRenderer;
	private MapRenderer mapRenderer;
	private float latitude;
	private float longitude;
	private float altitude;
	private float azimuth;
	private float elevation;
	private float distance;
	private float anteriorSpeed;
	private float lateralSpeed;
	private float azimuthialSpeed;
	private float elevationalSpeed;
	private float distantialSpeed;
	private long time;

	private static GLCapabilities configuration2Capabilities(final Configuration configuration) {
		Objects.requireNonNull(configuration, "Configuration must not be null");
		final GLProfile profile = GLProfile.get(GLProfile.GL3); // TODO get for configuration.getScreen()
		// AWTGraphicsScreen.createDefault(configuration.getScreen().getIDstring()); // TODO
		// AWTGraphicsScreen.findScreenIndex(configuration.getScreen())
		// AWTGraphicsScreen.createScreenDevice(arg0, arg1);
		final GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setHardwareAccelerated(configuration.isHardwareAccelerated());
		capabilities.setDoubleBuffered(configuration.isDoubleBuffered());
		capabilities.setRedBits(configuration.getFrameBufferBits(ColorComponent.RED));
		capabilities.setGreenBits(configuration.getFrameBufferBits(ColorComponent.GREEN));
		capabilities.setBlueBits(configuration.getFrameBufferBits(ColorComponent.BLUE));
		capabilities.setAlphaBits(configuration.getFrameBufferBits(ColorComponent.ALPHA));
		capabilities.setDepthBits(configuration.getDepthBufferBits());
		capabilities.setSampleBuffers(configuration.getSampleBuffers() > 0);
		capabilities.setNumSamples(configuration.getSampleBuffers());
		LOGGER.info("Attempting to initialize OpenGL with profile " + profile.getName() + ", " + capabilities2String(capabilities));
		return capabilities;
	}

	private static final String capabilities2String(final GLCapabilitiesImmutable capabilities) {
		return "hardware acceleration " + boolean2String(capabilities.getHardwareAccelerated()) + ", double buffering " + boolean2String(capabilities.getDoubleBuffered()) + ", " + capabilities.getRedBits() + " frame buffer red bits, " + capabilities.getGreenBits() + " frame buffer green bits, " + capabilities.getBlueBits() + " frame buffer blue bits, " + capabilities.getAlphaBits() + " frame buffer alpha bits, " + capabilities.getDepthBits() + " depth buffer bits and " + capabilities.getNumSamples() + " sample buffers";
	}

	private static final String boolean2String(final boolean value) {
		return value ? "enabled" : "disabled";
	}

	public MapPanel(final Configuration configuration, final Map map) {
		super(configuration2Capabilities(configuration));
		Objects.requireNonNull(map, "Map must not be null");
		this.configuration = configuration;
		this.map = map;
		animator = new FPSAnimator(this, configuration.getFramesPerSecond());
		latitude = 0.0f;
		longitude = 0.0f;
		altitude = 0.0f;
		azimuth = 0.0f;
		elevation = 0.0f;
		distance = DISTANCE_MIN;
		anteriorSpeed = 0.0f;
		lateralSpeed = 0.0f;
		azimuthialSpeed = 0.0f;
		elevationalSpeed = 0.0f;
		distantialSpeed = 0.0f;
		time = System.nanoTime();
		addGLEventListener(this);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		final long now = System.nanoTime();
		final float dt = (now - time) / (float) TimeUnit.NANOSECONDS.convert(1L, TimeUnit.SECONDS); // dt in fractional seconds
		latitude += (((float) Math.cos(azimuth)) * anteriorSpeed - ((float) Math.sin(azimuth)) * lateralSpeed) * dt;
		latitude = (latitude < 0.0f) ? 0.0f : ((latitude > map.getLatitudinalSize()) ? map.getLatitudinalSize() : latitude);
		longitude += (((float) Math.sin(azimuth)) * anteriorSpeed + ((float) Math.cos(azimuth)) * lateralSpeed) * dt;
		longitude = (longitude < 0.0f) ? 0.0f : ((longitude > map.getLongitudinalSize()) ? map.getLongitudinalSize() : longitude);
		// altitude = mapRenderer.getAltitude(latitude, longitude); TODO
		azimuth += azimuthialSpeed * dt;
		azimuth = (azimuth < AZIMUTH_MIN) ? (AZIMUTH_MAX - azimuth) : ((azimuth > AZIMUTH_MAX) ? (azimuth - AZIMUTH_MAX) : azimuth);
		elevation += elevationalSpeed * dt;
		elevation = (elevation < ELEVATION_MIN) ? ELEVATION_MIN : ((elevation > ELEVATION_MAX) ? ELEVATION_MAX : elevation);
		distance += distantialSpeed * dt;
		distance = (distance < DISTANCE_MIN) ? DISTANCE_MIN : ((distance > DISTANCE_MAX) ? DISTANCE_MAX : distance);
		final GL3 gl = drawable.getGL().getGL3();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // opaque black
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		cubeRenderer.render(gl, projection().multiply(view()));
		mapRenderer.render(gl, projection().multiply(view()));
		swapBuffers();
		time = now;
		gl.glFlush();
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		animator.stop();
		final GL3 gl = drawable.getGL().getGL3();
		cubeRenderer.dispose(gl);
		mapRenderer.dispose(gl);
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		final GL3 gl = new DebugGL3(drawable.getGL().getGL3());
		drawable.setGL(gl);
		gl.glEnable(GL3.GL_DEPTH_TEST);
		gl.glEnable(GL3.GL_CULL_FACE);
		gl.glEnable(GL3.GL_MULTISAMPLE);
		try {
			cubeRenderer = new CubeRenderer(gl);
			mapRenderer = new MapRenderer(gl, configuration, map);
		} catch (final RendererException e) {
			LOGGER.log(Level.WARNING, "Error initializing cube renderer", e); // TODO do something worse
		}
		// altitude = mapRenderer.getAltitude(latitude, longitude);
		LOGGER.info("Initialized OpenGL with profile " + getGLProfile().getName() + ", " + capabilities2String(getChosenGLCapabilities()));
		animator.start();
	}

	@Override
	public void keyTyped(final KeyEvent event) {
	}

	@Override
	public void keyPressed(final KeyEvent event) {
		if (eventsMatch(event, KeyboardControl.MOVE_FORWARD)) {
			anteriorSpeed = SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_BACKWARD)) {
			anteriorSpeed = -SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_LEFT)) {
			lateralSpeed = -SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_RIGHT)) {
			lateralSpeed = SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_FORWARD_LEFT)) {
			anteriorSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_FORWARD_RIGHT)) {
			anteriorSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_BACKWARD_LEFT)) {
			anteriorSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.MOVE_BACKWARD_RIGHT)) {
			anteriorSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.TURN_LEFT)) {
			azimuthialSpeed = -ROTATION_SPEED;
		} else if (eventsMatch(event, KeyboardControl.TURN_RIGHT)) {
			azimuthialSpeed = ROTATION_SPEED;
		} else if (eventsMatch(event, KeyboardControl.PITCH_UP)) {
			elevationalSpeed = ROTATION_SPEED;
		} else if (eventsMatch(event, KeyboardControl.PITCH_DOWN)) {
			elevationalSpeed = -ROTATION_SPEED;
		} else if (eventsMatch(event, KeyboardControl.ZOOM_OUT)) {
			distantialSpeed = ZOOMING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.ZOOM_IN)) {
			distantialSpeed = -ZOOMING_SPEED;
		} else if (eventsMatch(event, KeyboardControl.PAUSE_RESUME)) {
			if (animator.isPaused()) {
				animator.resume();
			} else {
				animator.pause();
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent event) {
		if (eventsMatch(event, KeyboardControl.MOVE_FORWARD, KeyboardControl.MOVE_BACKWARD, KeyboardControl.MOVE_LEFT, KeyboardControl.MOVE_RIGHT, KeyboardControl.MOVE_FORWARD_LEFT, KeyboardControl.MOVE_FORWARD_RIGHT, KeyboardControl.MOVE_BACKWARD_LEFT, KeyboardControl.MOVE_BACKWARD_RIGHT)) {
			anteriorSpeed = 0.0f;
			lateralSpeed = 0.0f;
		} else if (eventsMatch(event, KeyboardControl.TURN_LEFT, KeyboardControl.TURN_RIGHT)) {
			azimuthialSpeed = 0.0f;
		} else if (eventsMatch(event, KeyboardControl.PITCH_UP, KeyboardControl.PITCH_DOWN)) {
			elevationalSpeed = 0.0f;
		} else if (eventsMatch(event, KeyboardControl.ZOOM_OUT, KeyboardControl.ZOOM_IN)) {
			distantialSpeed = 0.0f;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent event) {
	}

	@Override
	public void mouseDragged(final MouseEvent event) {
	}

	@Override
	public void mouseEntered(final MouseEvent event) {
	}

	@Override
	public void mouseExited(final MouseEvent event) {
	}

	@Override
	public void mouseMoved(final MouseEvent event) {
		if ((event.getX() < SCROLL_PADDING) && (event.getY() < SCROLL_PADDING)) {
			anteriorSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if ((event.getX() >= getWidth() - SCROLL_PADDING) && (event.getY() < SCROLL_PADDING)) {
			anteriorSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if ((event.getX() < SCROLL_PADDING) && (event.getY() >= getHeight() - SCROLL_PADDING)) {
			anteriorSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if ((event.getX() >= getWidth() - SCROLL_PADDING) && (event.getY() >= getHeight() - SCROLL_PADDING)) {
			anteriorSpeed = -((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
			lateralSpeed = ((float) Math.pow(2.0f, 1 / 2.0f)) / 2.0f * SCROLLING_SPEED;
		} else if (event.getX() < SCROLL_PADDING) {
			lateralSpeed = -SCROLLING_SPEED;
		} else if (event.getX() >= getWidth() - SCROLL_PADDING) {
			lateralSpeed = SCROLLING_SPEED;
		} else if (event.getY() < SCROLL_PADDING) {
			anteriorSpeed = SCROLLING_SPEED;
		} else if (event.getY() >= getHeight() - SCROLL_PADDING) {
			anteriorSpeed = -SCROLLING_SPEED;
		} else {
			anteriorSpeed = 0.0f;
			lateralSpeed = 0.0f;

			System.out.println("x: " + event.getX() + ", width: " + getWidth());
			System.out.println("y: " + event.getY() + ", height: " + getHeight());
		}
	}

	@Override
	public void mousePressed(final MouseEvent event) {
	}

	@Override
	public void mouseReleased(final MouseEvent event) {
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent event) {
		distance -= event.getPreciseWheelRotation() / 10.0f; // TODO use configuration
		distance = (distance < DISTANCE_MIN) ? DISTANCE_MIN : ((distance > DISTANCE_MAX) ? DISTANCE_MAX : distance);
	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
		final GL3 gl = drawable.getGL().getGL3();
		gl.glViewport(0, 0, width, height);
	}

	private Matrix projection() {
		return Matrix.perspectiveProjection(FIELD_OF_VIEW_X, FIELD_OF_VIEW_Y, DISTANCE_MIN, DISTANCE_MAX + (float) Math.sqrt(Math.pow(map.getLatitudinalSize(), 2.0f) + Math.pow(map.getLongitudinalSize(), 2.0f)));

	}

	private Matrix view() {
		return Matrix.translation(new Vector(0.0f, 0.0f, -distance)).multiply(Matrix.rotation(elevation, Vector.I).multiply(Matrix.rotation(azimuth, Vector.J).multiply(Matrix.translation(new Vector(-longitude, -altitude, latitude)))));
	}

	private boolean eventsMatch(final KeyEvent event, final KeyboardControl control) {
		final KeyEvent controlEvent = configuration.getKeyEvent(control);
		return (controlEvent == null) ? false : ((event.getKeyCode() == controlEvent.getKeyCode()) && (event.getKeyLocation() == controlEvent.getKeyLocation()) && (event.getModifiersEx() == controlEvent.getModifiersEx()));
	}

	private boolean eventsMatch(final KeyEvent event, final KeyboardControl... controls) {
		for (final KeyboardControl control : controls) {
			if (eventsMatch(event, control)) {
				return true;
			}
		}
		return false;
	}
}
