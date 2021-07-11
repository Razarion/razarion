package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class NodeScene extends NameIdColladaXml {
    private static Logger LOGGER = Logger.getLogger(NodeScene.class.getName());
    private ShapeTransform transform;
    private Collection<InstanceGeometry> instanceGeometries;

    public NodeScene(Node node) {
        super(node);
        setupShapeTransform(node);
        instanceGeometries = new ArrayList<>();
        for (Node instanceGeometryNode : getChildren(node, ELEMENT_INSTANCE_GEOMETRIES)) {
            instanceGeometries.add(new InstanceGeometry(instanceGeometryNode));
        }
    }

    public Element3DBuilder create(Map<String, Geometry> geometries, Map<String, Material> materials, Map<String, Effect> effects) {
        if (instanceGeometries.isEmpty()) {
            return null;
        }

        Element3DBuilder element3DBuilder = new Element3DBuilder(getId());
        for (InstanceGeometry instanceGeometry : instanceGeometries) {
            Geometry geometry = geometries.get(instanceGeometry.getUrl());
            if (geometry == null) {
                throw new ColladaRuntimeException("No geometry for url found: " + instanceGeometry.getUrl());
            }
            String materialUri = instanceGeometry.getMaterialTargetUri();
            String materialId = null;
            String materialName = null;
            if (materialUri != null) {
                Material material = materials.get(materialUri);
                if (material != null) {
                    materialId = material.getId();
                    materialName = material.getName();
                }
            }

            LOGGER.finest("--:convert:  " + geometry);
            VertexContainerBuffer vertexContainerBuffer = geometry.getMesh().createVertexContainerBuffer();
            VertexContainer vertexContainer = new VertexContainer();
            vertexContainer.setShapeTransform(transform);
            vertexContainer.setVerticesCount(vertexContainerBuffer.calculateVertexCount());
            if (materialId != null) {
                vertexContainer.setVertexContainerMaterial(new VertexContainerMaterial().materialId(materialId).materialName(materialName));
            }
            element3DBuilder.addVertexContainer(vertexContainer, vertexContainerBuffer);
        }
        return element3DBuilder;
    }

    private void setupShapeTransform(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            switch (child.getNodeName().toLowerCase()) {
                case ELEMENT_MATRIX:
                    matrix(child);
                    break;
                case ELEMENT_ROTATE:
                    rotate(child);
                    break;
                case ELEMENT_SCALE:
                    scale(child);
                    break;
                case ELEMENT_TRANSLATE:
                    translate(child);
                    break;
                case ELEMENT_LOOKAT:
                    System.out.println("Transformation not supported: " + ELEMENT_LOOKAT);
                    break;
                case ELEMENT_SKEW:
                    System.out.println("Transformation not supported: " + ELEMENT_SKEW);
                    break;
            }
        }
    }

    private void matrix(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 16) {
            throw new ColladaRuntimeException("Matrix length mus be 16. Current length: " + doubleList.size());
        }

        double doubleArray[] = new double[16];
        for (int i = 0; i < doubleList.size(); i++) {
            doubleArray[i] = doubleList.get(i);
        }

        if (transform != null) {
            throw new IllegalStateException();
        }

        transform = new ShapeTransform().setStaticMatrix(new Matrix4(doubleArray));
    }

    private void translate(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 3) {
            throw new ColladaRuntimeException("Translation length must be 3. Current length: " + doubleList.size());
        }
        if (transform == null) {
            transform = new ShapeTransform();
        }

        transform.setTranslateX(doubleList.get(0)).setTranslateY(doubleList.get(1)).setTranslateZ(doubleList.get(2));
    }

    private void scale(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 3) {
            throw new ColladaRuntimeException("Scale length must be 3. Current length: " + doubleList.size());
        }
        if (transform == null) {
            transform = new ShapeTransform();
        }

        transform.setScaleX(doubleList.get(0)).setScaleY(doubleList.get(1)).setScaleZ(doubleList.get(2));
    }

    private void rotate(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 4) {
            throw new ColladaRuntimeException("Rotation length must be 4. Current length: " + doubleList.size());
        }

        if (!MathHelper.compareWithPrecision(CollectionUtils.sum(doubleList.subList(0, 3)), 1.0)) {
            throw new IllegalArgumentException();
        }
        if (transform == null) {
            transform = new ShapeTransform();
        }

        double radians = Math.toRadians(doubleList.get(3));
        if (doubleList.get(0) > 0.0) {
            transform.setRotateX(radians);
        } else if (doubleList.get(1) > 0.0) {
            transform.setRotateY(radians);
        } else if (doubleList.get(2) > 0.0) {
            transform.setRotateZ(radians);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "NodeScene{" +
                super.toString() +
                "transform=" + transform +
                ", instanceGeometries=" + instanceGeometries +
                '}';
    }
}
