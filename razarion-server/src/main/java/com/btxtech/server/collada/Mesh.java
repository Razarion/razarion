package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Mesh extends ColladaXml {
    private Polylist polylist;
    private Triangles triangles;
    private Map<String, Source> sources;
    private Vertices vertices;

    public Mesh(Node node) {
        sources = new HashMap<>();
        for (Node sourceNode : getChildren(node, ELEMENT_SOURCE)) {
            Source source = new Source(sourceNode);
            sources.put(source.getId(), source);
        }

        vertices = new Vertices(getChild(node, ELEMENT_VERTICES));
        if (!vertices.getInput().getSemantic().equals(SEMANTIC_POSITION)) {
            throw new ColladaRuntimeException("Semantics must be POSITION. " + node);
        }

        Node polylistNode = getChildOrNull(node, ELEMENT_POLYLIST);
        if (polylistNode != null) {
            polylist = new Polylist(polylistNode);
        }
        Node trianglesNode = getChildOrNull(node, ELEMENT_TRIANGLES);
        if (trianglesNode != null) {
            triangles = new Triangles(trianglesNode);
        }
    }

    public VertexContainerBuffer createVertexContainerBuffer() {
        if(polylist != null) {
            return polylist.createVertexContainer(sources, vertices);
        }
        if(triangles != null) {
            return triangles.createVertexContainer(sources, vertices);
        }
        throw new ColladaRuntimeException("polylist == null &&  triangles == null");
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "polylist=" + polylist +
                '}';
    }
}
