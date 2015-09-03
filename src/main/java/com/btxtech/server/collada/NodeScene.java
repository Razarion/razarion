package com.btxtech.server.collada;

import com.btxtech.shared.VertexList;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class NodeScene extends ColladaXml {
    private Collection<Matrix> matrices;
    private Collection<InstanceGeometry> instanceGeometries;

    public NodeScene(Node node) {
        matrices = new ArrayList<>();
        for (Node matrixNode : getChildren(node, ELEMENT_MATRIX)) {
            matrices.add(new Matrix(matrixNode));
        }
        instanceGeometries = new ArrayList<>();
        for (Node instanceGeometryNode : getChildren(node, ELEMENT_INSTANCE_GEOMETRIES)) {
            instanceGeometries.add(new InstanceGeometry(instanceGeometryNode));
        }
    }

    public void processGeometry(VertexList vertexList, Map<String, Geometry> geometries) {
        for (InstanceGeometry instanceGeometry : instanceGeometries) {
            Geometry geometry = geometries.get(instanceGeometry.getUrl());
            if(geometry == null) {
                throw new ColladaRuntimeException("No geometry for url found: " + instanceGeometry.getUrl());
            }
            VertexList meshVertex = geometry.getMesh().getVertexList();
            for (Matrix matrix : matrices) {
                meshVertex.multiply(matrix.getMatrix4());
                vertexList.append(meshVertex);
            }
        }
    }

    @Override
    public String toString() {
        return "NodeScene{" +
                "matrices=" + matrices +
                ", instanceGeometries=" + instanceGeometries +
                '}';
    }
}
