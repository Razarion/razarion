package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.ShapeTransformMatrix;
import com.btxtech.shared.datatypes.shape.ShapeTransformTRS;
import com.btxtech.shared.datatypes.shape.VertexContainer;
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

    public Element3D convert(Map<String, Geometry> geometries, Map<String, Material> materials, Map<String, Effect> effects) {
        if (instanceGeometries.isEmpty()) {
            return null;
        }

        List<VertexContainer> vertexContainers = new ArrayList<>();
        for (InstanceGeometry instanceGeometry : instanceGeometries) {
            Geometry geometry = geometries.get(instanceGeometry.getUrl());
            if (geometry == null) {
                throw new ColladaRuntimeException("No geometry for url found: " + instanceGeometry.getUrl());
            }
            Effect effect = null;
            String materialUri = instanceGeometry.getMaterialTargetUri();
            String materialId = null;
            String materialName = null;
            if (materialUri != null) {
                Material material = materials.get(materialUri);
                if (material != null) {
                    materialId = material.getId();
                    materialName = material.getName();
                    effect = effects.get(material.getInstanceEffectUrl());
                }
            }

            LOGGER.finest("--:convert:  " + geometry);
            VertexContainer vertexContainer = geometry.getMesh().createVertexContainer();
            vertexContainer.setShapeTransform(transform);
            vertexContainer.setMaterialId(materialId);
            vertexContainer.setMaterialName(materialName);
            if (effect != null && effect.getTechnique() != null) {
                vertexContainer.setAmbient(effect.getTechnique().getAmbient());
                vertexContainer.setDiffuse(effect.getTechnique().getDiffuse());
                vertexContainer.setSpecular(effect.getTechnique().getSpecular());
                vertexContainer.setEmission(effect.getTechnique().getEmission());
            }
            vertexContainers.add(vertexContainer);
        }
        Element3D element3D = new Element3D();
        element3D.setId(getId()).setVertexContainers(vertexContainers);
        return element3D;
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

        transform = new ShapeTransformMatrix().setMatrix(new Matrix4(doubleArray));
    }

    private void translate(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 3) {
            throw new ColladaRuntimeException("Translation length must be 3. Current length: " + doubleList.size());
        }
        if (transform == null) {
            transform = new ShapeTransformTRS();
        }

        ((ShapeTransformTRS) transform).setXTranslate(doubleList.get(0)).setYTranslate(doubleList.get(1)).setZTranslate(doubleList.get(2));
    }

    private void scale(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 3) {
            throw new ColladaRuntimeException("Scale length must be 3. Current length: " + doubleList.size());
        }
        if (transform == null) {
            transform = new ShapeTransformTRS();
        }

        ((ShapeTransformTRS) transform).setXScale(doubleList.get(0)).setYScale(doubleList.get(1)).setZScale(doubleList.get(2));
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
            transform = new ShapeTransformTRS();
        }

        double radians = Math.toRadians(doubleList.get(3));
        if (doubleList.get(0) > 0.0) {
            ((ShapeTransformTRS) transform).setXRotate(radians);
        } else if (doubleList.get(1) > 0.0) {
            ((ShapeTransformTRS) transform).setYRotate(radians);
        } else if (doubleList.get(2) > 0.0) {
            ((ShapeTransformTRS) transform).setZRotate(radians);
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
