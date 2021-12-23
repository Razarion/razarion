package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.11.2018.
 */
public class AbstractGeometricPrimitive extends ColladaXml{
    private static Logger LOGGER = Logger.getLogger(AbstractGeometricPrimitive.class.getName());
    private Input vertexInput;
    private Input normInput;
    private Input textCoordInput;
    private Input colorInput;
    private List<Integer> primitiveIndices;

    public AbstractGeometricPrimitive(Node node) {
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
                    textCoordInput = input;
                    break;
                case SEMANTIC_COLOR:
                    colorInput = input;
                    break;
            }
        }
        if (vertexInput == null) {
            throw new ColladaRuntimeException("No vertex found in polygon list inputs. " + node);
        }
        if (normInput == null) {
            throw new ColladaRuntimeException("No norm found in polygon list inputs. " + node);
        }
        primitiveIndices = getElementAsIntegerList(getChild(node, ELEMENT_P));
    }

    protected VertexContainerBuffer createVertexContainer(Map<String, Source> sources, Vertices positionVertex, int verticesPerPolygonCount) {
        if (verticesPerPolygonCount != 3) {
            throw new ColladaRuntimeException("Only polygon with 3 vertices supported (triangle). Given vertices: " + verticesPerPolygonCount);
        }

        int vertexOffset = vertexInput.getOffset();
        int normOffset = normInput.getOffset();
        if (!positionVertex.getId().equals(vertexInput.getSourceId())) {
            throw new ColladaRuntimeException("Vertices id and input source with vertex semantic do not match. Vertices id: " + positionVertex.getId() + " vertex input source: " + vertexInput.getSourceId());
        }
        List<Vertex> vertices = sources.get(positionVertex.getInput().getSourceId()).setupVertices();
        List<Vertex> norms = sources.get(normInput.getSourceId()).setupVertices();
        List<TextureCoordinate> textureCoordinates = null;
        List<Color> vertexColors = null;

        int step = 2;
        int textureOffset = 0;
        int colorOffset = 0;
        if (textCoordInput != null) {
            textureCoordinates = sources.get(textCoordInput.getSourceId()).setupTextureCoordinates();
            step++;
            textureOffset = textCoordInput.getOffset();
        }
        if (colorInput != null) {
            vertexColors = sources.get(colorInput.getSourceId()).setupColors();
            step++;
            colorOffset = colorInput.getOffset();
        }

        List<Float> verticesDest = new ArrayList<>();
        List<Float> normsDest = new ArrayList<>();
        List<Float> textureCoordinatesDest = new ArrayList<>();
        List<Float> vertexColorDest = new ArrayList<>();
        for (int i = 0; i < primitiveIndices.size() / step; i++) {
            try {
                int baseIndex = i * step;
                Vertex vertex = vertices.get(primitiveIndices.get(baseIndex + vertexOffset));
                verticesDest.add((float) vertex.getX());
                verticesDest.add((float) vertex.getY());
                verticesDest.add((float) vertex.getZ());
                Vertex norm = norms.get(primitiveIndices.get(baseIndex + normOffset));
                normsDest.add((float) norm.getX());
                normsDest.add((float) norm.getY());
                normsDest.add((float) norm.getZ());
                if (textureCoordinates != null) {
                    TextureCoordinate textureCoordinate = textureCoordinates.get(primitiveIndices.get(baseIndex + textureOffset));
                    textureCoordinatesDest.add((float) textureCoordinate.getS());
                    textureCoordinatesDest.add((float) textureCoordinate.getT());
                }
                if (vertexColors != null) {
                    Color vertexColor = vertexColors.get(primitiveIndices.get(baseIndex + colorOffset));
                    vertexColorDest.add((float) vertexColor.getR());
                    vertexColorDest.add((float) vertexColor.getB());
                    vertexColorDest.add((float) vertexColor.getG());
                    vertexColorDest.add((float) vertexColor.getA());
                }
            } catch (Throwable throwable) {
                LOGGER.log(Level.WARNING, throwable.getMessage(), throwable);
            }

        }
        VertexContainerBuffer vertexContainerBuffer = new VertexContainerBuffer();
        vertexContainerBuffer.setVertexData(verticesDest);
        vertexContainerBuffer.setNormData(normsDest);

        if (!textureCoordinatesDest.isEmpty()) {
            vertexContainerBuffer.setTextureCoordinate(textureCoordinatesDest);
        }
        if(!vertexColorDest.isEmpty()) {
            vertexContainerBuffer.setVertexColor(vertexColorDest);
        }
        return vertexContainerBuffer;
    }
}
