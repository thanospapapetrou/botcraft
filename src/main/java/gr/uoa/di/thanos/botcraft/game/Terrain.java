package gr.uoa.di.thanos.botcraft.game;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Enumeration representing the available terrains for map tiles.
 * 
 * @author thanos
 */
@XmlEnum
@XmlType(name = "terrain", namespace = Map.NAMESPACE)
public enum Terrain {
	/**
	 * Dirt.
	 */
	@XmlEnumValue("dirt")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	DIRT("/gr/uoa/di/thanos/botcraft/terrains/dirt.png"),

	/**
	 * Grass.
	 */
	@XmlEnumValue("grass")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	GRASS("/gr/uoa/di/thanos/botcraft/terrains/grass.png"),

	/**
	 * Ice.
	 */
	@XmlEnumValue("ice")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	ICE("/gr/uoa/di/thanos/botcraft/terrains/ice.png"),

	/**
	 * Lava.
	 */
	@XmlEnumValue("lava")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	LAVA("/gr/uoa/di/thanos/botcraft/terrains/lava.png"),

	/**
	 * Mud.
	 */
	@XmlEnumValue("mud")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	MUD("/gr/uoa/di/thanos/botcraft/terrains/mud.png"),

	/**
	 * Rock.
	 */
	@XmlEnumValue("rock")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	ROCK("/gr/uoa/di/thanos/botcraft/terrains/rock.png"),

	/**
	 * Sand.
	 */
	@XmlEnumValue("sand")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	SAND("/gr/uoa/di/thanos/botcraft/terrains/sand.png"),

	/**
	 * Water.
	 */
	@XmlEnumValue("water")
	@XmlSchemaType(name = "terrain", namespace = Map.NAMESPACE)
	WATER("/gr/uoa/di/thanos/botcraft/terrains/water.png");

	private final String texture;

	private Terrain(final String texture) {
		this.texture = texture;
	}

	/**
	 * Get the resource name of the texture image of this terrain.
	 * 
	 * @return the resource name of the texture image of this terrain
	 */
	public String getTexture() {
		return texture;
	}
}
