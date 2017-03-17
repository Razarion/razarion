package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 19.08.2015.
 */
public class Scene extends ColladaXml {
    private InstanceVisualScene instanceVisualScene;

    public Scene(Node node) {
        instanceVisualScene = new InstanceVisualScene(getChild(node, ELEMENT_INSTANCE_VISUAL_SCENE));
    }

    public String getVisualSceneUrl() {
        return instanceVisualScene.getUrl();
    }

    @Override
    public String toString() {
        return "Scene{" +
                "instanceVisualScene=" + instanceVisualScene +
                '}';
    }
}
