package com.btxtech.client.shape3d;

import elemental2.core.Float32Array;

/**
 * Created by Beat
 * 07.03.2017.
 */
public class Shape3DBuffer {
    private Float32Array vertex;
    private Float32Array norm;
    private Float32Array textureCoordinate;

    public Shape3DBuffer(Float32Array vertex, Float32Array norm, Float32Array textureCoordinate) {
        this.vertex = vertex;
        this.norm = norm;
        this.textureCoordinate = textureCoordinate;
    }

    public Float32Array getVertex() {
        return vertex;
    }

    public Float32Array getNorm() {
        return norm;
    }

    public Float32Array getTextureCoordinate() {
        return textureCoordinate;
    }
}
