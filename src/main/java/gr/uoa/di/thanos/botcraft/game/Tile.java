package gr.uoa.di.thanos.botcraft.game;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Class representing a map tile.
 * 
 * @author thanos
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tile", namespace = Map.NAMESPACE)
public class Tile {
	@XmlAttribute(name = "altitude", required = true)
	@XmlSchemaType(name = "float")
	private final float altitude;

	@XmlAttribute(name = "terrain", required = true)
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	private final Terrain terrain;

	/**
	 * Construct a new tile.
	 * 
	 * @param altitude
	 *            the tile altitude
	 * @param terrain
	 *            the tile terrain
	 */
	public Tile(final float altitude, final Terrain terrain) {
		Objects.requireNonNull(terrain, "Terrain must not be null");
		this.altitude = altitude;
		this.terrain = terrain;
	}

	@SuppressWarnings("unused")
	private Tile() {
		altitude = 0.0f;
		terrain = null;
	}

	/**
	 * Get the tile altitude.
	 * 
	 * @return the altitude of this tile.
	 */
	public float getAltitude() {
		return altitude;
	}

	/**
	 * Get the tile terrain.
	 * 
	 * @return the terrain of this tile
	 */
	public Terrain getTerrain() {
		return terrain;
	}
}
