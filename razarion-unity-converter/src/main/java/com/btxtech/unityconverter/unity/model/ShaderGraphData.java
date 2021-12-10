package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShaderGraphData {
    @JsonProperty("m_DefaultReferenceName")
    private String defaultReferenceName;
    @JsonProperty("m_Name")
    private String name;
     @JsonProperty("m_Value")
     private Object value;

    public String getDefaultReferenceName() {
        return defaultReferenceName;
    }

    public void setDefaultReferenceName(String defaultReferenceName) {
        this.defaultReferenceName = defaultReferenceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ShaderGraphData{" +
                "defaultReferenceName='" + defaultReferenceName + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
