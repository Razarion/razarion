package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.LogManager;

class UnityAssetConverterTest {
    @Test
    @Ignore
    void createAssetConfig() throws IOException {
        try (InputStream is = UnityAssetConverter.class.getClassLoader().
                getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AssetConfig assetConfig = UnityAssetConverter.createAssetConfig("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta", createAssetContext());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\mesh_container\\unityAssetConverterTestAssetConfig.json"), assetConfig);
    }

    private AssetContext createAssetContext() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Integer> guid2ThreeJsModelId = mapper.readValue(new File("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-ui-service\\src\\test\\resources\\guid2ThreeJsModelId.json"),
                    new TypeReference<Map<String, Integer>>() {
                    });

            return (fbx, materialInfo, assetName) -> {
                Integer threeJsModelId = guid2ThreeJsModelId.get(fbx.getGuid());
                if (threeJsModelId == null) {
                    throw new IllegalArgumentException("No ThreeJsModel für Unity FBX GUID: " + fbx.getGuid());
                }
                return threeJsModelId;
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}