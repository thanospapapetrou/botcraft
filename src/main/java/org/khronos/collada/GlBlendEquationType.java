//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.01.05 at 12:27:33 AM EET 
//


package org.khronos.collada;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gl_blend_equation_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="gl_blend_equation_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FUNC_ADD"/>
 *     &lt;enumeration value="FUNC_SUBTRACT"/>
 *     &lt;enumeration value="FUNC_REVERSE_SUBTRACT"/>
 *     &lt;enumeration value="MIN"/>
 *     &lt;enumeration value="MAX"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "gl_blend_equation_type")
@XmlEnum
public enum GlBlendEquationType {

    FUNC_ADD,
    FUNC_SUBTRACT,
    FUNC_REVERSE_SUBTRACT,
    MIN,
    MAX;

    public String value() {
        return name();
    }

    public static GlBlendEquationType fromValue(String v) {
        return valueOf(v);
    }

}
