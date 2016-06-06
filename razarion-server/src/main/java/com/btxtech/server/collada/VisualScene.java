package com.btxtech.server.collada;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class VisualScene extends NameIdColladaXml {
    private static Logger LOGGER = Logger.getLogger(VisualScene.class.getName());
    private Collection<NodeScene> nodeScenes;

    public VisualScene(Node node) {
        super(node);

        nodeScenes = new ArrayList<>();
        for (Node innerNode : getChildren(node, ELEMENT_NODE)) {
            nodeScenes.add(new NodeScene(innerNode));
        }
    }

    public void convert(Map<String, Geometry> geometries, Map<String, Material> materials, Map<String, Effect> effects, ColladaConverterControl colladaConverterControl) {
        //Collection<TerrainObjectVertexContainer> allTerrainObjectVertexContainers = new ArrayList<>();
        for (NodeScene nodeScene : nodeScenes) {
            LOGGER.finest("-:convert node : " + nodeScene);
            nodeScene.convert(colladaConverterControl, geometries, materials, effects);
        }
    }

    @Override
    public String toString() {
        return "VisualScene{" +
                super.toString() +
                "nodeScenes=" + nodeScenes +
                '}';
    }
}
