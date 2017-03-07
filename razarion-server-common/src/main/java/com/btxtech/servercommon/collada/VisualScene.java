package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class VisualScene extends NameIdColladaXml {
    private static Logger LOGGER = Logger.getLogger(VisualScene.class.getName());
    private Map<String, NodeScene> nodeScenes;

    public VisualScene(Node node) {
        super(node);

        nodeScenes = new HashMap<>();
        for (Node innerNode : getChildren(node, ELEMENT_NODE)) {
            NodeScene nodeScene = new NodeScene(innerNode);
            nodeScenes.put(nodeScene.getId(), nodeScene);
        }
    }

    public Shape3D create(int id, Map<String, Geometry> geometries, Map<String, Material> materials, Map<String, Effect> effects) {
        Shape3D shape3D = new Shape3D();
        shape3D.setDbId(id);
        shape3D.setInternalName(getId() + ":" + getName());
        List<Element3D> element3Ds = new ArrayList<>();
        for (NodeScene nodeScene : nodeScenes.values()) {
            LOGGER.finest("-:convert node : " + nodeScene);
            Element3D element3D = nodeScene.convert(geometries, materials, effects);
            if (element3D != null) {
                element3D.updateVertexContainerKey(shape3D);
                element3Ds.add(element3D);
            }
        }
        shape3D.setElement3Ds(element3Ds);
        return shape3D;
    }

    public NodeScene getNodeScene(String id) {
        return nodeScenes.get(id);
    }

    @Override
    public String toString() {
        return "VisualScene{" +
                super.toString() +
                "nodeScenes=" + nodeScenes +
                '}';
    }
}
