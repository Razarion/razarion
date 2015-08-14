package com.btxtech.server.collada;

import com.btxtech.client.math3d.Vertex;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Accessor extends ColladaXml {
    private int stride;
    private List<Param> params;

    public Accessor(Node node) {
        stride = getAttributeAsInt(node, ATTRIBUTE_STRIDE);
        params = new ArrayList<>();
        for (Node paramNode : getChildren(node, ELEMENT_PARAM)) {
            params.add(new Param(paramNode));
        }
    }

    public List<Vertex> convertToVertex(List<Double> doubles, int count) {
        if (stride != 3) {
            throw new ColladaRuntimeException("Stride mismatch. Stride must be 3 to convert to a vertex. Current stride is: " + stride);
        }
        if (params.size() != 3) {
            throw new ColladaRuntimeException("Parameter count mismatch. Parameter count must be 3 to convert to a vertex. Current parameter count is: " + params.size());
        }

        if (count % 3 != 0) {
            throw new ColladaRuntimeException("Doubles count mismatch. Must be a multipe of 3. Received: " + count);
        }

        if (!params.get(0).getName().equals("X") || !params.get(0).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter count mismatch. Expected X as float. Received: " + params.get(0));
        }

        if (!params.get(1).getName().equals("Y") || !params.get(1).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter count mismatch. Expected Y as float. Received: " + params.get(1));
        }

        if (!params.get(2).getName().equals("Z") || !params.get(2).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter count mismatch. Expected Z as float. Received: " + params.get(2));
        }

        List<Vertex> vertices = new ArrayList<>();
        for (int i = 0; i < doubles.size(); i = i + 3) {
            vertices.add(new Vertex(doubles.get(i), doubles.get(i + 1), doubles.get(i + 2)));
        }
        return vertices;
    }

    @Override
    public String toString() {
        return "Accessor{" +
                "stride=" + stride +
                ", params=" + params +
                "} " + super.toString();
    }
}
