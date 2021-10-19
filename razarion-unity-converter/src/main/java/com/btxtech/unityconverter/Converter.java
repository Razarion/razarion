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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error Converter Asset", e);
        }
    }
}
