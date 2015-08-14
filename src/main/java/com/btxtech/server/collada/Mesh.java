package com.btxtech.server.collada;

import com.btxtech.client.math3d.Vertex;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Mesh extends ColladaXml {
    List<Vertex> positions;
    List<Vertex> norms;

    public Mesh(Node node) {
        List<Source> sources = new ArrayList<>();
        for (Node sourceNode : getChildren(node, ELEMENT_SOURCE)) {
            sources.add(new Source(sourceNode));
        }
        if (sources.size() != 2) {
            throw new ColladaRuntimeException("2 sources expected. Actual: " + sources.size());
        }

        VerticesInput verticesInput = new VerticesInput(getChild(node, ELEMENT_VERTICES));
        if (sources.get(0).getId().equals(verticesInput.getSourceId())) {
            positions = sources.get(0).getVertices();
            norms = sources.get(1).getVertices();
        } else {
            positions = sources.get(1).getVertices();
            norms = sources.get(0).getVertices();
        }
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "positions=" + positions +
                ", norms=" + norms +
                "} ";
    }
}
