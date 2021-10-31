package com.btxtech.unityconverter.unity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Texture2D {
    @JsonProperty("m_Texture")
    private Reference texture;

    public Reference getTexture() {
        return texture;
    }

    public void setTexture(Reference texture) {
        this.texture = texture;
    }

    @Override
    public String toString() {
        return "Texture2D{" +
                "texture=" + texture +
                '}';
    }
}
