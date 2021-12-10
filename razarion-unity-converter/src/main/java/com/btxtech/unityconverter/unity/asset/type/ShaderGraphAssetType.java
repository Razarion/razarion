package com.btxtech.unityconverter.unity.asset.type;

import com.btxtech.unityconverter.unity.asset.meta.Meta;
import com.btxtech.unityconverter.unity.model.ShaderGraph;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShaderGraphAssetType extends AssetType {
    private ShaderGraph shaderGraph;

    public ShaderGraphAssetType(Meta meta) {
        super(meta);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.findAndRegisterModules();
            shaderGraph = mapper.readValue(getAssetFile(), ShaderGraph.class);
            shaderGraph.readInner();
        } catch (Throwable e) {
            throw new RuntimeException("Error reading: " + getMeta().getAssetFile(), e);
        }
    }

    public ShaderGraph getShaderGraph() {
        return shaderGraph;
    }

    @Override
    public String toString() {
        return "ShaderGraphAssetType{" +
                "shaderGraph=" + shaderGraph +
                '}';
    }
}
