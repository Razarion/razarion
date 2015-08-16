package com.btxtech.server.collada;

import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.terrain.VertexList;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Mesh extends ColladaXml {
    // private List<Vertex> positions;
    // private List<Vertex> norms;
    private VertexList vertexList;

    public Mesh(Node node) {
        Map<String, Source> sources = new HashMap<>();
        for (Node sourceNode : getChildren(node, ELEMENT_SOURCE)) {
            Source source = new Source(sourceNode);
            sources.put(source.getId(), source);
        }
        if (sources.size() != 2) {
            throw new ColladaRuntimeException("2 sources expected. Actual: " + sources.size());
        }

        Vertices vertices = new Vertices(getChild(node, ELEMENT_VERTICES));
        if (!vertices.getInput().getSemantic().equals(SEMANTIC_POSITION)) {
            throw new ColladaRuntimeException("Semantics must be POSITION. " + node);
        }
//
//        if (sources.get(0).getId().equals(vertices.getInput().getSourceId())) {
//            positions = sources.get(0).getVertices();
//            norms = sources.get(1).getVertices();
//        } else {
//            positions = sources.get(1).getVertices();
//            norms = sources.get(0).getVertices();
//        }

        Polylist polylist = new Polylist(getChild(node, ELEMENT_POLYLIST));
        vertexList = polylist.toTriangleVertexList(sources, vertices);
    }

    public VertexList getVertexList() {
        return vertexList;
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "vertexList=" + vertexList +
                '}';
    }
}
