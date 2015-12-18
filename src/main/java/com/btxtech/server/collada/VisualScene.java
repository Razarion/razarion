package com.btxtech.server.collada;

import com.btxtech.shared.VertexList;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public List<VertexList> generateVertexList(Map<String, Geometry> geometries) {
        List<VertexList> vertexLists = new ArrayList<>();

        for (NodeScene nodeScene : nodeScenes) {
            LOGGER.finest("-:process node : " + nodeScene);
            VertexList vertexList = nodeScene.processGeometry(geometries);
            if (vertexList != null) {
                vertexLists.add(vertexList);
            }
        }

        return vertexLists;
    }

    @Override
    public String toString() {
        return "VisualScene{" +
                super.toString() +
                "nodeScenes=" + nodeScenes +
                '}';
    }
}
