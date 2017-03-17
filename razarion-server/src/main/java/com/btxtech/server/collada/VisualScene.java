package com.btxtech.server.collada;

import org.w3c.dom.Node;

import java.util.HashMap;
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

    public Shape3DBuilder create(Map<String, Geometry> geometries, Map<String, Material> materials, Map<String, Effect> effects) {
        Shape3DBuilder shape3DBuilder = new Shape3DBuilder();
        shape3DBuilder.setInternalName(getId() + ":" + getName());
        for (NodeScene nodeScene : nodeScenes.values()) {
            LOGGER.finest("-:convert node : " + nodeScene);
            Element3DBuilder element3DBuilder = nodeScene.create(geometries, materials, effects);
            if (element3DBuilder != null) {
                shape3DBuilder.addElement3DBuilder(element3DBuilder);
            }
        }
        return shape3DBuilder;
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
