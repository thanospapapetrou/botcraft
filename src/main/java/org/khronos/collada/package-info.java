/**
 * This package defines JAXB mappings for the COLLADA Digital Asset Schema 1.4.1.
 * @author dimitris
 * @see <a href="https://www.khronos.org/files/collada_spec_1_4.pdf">COLLADA – Digital Asset Schema Release 1.4.1</a>
 */
@XmlSchema(namespace = Collada.NAMESPACE, xmlns = {@XmlNs(namespaceURI = Collada.NAMESPACE, prefix = "collada")}, location = Collada.SCHEMA, elementFormDefault = XmlNsForm.QUALIFIED)
package org.khronos.collada;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

