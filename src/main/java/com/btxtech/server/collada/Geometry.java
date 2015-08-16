package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Geometry extends NameIdColladaXml {
    private Mesh mesh;

    public Geometry(Node node) {
        super(node);
        mesh = new Mesh(getChild(node, ELEMENT_MESH));
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public String toString() {
        return "Geometry{" +
                "mesh=" + mesh +
                "} " + super.toString();
    }
}
