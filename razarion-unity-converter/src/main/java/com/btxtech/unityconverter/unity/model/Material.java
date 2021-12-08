package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Material extends Component{
    @JsonProperty("m_SavedProperties")
    private SavedProperties savedProperties;

    public SavedProperties getSavedProperties() {
        return savedProperties;
    }

    public void setSavedProperties(SavedProperties savedProperties) {
        this.savedProperties = savedProperties;
    }

    @Override
    public String toString() {
        return "Material{" +
                "savedProperties=" + savedProperties +
                '}';
    }
}
