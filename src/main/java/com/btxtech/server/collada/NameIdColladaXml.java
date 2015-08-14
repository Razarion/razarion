package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class NameIdColladaXml extends ColladaXml {
    private String name;
    private String id;

    public NameIdColladaXml(Node node) {
        id = getAttributeAsStringSafe(node, ATTRIBUTE_ID);
        name = getAttributeAsStringSafe(node, ATTRIBUTE_NAME);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "NameIdColladaXml{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
