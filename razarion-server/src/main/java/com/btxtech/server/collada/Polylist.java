package com.btxtech.server.collada;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import org.w3c.dom.Node;

import java.util.Collection;
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

    public void toTriangleVertexContainer(Map<String, Source> sources, Vertices positionVertex, Collection<Matrix4> matrices, ColladaConverterControl colladaConverterControl) {
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
        int step = 6;
        int textureOffset = 0;
        if (textcoordInput != null) {
            textureCoordinates = sources.get(textcoordInput.getSourceId()).setupTextureCoordinates();
            step = 9;
            textureOffset = textcoordInput.getOffset();
        }

        for (int i = 0; i < primitiveIndices.size() / step; i++) {
            int baseIndex = i * step;
            if (textureCoordinates != null) {
                colladaConverterControl.addTriangle(matrices, vertices.get(primitiveIndices.get(baseIndex + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + normOffset)),
                        textureCoordinates.get(primitiveIndices.get(baseIndex + textureOffset)),
                        vertices.get(primitiveIndices.get(baseIndex + 3 + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + 3 + normOffset)),
                        textureCoordinates.get(primitiveIndices.get(baseIndex + 3 + textureOffset)),
                        vertices.get(primitiveIndices.get(baseIndex + 6 + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + 6 + normOffset)),
                        textureCoordinates.get(primitiveIndices.get(baseIndex + 6 + textureOffset))
                );
            } else {
                colladaConverterControl.addTriangle(matrices, vertices.get(primitiveIndices.get(baseIndex + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + normOffset)),
                        vertices.get(primitiveIndices.get(baseIndex + 2 + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + 2 + normOffset)),
                        vertices.get(primitiveIndices.get(baseIndex + 4 + vertexOffset)),
                        norms.get(primitiveIndices.get(baseIndex + 4 + normOffset))
                );
            }
        }
    }
}
