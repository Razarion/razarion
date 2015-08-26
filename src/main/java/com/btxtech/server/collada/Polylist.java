package com.btxtech.server.collada;

import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.terrain.VertexList;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 15.08.2015.
 */
public class Polylist extends ColladaXml {
    private int count;
    private Input vertexInput;
    private Input normInput;
    private List<Integer> polygonPrimitiveCounts;
    private List<Integer> primitiveIndices;

    public Polylist(Node node) {
        count = getAttributeAsInt(node, ATTRIBUTE_COUNT);

        for (Node inputNode : getChildren(node, ELEMENT_INPUT)) {
            Input input = new Input(inputNode);
            if (input.getSemantic().equals(SEMANTIC_VERTEX)) {
                vertexInput = input;
            } else if (input.getSemantic().equals(SEMANTIC_NORMAL)) {
                normInput = input;
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

    public VertexList toTriangleVertexList(Map<String, Source> sources, Vertices positionVertex) {
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
        List<Vertex> vertices = sources.get(positionVertex.getInput().getSourceId()).getVertices();
        List<Vertex> norms = sources.get(normInput.getSourceId()).getVertices();

        VertexList vertexList = new VertexList();

        for (int i = 0; i < primitiveIndices.size() / 6; i++) {
            int baseIndex = i * 6;
            vertexList.add(vertices.get(primitiveIndices.get(baseIndex + vertexOffset)),
                    norms.get(primitiveIndices.get(baseIndex + normOffset)),
                    vertices.get(primitiveIndices.get(baseIndex + 2 + vertexOffset)),
                    norms.get(primitiveIndices.get(baseIndex + 2 + normOffset)),
                    vertices.get(primitiveIndices.get(baseIndex + 4 + vertexOffset)),
                    norms.get(primitiveIndices.get(baseIndex + 4 + normOffset))
            );
        }

        return vertexList;
    }
}
