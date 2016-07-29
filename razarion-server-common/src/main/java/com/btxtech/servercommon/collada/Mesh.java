package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Mesh extends ColladaXml {
    private Polylist polylist;
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

        polylist = new Polylist(getChild(node, ELEMENT_POLYLIST));
    }

    public VertexContainer createVertexContainer(Collection<Matrix4> matrices) {
        return polylist.createVertexContainer(sources, vertices, matrices);
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "polylist=" + polylist +
                '}';
    }
}
