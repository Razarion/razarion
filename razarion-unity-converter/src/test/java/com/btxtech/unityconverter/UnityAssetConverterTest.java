package com.btxtech.unityconverter;

import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.unityconverter.unity.asset.type.Fbx;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnityAssetConverterTest {
    @Test
    void createAssetConfig() throws URISyntaxException {
        URL url =  UnityAssetConverterTest.class.getClassLoader().getResource("Assets/Asset 1.meta");
        assertNotNull(url);
        AssetConfig assetConfig = UnityAssetConverter.createAssetConfig(Paths.get(url.toURI()).toString(), (fbx, materialContext) -> -123);
    }
}