package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

class UnityAssetConverterTest {
    @Test
    void createAssetConfig() throws IOException {
        AssetConfig assetConfig = UnityAssetConverter.createAssetConfig("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta", createAssetContext());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\mesh_container\\unityAssetConverterTestAssetConfig.json"), assetConfig);
    }

    private AssetContext createAssetContext() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Integer> guid2Shape3DId = mapper.readValue(new File("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-ui-service\\src\\test\\resources\\guid2Shape3DId.json"),
                    new TypeReference<Map<String, Integer>>() {
                    });

            return (fbx, materialInfo) -> {
                Integer shape3dId = guid2Shape3DId.get(fbx.getGuid());
                if (shape3dId == null) {
                    throw new IllegalArgumentException("No Shape3D für Unity FBX GUID: " + fbx.getGuid());
                }
                return shape3dId;
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}