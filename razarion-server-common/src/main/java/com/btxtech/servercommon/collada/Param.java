package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Param extends ColladaXml {
    private String name;
    private String type;

    public Param(Node node) {
        name = getAttributeAsStringSafe(node, ATTRIBUTE_NAME);
        type = getAttributeAsStringSafe(node, ATTRIBUTE_TYPE);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                "} " + super.toString();
    }
}
