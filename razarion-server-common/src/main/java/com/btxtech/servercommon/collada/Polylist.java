package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.08.2015.
 */
public class Polylist extends ColladaXml {
    private Input vertexInput;
    private Input normInput;
    private Input textcoordInput;
    private List<Integer> polygonPrimitiveCounts;
    private List<Integer> primitiveIndices;

    public Polylist(Node node) {
        // count = getAttributeAsInt(node, ATTRIBUTE_COUNT);

        for (Node inputNode : getChildren(node, ELEMENT_INPUT)) {
            Input input = new Input(inputNode);
            switch (input.getSemantic()) {
                case SEMANTIC_VERTEX:
                    vertexInput = input;
                    break;
                case SEMANTIC_NORMAL:
                    normInput = input;
                    break;
                case SEMANTIC_TEXCOORD:
                    textcoordInput = input;
                    break;
            }
        }
        if (vertexInput == null) {
            throw new ColladaRuntimeException("No vertex found in polygon list inputs. " + node);
        }
        if (normInput == null) {
            throw new ColladaRuntimeException("No norm found in polygon list inputs. " + node);
        }

        polygonPrimitiveCounts = getElementAsIntegerList(getChild(node, ELEMENT_VCOUNT));
        primitiveIndices = getElementAsIntegerList(getChild(node, ELEMENT_P));
    }

    public VertexContainer createVertexContainer(Map<String, Source> sources, Vertices positionVertex) {
        for (Integer polygonPrimitiveCount : polygonPrimitiveCounts) {
            if (polygonPrimitiveCount != 3) {
                throw new ColladaRuntimeException("Only polygon with 3 vertices supported (triangle). Given vertices: " + polygonPrimitiveCount);
            }
        }
        int vertexOffset = vertexInput.getOffset();
        int normOffset = normInput.getOffset();
        if (!positionVertex.getId().equals(vertexInput.getSourceId())) {
            throw new ColladaRuntimeException("Vertices id and input source with vertex semantic do not match. Vertices id: " + positionVertex.getId() + " vertex input source: " + vertexInput.getSourceId());
        }
        List<Vertex> vertices = sources.get(positionVertex.getInput().getSourceId()).setupVertices();
        List<Vertex> norms = sources.get(normInput.getSourceId()).setupVertices();
        List<TextureCoordinate> textureCoordinates = null;
        int step = 2;
        int textureOffset = 0;
        if (textcoordInput != null) {
            textureCoordinates = sources.get(textcoordInput.getSourceId()).setupTextureCoordinates();
            step = 3;
            textureOffset = textcoordInput.getOffset();
        }

        List<Vertex> verticesDest = new ArrayList<>();
        List<Vertex> normsDest = new ArrayList<>();
        List<TextureCoordinate> textureCoordinatesDest = new ArrayList<>();
        for (int i = 0; i < primitiveIndices.size() / step; i++) {
            int baseIndex = i * step;
            verticesDest.add(vertices.get(primitiveIndices.get(baseIndex + vertexOffset)));
            normsDest.add(norms.get(primitiveIndices.get(baseIndex + normOffset)));
            if (textureCoordinates != null) {
                textureCoordinatesDest.add(textureCoordinates.get(primitiveIndices.get(baseIndex + textureOffset)));
            }

        }
        VertexContainer vertexContainer = new VertexContainer();
        vertexContainer.setVerticesCount(verticesDest.size());
        vertexContainer.setVertices(verticesDest).setNorms(normsDest);
        if (!textureCoordinatesDest.isEmpty()) {
            vertexContainer.setTextureCoordinates(textureCoordinatesDest);
        }
        return vertexContainer;
    }
}
