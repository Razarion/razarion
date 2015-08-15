package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Vertices extends ColladaXml {
    private String id;
    private Input input;

    public Vertices(Node node) {
        id = getAttributeAsStringSafe(node, ATTRIBUTE_ID);
        input = new Input(getChild(node, ELEMENT_INPUT));
    }

    public String getId() {
        return id;
    }

    public Input getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "Vertices{" +
                "id=" + id +
                "input=" + input +
                '}';
    }
}
