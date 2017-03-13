package com.btxtech.shared.datatypes.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;

/**
 * Created by Beat
 * 13.03.2017.
 */
public class TerrainUi {
    private int elementCount;
    private Float32ArrayEmu vertices;
    private Float32ArrayEmu norms;
    private Float32ArrayEmu tangents;

    public TerrainUi() {
    }

    public TerrainUi(int elementCount, Float32ArrayEmu vertices, Float32ArrayEmu norms, Float32ArrayEmu tangents) {
        this.elementCount = elementCount;
        this.vertices = vertices;
        this.norms = norms;
        this.tangents = tangents;
    }

    public int getElementCount() {
        return elementCount;
    }

    public Float32ArrayEmu getVertices() {
        return vertices;
    }

    public void setVertices(Float32ArrayEmu vertices) {
        this.vertices = vertices;
    }

    public Float32ArrayEmu getNorms() {
        return norms;
    }

    public void setNorms(Float32ArrayEmu norms) {
        this.norms = norms;
    }

    public Float32ArrayEmu getTangents() {
        return tangents;
    }

    public void setTangents(Float32ArrayEmu tangents) {
        this.tangents = tangents;
    }

    public void setBuffers(TerrainUi terrainUi) {
        elementCount = terrainUi.getElementCount();
        vertices = terrainUi.getVertices();
        norms = terrainUi.getNorms();
        tangents = terrainUi.getTangents();
    }

}
