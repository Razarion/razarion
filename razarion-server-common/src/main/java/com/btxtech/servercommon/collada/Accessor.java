package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
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
            throw new ColladaRuntimeException("Doubles count mismatch. Must be a multiple of 3. Received: " + count);
        }
        if (!params.get(0).getName().equals("X") || !params.get(0).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter mismatch. Expected X as float. Received: " + params.get(0));
        }
        if (!params.get(1).getName().equals("Y") || !params.get(1).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter mismatch. Expected Y as float. Received: " + params.get(1));
        }
        if (!params.get(2).getName().equals("Z") || !params.get(2).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter mismatch. Expected Z as float. Received: " + params.get(2));
        }
        List<Vertex> vertices = new ArrayList<>();
        for (int i = 0; i < doubles.size(); i = i + 3) {
            vertices.add(new Vertex(doubles.get(i), doubles.get(i + 1), doubles.get(i + 2)));
        }
        return vertices;
    }


    public List<TextureCoordinate> convertToTextureCoordinate(List<Double> doubles, int count) {
        if (stride != 2) {
            throw new ColladaRuntimeException("Stride mismatch. Stride must be 2 to convert to a TextureCoordinate. Current stride is: " + stride);
        }
        if (params.size() != 2) {
            throw new ColladaRuntimeException("Parameter count mismatch. Parameter count must be 2 to convert to a TextureCoordinate. Current parameter count is: " + params.size());
        }
        if (count % 2 != 0) {
            throw new ColladaRuntimeException("Doubles count mismatch. Must be a multiple of 2. Received: " + count);
        }
        if (!params.get(0).getName().equals("S") || !params.get(0).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter mismatch. Expected S as float. Received: " + params.get(0));
        }
        if (!params.get(1).getName().equals("T") || !params.get(1).getType().equals("float")) {
            throw new ColladaRuntimeException("Parameter mismatch. Expected T as float. Received: " + params.get(1));
        }
        List<TextureCoordinate> vetextureCoordinatestices = new ArrayList<>();
        for (int i = 0; i < doubles.size(); i = i + 2) {
            vetextureCoordinatestices.add(new TextureCoordinate(doubles.get(i), doubles.get(i + 1)));
        }
        return vetextureCoordinatestices;
    }

    public List<Param> getParams() {
        return params;
    }

    public int getStride() {
        return stride;
    }

    @Override
    public String toString() {
        return "Accessor{" +
                "stride=" + stride +
                ", params=" + params +
                "} " + super.toString();
    }
}
