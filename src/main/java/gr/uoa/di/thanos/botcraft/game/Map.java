package gr.uoa.di.thanos.botcraft.game;

import gr.uoa.di.thanos.botcraft.etc.utilities.SimpleErrorHandler;
import gr.uoa.di.thanos.botcraft.etc.utilities.SimpleValidationEventHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Class representing a map.
 * 
 * @author thanos
 */
@XmlRootElement(name = "map", namespace = Map.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "map", namespace = Map.NAMESPACE, propOrder = {"latitudinalSize", "longitudinalSize", "tiles"})
public class Map {
	/**
	 * The namespace URI to use in XML representation of a map.
	 */
	public static final String NAMESPACE = "http://www.di.uoa.gr/~thanos/botcraft/game/map/";
	private static final String SCHEMA = "/gr/uoa/di/thanos/botcraft/schemas/map.xsd";
	private static final Logger LOGGER = Logger.getLogger(Map.class.getName());

	@XmlElement(name = "latitudinalSize", namespace = NAMESPACE, required = true)
	@XmlSchemaType(name = "unsignedShort")
	private final int latitudinalSize;

	@XmlElement(name = "longitudinalSize", namespace = NAMESPACE, required = true)
	@XmlSchemaType(name = "unsignedShort")
	private final int longitudinalSize;

	@XmlElement(name = "tile", namespace = NAMESPACE, required = true)
	@XmlSchemaType(name = "tile", namespace = NAMESPACE)
	private final List<Tile> tiles;

	/**
	 * Load a map from a URL.
	 * 
	 * @param url
	 *            the URL to load the map from
	 * @return the map loaded
	 * @throws IOException
	 *             if any errors occur
	 */
	public static Map load(final URL url) throws IOException {
		Objects.requireNonNull(url, "URL must not be null");
		try {
			final Unmarshaller unmarshaller = JAXBContext.newInstance(Map.class).createUnmarshaller();
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setErrorHandler(new SimpleErrorHandler());
			unmarshaller.setSchema(schemaFactory.newSchema(Map.class.getResource(SCHEMA)));
			unmarshaller.setEventHandler(new SimpleValidationEventHandler());
			final Map map = (Map) unmarshaller.unmarshal(url);
			if (map.tiles.size() != map.latitudinalSize * map.longitudinalSize) {
				throw new IOException("Error loading map from " + url, new IllegalStateException("Map must have exactly " + (map.latitudinalSize * map.longitudinalSize) + " tiles"));
			}
			LOGGER.info("Loaded map from " + url);
			return map;
		} catch (final JAXBException | SAXException e) {
			throw new IOException("Error loading map from " + url, e);
		}
	}

	/**
	 * Construct a new map.
	 * 
	 * @param latitudinalSize
	 *            the map latitudinal size
	 * @param longitudinalSize
	 *            the map longitudinal size
	 * @param terrain
	 *            the map default terrain
	 */
	public Map(final int latitudinalSize, final int longitudinalSize, final Terrain terrain) {
		if (latitudinalSize <= 0) {
			throw new IllegalArgumentException("Latitudinal size must be positive");
		}
		if (longitudinalSize <= 0) {
			throw new IllegalArgumentException("Longitudinal size must be positive");
		}
		Objects.requireNonNull(terrain, "Terrain must not be null");
		this.latitudinalSize = latitudinalSize;
		this.longitudinalSize = longitudinalSize;
		tiles = new ArrayList<Tile>();
		for (int i = 0; i < latitudinalSize * longitudinalSize; i++) {
			tiles.add(new Tile(0.0f, terrain));
		}
	}

	@SuppressWarnings("unused")
	private Map() {
		this.latitudinalSize = 0;
		this.longitudinalSize = 0;
		this.tiles = new ArrayList<Tile>();
	}

	/**
	 * Get the map latitudinal size.
	 * 
	 * @return the latitudinal size of this map
	 */
	public int getLatitudinalSize() {
		return latitudinalSize;
	}

	/**
	 * Get the map longitudinal size.
	 * 
	 * @return the longitudinal size of this map
	 */
	public int getLongitudinalSize() {
		return longitudinalSize;
	}

	/**
	 * Get a map tile.
	 * 
	 * @param latitude
	 *            the latitude of the tile to get
	 * @param longitude
	 *            the longitude of the tile to get
	 * @return the tile at the given latitude and longitude
	 */
	public Tile getTile(final int latitude, final int longitude) {
		if ((latitude < 0) || (latitude >= latitudinalSize)) {
			throw new IllegalArgumentException("Latitude must be between 0 and " + latitudinalSize);
		}
		if ((longitude < 0) || (longitude >= longitudinalSize)) {
			throw new IllegalArgumentException("Longitude must be between 0 and " + longitudinalSize);
		}
		return tiles.get(latitude * longitudinalSize + longitude);
	}

	/**
	 * Set a map tile.
	 * 
	 * @param latitude
	 *            the latitude of the tile to set
	 * @param longitude
	 *            the longitude of the tile to set
	 * @param tile
	 *            the tile to set
	 */
	public void setTile(final int latitude, final int longitude, final Tile tile) {
		if ((latitude < 0) || (latitude >= latitudinalSize)) {
			throw new IllegalArgumentException("Latitude must be between 0 and " + latitudinalSize);
		}
		if ((longitude < 0) || (longitude >= longitudinalSize)) {
			throw new IllegalArgumentException("Longitude must be between 0 and " + longitudinalSize);
		}
		tiles.set(latitude * longitudinalSize + longitude, tile);
	}

	/**
	 * Save this map to a file.
	 * 
	 * @param file
	 *            the file to save to
	 * @throws IOException
	 *             if any errors occur
	 */
	public void save(final File file) throws IOException {
		Objects.requireNonNull(file, "File must not be null");
		try {
			final Marshaller marshaller = JAXBContext.newInstance(getClass()).createMarshaller();
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setErrorHandler(new SimpleErrorHandler());
			marshaller.setSchema(schemaFactory.newSchema(getClass().getResource(SCHEMA)));
			marshaller.setEventHandler(new SimpleValidationEventHandler());
			marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, NAMESPACE + " " + SCHEMA);
			marshaller.marshal(this, file);
			LOGGER.info("Saved map to " + file);
		} catch (final JAXBException | SAXException e) {
			throw new IOException("Error saving map to " + file, e);
		}
	}
}
