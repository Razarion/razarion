package com.btxtech.server.collada;

import com.btxtech.client.math3d.Vertex;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Source extends NameIdColladaXml {
    List<Vertex> vertices;

    public Source(Node node) {
        super(node);

        TechniqueCommon techniqueCommon = new TechniqueCommon(getChild(node, ELEMENT_TECHNIQUE_COMMON));
        FloatArray floatArray = new FloatArray(getChild(node, ELEMENT_FLOAT_ARRAY));

        vertices = techniqueCommon.getAccessor().convertToVertex(floatArray.getFloatArray(), floatArray.getCount());
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return "Source{" +
                "vertices=" + vertices +
                "} " + super.toString();
    }
}
