package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Float32ArrayEmu;

public class SlopeGeometry {
    private Float32ArrayEmu positions;
    private Float32ArrayEmu norms;
    private Float32ArrayEmu uvs;
    private Float32ArrayEmu slopeFactors;

    public Float32ArrayEmu getPositions() {
        return positions;
    }

    public void setPositions(Float32ArrayEmu positions) {
        this.positions = positions;
    }

    public Float32ArrayEmu getNorms() {
        return norms;
    }

    public void setNorms(Float32ArrayEmu norms) {
        this.norms = norms;
    }

    public Float32ArrayEmu getUvs() {
        return uvs;
    }

    public void setUvs(Float32ArrayEmu uvs) {
        this.uvs = uvs;
    }

    public Float32ArrayEmu getSlopeFactors() {
        return slopeFactors;
    }

    public void setSlopeFactors(Float32ArrayEmu slopeFactors) {
        this.slopeFactors = slopeFactors;
    }
}
