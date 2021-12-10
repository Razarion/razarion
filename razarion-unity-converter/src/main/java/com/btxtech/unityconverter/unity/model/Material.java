package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Material extends Component{
    @JsonProperty("m_SavedProperties")
    private SavedProperties savedProperties;
    @JsonProperty("m_Shader")
    private Reference shader;

    public SavedProperties getSavedProperties() {
        return savedProperties;
    }

    public void setSavedProperties(SavedProperties savedProperties) {
        this.savedProperties = savedProperties;
    }

    public Reference getShader() {
        return shader;
    }

    public void setShader(Reference shader) {
        this.shader = shader;
    }

    @Override
    public String toString() {
        return "Material{" +
                "savedProperties=" + savedProperties +
                ", shader=" + shader +
                '}';
    }
}
