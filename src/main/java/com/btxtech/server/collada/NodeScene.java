package com.btxtech.server.collada;

import com.btxtech.shared.primitives.Matrix4;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class NodeScene extends NameIdColladaXml {
    private static Logger LOGGER = Logger.getLogger(NodeScene.class.getName());
    private Collection<Matrix4> matrices;
    private Collection<InstanceGeometry> instanceGeometries;

    public NodeScene(Node node) {
        super(node);
        matrices = new ArrayList<>();
        for (Node matrixNode : getChildren(node, ELEMENT_MATRIX)) {
            matrices.add(new Matrix(matrixNode).getMatrix4());
        }
        instanceGeometries = new ArrayList<>();
        for (Node instanceGeometryNode : getChildren(node, ELEMENT_INSTANCE_GEOMETRIES)) {
            instanceGeometries.add(new InstanceGeometry(instanceGeometryNode));
        }
    }

    public void convert(ColladaConverterControl colladaConverterControl, Map<String, Geometry> geometries) {
        if (instanceGeometries.isEmpty()) {
            return;
        }

        for (InstanceGeometry instanceGeometry : instanceGeometries) {
            Geometry geometry = geometries.get(instanceGeometry.getUrl());
            if (geometry == null) {
                throw new ColladaRuntimeException("No geometry for url found: " + instanceGeometry.getUrl());
            }
            LOGGER.finest("--:convert:  " + geometry);
            colladaConverterControl.createVertexContainer(geometry.getName());
            geometry.getMesh().fillVertexContainer(matrices, colladaConverterControl);
            colladaConverterControl.vertexContainerCreated();
        }
    }

    @Override
    public String toString() {
        return "NodeScene{" +
                super.toString() +
                "matrices=" + matrices +
                ", instanceGeometries=" + instanceGeometries +
                '}';
    }
}
