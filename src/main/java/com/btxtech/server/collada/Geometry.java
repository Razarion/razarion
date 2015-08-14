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
        for (Node childMeshNode : getChildren(node, ELEMENT_MESH)) {
            mesh = new Mesh(childMeshNode);
        }
    }

    @Override
    public String toString() {
        return "Geometry{" +
                "mesh=" + mesh +
                "} " + super.toString();
    }
}
