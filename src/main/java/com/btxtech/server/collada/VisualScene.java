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
public class VisualScene extends NameIdColladaXml {
    private Collection<NodeScene> nodeScenes;

    public VisualScene(Node node) {
        super(node);

        nodeScenes = new ArrayList<>();
        for (Node innerNode : getChildren(node, ELEMENT_NODE)) {
            nodeScenes.add(new NodeScene(innerNode));
        }
    }

    public VertexList generateVertexList(Map<String, Geometry> geometries) {
        VertexList vertexList = new VertexList();

        for (NodeScene nodeScene : nodeScenes) {
            nodeScene.processGeometry(vertexList, geometries);
        }

        return vertexList;
    }
}
