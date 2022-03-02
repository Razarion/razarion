package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import jsinterop.annotations.JsType;

@JsType
public class SlopeGeometry {
    public Float32ArrayEmu positions;
    public Float32ArrayEmu norms;
    public Float32ArrayEmu uvs;
    public Float32ArrayEmu slopeFactors;

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
