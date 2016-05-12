package com.btxtech.server.collada;

import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
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

    public TerrainObject generateTerrainObjectEntity(Map<String, Geometry> geometries, ColladaConverterControl colladaConverterControl) {
        Collection<VertexContainer> allVertexContainers = new ArrayList<>();
        for (NodeScene nodeScene : nodeScenes) {
            LOGGER.finest("-:process node : " + nodeScene);
            Collection<VertexContainer> vertexContainers = nodeScene.processGeometry(colladaConverterControl, geometries);
            if (vertexContainers != null) {
                allVertexContainers.addAll(vertexContainers);
            }
        }
        TerrainObject terrainObject = new TerrainObject();
        terrainObject.setId(colladaConverterControl.getObjectId());
        terrainObject.setVertexContainers(allVertexContainers);
        return terrainObject;
    }

    @Override
    public String toString() {
        return "VisualScene{" +
                super.toString() +
                "nodeScenes=" + nodeScenes +
                '}';
    }
}
