package com.btxtech.server.collada;

import com.btxtech.shared.dto.TerrainObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class Collada extends ColladaXml {
    private static Logger LOGGER = Logger.getLogger(Collada.class.getName());
    private Map<String, Geometry> geometries = new HashMap<>();
    private Map<String, VisualScene> visualScenes = new HashMap<>();
    private Scene scene;

    public Collada(Document doc) {
        Node collada = getChild(doc, ELEMENT_COLLADA);
        String version = getAttributeAsString(collada, ATTRIBUTE_VERSION);
        LOGGER.finest("version: " + version);
        if (!version.equals("1.4") && !version.equals("1.4.1")) {
            throw new ColladaRuntimeException("Unknown Collada version: " + version);
        }

        readVisualScenes(doc);
        readGeometries(doc);
        scene = new Scene(getChild(collada, ELEMENT_SCENE));
        readAsset(doc);
        LOGGER.finest("scene: " + scene);
    }

    public TerrainObject generateTerrainObject(ColladaConverterControl colladaConverterControl) {
        LOGGER.finest("generateTerrainObject");
        VisualScene visualScene = visualScenes.get(scene.getVisualSceneUrl());
        if (visualScene == null) {
            throw new ColladaRuntimeException("No visual scene found for url: " + scene.getVisualSceneUrl());
        }

        return visualScene.generateTerrainObjectEntity(geometries, colladaConverterControl);
    }

    private void readAsset(Document doc) {
        LOGGER.finest("readAsset");
        try {
            Node assetNode = getSingleTopLevelNode(doc, ELEMENT_ASSET);
            Asset asset = new Asset(assetNode);
            LOGGER.finest("asset: " + asset);
        } catch (ColladaRuntimeException e) {
            LOGGER.log(Level.SEVERE, "readAsset failed", e);
        }
    }

    private void readGeometries(Document doc) {
        LOGGER.finest("readGeometries");
        Node libraryGeometries = getSingleTopLevelNode(doc, ELEMENT_LIBRARY_GEOMETRIES);

        for (Node node : getChildren(libraryGeometries, ELEMENT_GEOMETRY)) {
            Geometry geometry = new Geometry(node);
            LOGGER.finest("-:" + geometry);
            geometries.put(geometry.getId(), geometry);
        }
    }

    private void readVisualScenes(Document doc) {
        LOGGER.finest("readVisualScenes");
        Node libraryVisualScenes = getSingleTopLevelNode(doc, ELEMENT_LIBRARY_VISUAL_SCENES);

        for (Node node : getChildren(libraryVisualScenes, ELEMENT_VISUAL_SCENE)) {
            VisualScene visualScene = new VisualScene(node);
            LOGGER.finest("-:" + visualScene);
            visualScenes.put(visualScene.getId(), visualScene);
        }
    }
}
