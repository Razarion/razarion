package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ShaderGraph {
    @JsonProperty("m_SerializedProperties")
    private List<ShaderGraphProperties> shaderGraphProperties;

    public List<ShaderGraphProperties> getShaderGraphProperties() {
        return shaderGraphProperties;
    }

    public void setShaderGraphProperties(List<ShaderGraphProperties> shaderGraphProperties) {
        this.shaderGraphProperties = shaderGraphProperties;
    }

    public void readInner() {
        shaderGraphProperties.forEach(ShaderGraphProperties::readJsonNodeData);
    }

    public ShaderGraphData findShaderGraphData4tReferenceName(String referenceName) {
        return shaderGraphProperties.stream()
                .filter(shaderGraphProperties1 -> referenceName.equals(shaderGraphProperties1.getReferenceName()))
                .findFirst()
                .map(ShaderGraphProperties::getShaderGraphData)
                .orElse(null);
    }

    @Override
    public String toString() {
        return "ShaderGraph{" +
                "shaderGraphProperties=" + shaderGraphProperties +
                '}';
    }
}
