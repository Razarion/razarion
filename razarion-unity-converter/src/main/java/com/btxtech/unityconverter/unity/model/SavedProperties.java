package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class SavedProperties {
    @JsonProperty("m_TexEnvs")
    private List<Map<String, Texture2D>> texEnvs;

    public List<Map<String, Texture2D>> getTexEnvs() {
        return texEnvs;
    }

    public void setTexEnvs(List<Map<String, Texture2D>> texEnvs) {
        this.texEnvs = texEnvs;
    }

    @Override
    public String toString() {
        return "SavedProperties{" +
                "texEnvs=" + texEnvs +
                '}';
    }
}
