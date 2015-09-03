package com.btxtech.server.collada;

import com.btxtech.shared.VertexList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class Collada extends ColladaXml {
    private Map<String, Geometry> geometries = new HashMap<>();
    private Map<String, VisualScene> visualScenes = new HashMap<>();
    private Scene scene;

    public Collada(Document doc) {
        Node collada = getChild(doc, ELEMENT_COLLADA);
        String version = getAttributeAsString(collada, ATTRIBUTE_VERSION);
        if (!version.equals("1.4") && !version.equals("1.4.1")) {
            throw new ColladaRuntimeException("Unknown Collada version: " + version);
        }

        readVisualScenes(doc);
        readGeometries(doc);
        scene = new Scene(getChild(collada, ELEMENT_SCENE));
    }

    private void readGeometries(Document doc) {
        Node libraryGeometries = getSingleTopLevelNode(doc, ELEMENT_LIBRARY_GEOMETRIES);

        for (Node node : getChildren(libraryGeometries, ELEMENT_GEOMETRY)) {
            Geometry geometry = new Geometry(node);
            geometries.put(geometry.getId(), geometry);
        }
    }

    private void readVisualScenes(Document doc) {
        Node libraryVisualScenes = getSingleTopLevelNode(doc, ELEMENT_LIBRARY_VISUAL_SCENES);

        for (Node node : getChildren(libraryVisualScenes, ELEMENT_VISUAL_SCENE)) {
            VisualScene visualScene = new VisualScene(node);
            visualScenes.put(visualScene.getId(), visualScene);
        }
    }

    public VertexList generateVertexList() {
        VisualScene visualScene = visualScenes.get(scene.getVisualSceneUrl());
        if (visualScene == null) {
            throw new ColladaRuntimeException("No visual scene found for url: " + scene.getVisualSceneUrl());
        }

        return visualScene.generateVertexList(geometries);
    }
}
