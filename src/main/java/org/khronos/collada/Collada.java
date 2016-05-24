package org.khronos.collada;

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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}asset"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_animations"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_animation_clips"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_cameras"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_controllers"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_geometries"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_effects"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_force_fields"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_images"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_lights"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_materials"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_nodes"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_materials"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_models"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_physics_scenes"/>
 *           &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}library_visual_scenes"/>
 *         &lt;/choice>
 *         &lt;element name="scene" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="instance_physics_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="instance_visual_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" minOccurs="0"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}VersionType" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"asset", "libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras", "scene", "extra"})
@XmlRootElement(name = "COLLADA")
public class Collada {
	/**
	 * The COLLADA namespace URI.
	 */
	public static final String NAMESPACE = "http://www.collada.org/2005/11/COLLADASchema";

	/**
	 * The COLLADA schema URL.
	 */
	public static final String SCHEMA = "https://www.khronos.org/files/collada_schema_1_4";
	private static final Logger LOGGER = Logger.getLogger(Collada.class.getName());

	@XmlElement(required = true)
	protected Asset asset;
	@XmlElements({@XmlElement(name = "library_animations", type = LibraryAnimations.class), @XmlElement(name = "library_animation_clips", type = LibraryAnimationClips.class), @XmlElement(name = "library_cameras", type = LibraryCameras.class), @XmlElement(name = "library_controllers", type = LibraryControllers.class), @XmlElement(name = "library_geometries", type = LibraryGeometries.class), @XmlElement(name = "library_effects", type = LibraryEffects.class), @XmlElement(name = "library_force_fields", type = LibraryForceFields.class), @XmlElement(name = "library_images", type = LibraryImages.class), @XmlElement(name = "library_lights", type = LibraryLights.class), @XmlElement(name = "library_materials", type = LibraryMaterials.class), @XmlElement(name = "library_nodes", type = LibraryNodes.class), @XmlElement(name = "library_physics_materials", type = LibraryPhysicsMaterials.class), @XmlElement(name = "library_physics_models", type = LibraryPhysicsModels.class), @XmlElement(name = "library_physics_scenes", type = LibraryPhysicsScenes.class), @XmlElement(name = "library_visual_scenes", type = LibraryVisualScenes.class)})
	protected List<Object> libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras;
	protected Collada.Scene scene;
	protected List<Extra> extra;
	@XmlAttribute(name = "version", required = true)
	protected String version;
	@XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
	@XmlSchemaType(name = "anyURI")
	protected String base;

	public static void main(final String[] arguments) {
		try {
			final Collada collada = Collada.load(Collada.class.getResource("/gr/uoa/di/thanos/botcraft/assets/SmallBoulder.dae"));
			collada.save(new File("/Users/thanos/small-boulder.dae"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load a COLLADA asset from a URL.
	 * 
	 * @param url
	 *            the URL to load the COLLADA asset from
	 * @return the asset loaded
	 * @throws IOException
	 *             if any errors occur
	 */
	public static Collada load(final URL url) throws IOException {
		Objects.requireNonNull(url, "URL must not be null");
		try {
			final Unmarshaller unmarshaller = JAXBContext.newInstance(Collada.class).createUnmarshaller();
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setErrorHandler(new SimpleErrorHandler());
			unmarshaller.setSchema(schemaFactory.newSchema(new URL(SCHEMA)));
			unmarshaller.setEventHandler(new SimpleValidationEventHandler());
			final Collada collada = (Collada) unmarshaller.unmarshal(url);
			LOGGER.info("Loaded COLLADA asset from " + url);
			return collada;
		} catch (final JAXBException | SAXException e) {
			throw new IOException("Error loading COLLADA asset from " + url, e);
		}
	}

	/**
	 * Save this COLLADA asset to a file.
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
			marshaller.setSchema(schemaFactory.newSchema(new URL(SCHEMA)));
			marshaller.setEventHandler(new SimpleValidationEventHandler());
			marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, NAMESPACE + " " + SCHEMA);
			marshaller.marshal(this, file);
			LOGGER.info("Saved COLLADA asset to " + file);
		} catch (final JAXBException | SAXException e) {
			throw new IOException("Error saving COLLADA asset to " + file, e);
		}
	}

	/**
	 * The COLLADA element must contain an asset element.
	 * 
	 * @return possible object is {@link Asset }
	 */
	public Asset getAsset() {
		return asset;
	}

	/**
	 * Sets the value of the asset property.
	 * 
	 * @param value
	 *            allowed object is {@link Asset }
	 */
	public void setAsset(Asset value) {
		this.asset = value;
	}

	/**
	 * Gets the value of the libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link LibraryAnimations } {@link LibraryAnimationClips } {@link LibraryCameras } {@link LibraryControllers } {@link LibraryGeometries } {@link LibraryEffects } {@link LibraryForceFields } {@link LibraryImages } {@link LibraryLights } {@link LibraryMaterials } {@link LibraryNodes } {@link LibraryPhysicsMaterials } {@link LibraryPhysicsModels } {@link LibraryPhysicsScenes } {@link LibraryVisualScenes }
	 */
	public List<Object> getLibraryAnimationsOrLibraryAnimationClipsOrLibraryCameras() {
		if (libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras == null) {
			libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras = new ArrayList<Object>();
		}
		return this.libraryAnimationsOrLibraryAnimationClipsOrLibraryCameras;
	}

	/**
	 * Gets the value of the scene property.
	 * 
	 * @return possible object is {@link Collada.Scene }
	 */
	public Collada.Scene getScene() {
		return scene;
	}

	/**
	 * Sets the value of the scene property.
	 * 
	 * @param value
	 *            allowed object is {@link Collada.Scene }
	 */
	public void setScene(Collada.Scene value) {
		this.scene = value;
	}

	/**
	 * The extra element may appear any number of times. Gets the value of the extra property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the extra property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExtra().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Extra }
	 */
	public List<Extra> getExtra() {
		if (extra == null) {
			extra = new ArrayList<Extra>();
		}
		return this.extra;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * The xml:base attribute allows you to define the base URI for this COLLADA document. See http://www.w3.org/TR/xmlbase/ for more information.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getBase() {
		return base;
	}

	/**
	 * Sets the value of the base property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setBase(String value) {
		this.base = value;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * <p>
	 * The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="instance_physics_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" maxOccurs="unbounded" minOccurs="0"/>
	 *         &lt;element name="instance_visual_scene" type="{http://www.collada.org/2005/11/COLLADASchema}InstanceWithExtra" minOccurs="0"/>
	 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {"instancePhysicsScene", "instanceVisualScene", "extra"})
	public static class Scene {

		@XmlElement(name = "instance_physics_scene")
		protected List<InstanceWithExtra> instancePhysicsScene;
		@XmlElement(name = "instance_visual_scene")
		protected InstanceWithExtra instanceVisualScene;
		protected List<Extra> extra;

		/**
		 * Gets the value of the instancePhysicsScene property.
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the instancePhysicsScene property.
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getInstancePhysicsScene().add(newItem);
		 * </pre>
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link InstanceWithExtra }
		 */
		public List<InstanceWithExtra> getInstancePhysicsScene() {
			if (instancePhysicsScene == null) {
				instancePhysicsScene = new ArrayList<InstanceWithExtra>();
			}
			return this.instancePhysicsScene;
		}

		/**
		 * Gets the value of the instanceVisualScene property.
		 * 
		 * @return possible object is {@link InstanceWithExtra }
		 */
		public InstanceWithExtra getInstanceVisualScene() {
			return instanceVisualScene;
		}

		/**
		 * Sets the value of the instanceVisualScene property.
		 * 
		 * @param value
		 *            allowed object is {@link InstanceWithExtra }
		 */
		public void setInstanceVisualScene(InstanceWithExtra value) {
			this.instanceVisualScene = value;
		}

		/**
		 * The extra element may appear any number of times. Gets the value of the extra property.
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the extra property.
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getExtra().add(newItem);
		 * </pre>
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link Extra }
		 */
		public List<Extra> getExtra() {
			if (extra == null) {
				extra = new ArrayList<Extra>();
			}
			return this.extra;
		}

	}

}
