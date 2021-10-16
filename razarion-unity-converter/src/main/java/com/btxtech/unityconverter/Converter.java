package com.btxtech.unityconverter;

import com.btxtech.unityconverter.unity.asset.Asset;
import com.btxtech.unityconverter.unity.asset.AssetReader;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Converter {
    private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

    public static void main(String[] args) {
        try {
            Asset asset = AssetReader.read("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor");
            // LOGGER.info("Asset:" + asset);
            // LOGGER.info("Fbxes:" + asset.getFbxes());
            // LOGGER.info("Prefabs:" + asset.getPrefabs());
            asset.getPrefabs().forEach(prefab -> prefab.readGameObject());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
    }
}
