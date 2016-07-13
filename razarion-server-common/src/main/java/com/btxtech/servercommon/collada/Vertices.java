package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Vertices extends NameIdColladaXml {
    private Input input;

    public Vertices(Node node) {
        super(node);
        input = new Input(getChild(node, ELEMENT_INPUT));
    }

    public Input getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "Vertices{" +
                "input=" + input +
                '}';
    }
}
