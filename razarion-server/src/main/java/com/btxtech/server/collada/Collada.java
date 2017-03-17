package com.btxtech.server.collada;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
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
    private Map<String, Effect> effects = new HashMap<>();
    private Map<String, Material> materials = new HashMap<>();
    private Map<String, VisualScene> visualScenes = new HashMap<>();
    private Collection<Animation> animations = new ArrayList<>();
    private Scene scene;

    public Collada(Document doc) {
        Node collada = getChild(doc, ELEMENT_COLLADA);
        String version = getAttributeAsString(collada, ATTRIBUTE_VERSION);
        LOGGER.finest("version: " + version);
        if (!version.equals("1.4") && !version.equals("1.4.1")) {
            throw new ColladaRuntimeException("Unknown Collada version: " + version);
        }

        readAsset(doc);
        readVisualScenes(doc);
        readGeometries(doc);
        readMaterials(doc);
        readEffects(doc);
        readAnimations(doc);
        scene = new Scene(getChild(collada, ELEMENT_SCENE));
        LOGGER.finest("scene: " + scene);
    }

    public Shape3DBuilder create() {
        LOGGER.finest("convert");
        VisualScene visualScene = visualScenes.get(scene.getVisualSceneUrl());
        if (visualScene == null) {
            throw new ColladaRuntimeException("No visual scene found for url: " + scene.getVisualSceneUrl());
        }

        Shape3DBuilder shape3DBuilder = visualScene.create(geometries, materials, effects);
        shape3DBuilder.setAnimations(animations);
        return shape3DBuilder;
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

    private void readMaterials(Document doc) {
        LOGGER.finest("readMaterials");
        Node libraryMaterials = getSingleTopLevelNodeOptional(doc, ELEMENT_LIBRARY_MATERIALS);
        if (libraryMaterials == null) {
            return;
        }

        for (Node node : getChildren(libraryMaterials, ELEMENT_MATERIAL)) {
            Material material = new Material(node);
            LOGGER.finest("-:" + material);
            materials.put(material.getId(), material);
        }
    }

    private void readEffects(Document doc) {
        LOGGER.finest("readEffects");
        Node libraryEffects = getSingleTopLevelNodeOptional(doc, ELEMENT_LIBRARY_EFFECTS);
        if (libraryEffects == null) {
            return;
        }

        for (Node node : getChildren(libraryEffects, ELEMENT_EFFECT)) {
            Effect effect = new Effect(node);
            LOGGER.finest("-:" + effect);
            effects.put(effect.getId(), effect);
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

    private void readAnimations(Document doc) {
        LOGGER.finest("readAnimations");
        Node libraryAnimations = getSingleTopLevelNodeOptional(doc, ELEMENT_LIBRARY_ANIMATIONS);
        if (libraryAnimations == null) {
            return;
        }

        for (Node node : getChildren(libraryAnimations, ELEMENT_ANIMATION)) {
            Animation animation = new Animation(node);
            LOGGER.finest("-:" + animation);
            animations.add(animation);
        }
    }
}
