package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShaderGraphProperties {
    @JsonProperty("JSONnodeData")
    private String jsonNodeData;
    private ShaderGraphData shaderGraphData;

    public String getJsonNodeData() {
        return jsonNodeData;
    }

    public void setJsonNodeData(String jsonNodeData) {
        this.jsonNodeData = jsonNodeData;
    }

    public String getReferenceName() {
        return shaderGraphData.getDefaultReferenceName();
    }

    public ShaderGraphData getShaderGraphData() {
        return shaderGraphData;
    }

    public void readJsonNodeData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            shaderGraphData = mapper.readValue(jsonNodeData, ShaderGraphData.class);
        } catch (Throwable e) {
            throw new RuntimeException("Error reading JsonNodeData: " + jsonNodeData, e);
        }
    }

    @Override
    public String toString() {
        return "ShaderGraphProperties{" +
                "shaderGraphData='" + shaderGraphData + '\'' +
                '}';
    }
}
