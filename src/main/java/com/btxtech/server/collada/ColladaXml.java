package com.btxtech.server.collada;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaXml {
    public static final String ELEMENT_LIBRARY_GEOMETRIES = "library_geometries";
    public static final String ELEMENT_GEOMETRY = "geometry";
    public static final String ELEMENT_MESH = "mesh";
    public static final String ELEMENT_SOURCE = "source";
    public static final String ELEMENT_FLOAT_ARRAY = "float_array";
    public static final String ELEMENT_TECHNIQUE_COMMON = "technique_common";
    public static final String ELEMENT_TECHNIQUE_ACCESSOR = "accessor";
    public static final String ELEMENT_PARAM = "param";
    public static final String ELEMENT_VERTICES = "vertices";
    public static final String ELEMENT_INPUT = "input";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_DIGITS = "digits";
    public static final String ATTRIBUTE_MAGNITUDE = "magnitude";
    public static final String ATTRIBUTE_COUNT = "count";
    public static final String ATTRIBUTE_STRIDE ="stride";
    public static final String ATTRIBUTE_SOURCE ="source";
    public static final String ATTRIBUTE_SEMANTIC ="semantic";

    protected List<Node> getChildren(Node node, String elementName) {
        List<Node> childNodes = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeName().toUpperCase().equals(elementName.toUpperCase())) {
                childNodes.add(child);
            }
        }
        return childNodes;
    }

    protected Node getChild(Node node, String elementName) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeName().toUpperCase().equals(elementName.toUpperCase())) {
                return child;
            }
        }
        throw new ColladaRuntimeException("No child " + elementName + " found in: " + node);
    }

    protected String getAttributeAsString(Node node, String attributeId) {
        Node attributeNode = node.getAttributes().getNamedItem(attributeId);
        if(attributeNode != null) {
            return attributeNode.getNodeValue();
        } else {
            return null;
        }
    }

    protected String getAttributeAsStringSafe(Node node, String attributeId) {
        Node attributeNode = node.getAttributes().getNamedItem(attributeId);
        if(attributeNode != null) {
            return attributeNode.getNodeValue();
        } else {
            throw new ColladaRuntimeException("No such attribute " + attributeId + " in node: " + node);
        }
    }

    protected int getAttributeAsInt(Node node, String attributeName) {
        String stringValue = getAttributeAsString(node, attributeName);
        if(stringValue == null) {
            throw new ColladaRuntimeException("No attribute for name: " + attributeName + " in " + node);
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            throw new ColladaRuntimeException("Error parsing attribute: " + attributeName + " in " + node, e);
        }
    }

    protected int getAttributeAsInt(Node node, String attributeName, int defaultValue) {
        String stringValue = getAttributeAsString(node, attributeName);
        if(stringValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

}
