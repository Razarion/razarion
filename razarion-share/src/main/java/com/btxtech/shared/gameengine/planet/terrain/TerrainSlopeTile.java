package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 03.04.2017.
 */
@JsType
@Deprecated
public class TerrainSlopeTile {
    public int slopeConfigId;
    public SlopeGeometry outerSlopeGeometry;
    public SlopeGeometry centerSlopeGeometry;
    public SlopeGeometry innerSlopeGeometry;

    public int getSlopeConfigId() {
        return slopeConfigId;
    }

    public void setSlopeConfigId(int slopeConfigId) {
        this.slopeConfigId = slopeConfigId;
    }

    public SlopeGeometry getOuterSlopeGeometry() {
        return outerSlopeGeometry;
    }

    public void setOuterSlopeGeometry(SlopeGeometry outerSlopeGeometry) {
        this.outerSlopeGeometry = outerSlopeGeometry;
    }

    public SlopeGeometry getCenterSlopeGeometry() {
        return centerSlopeGeometry;
    }

    public void setCenterSlopeGeometry(SlopeGeometry centerSlopeGeometry) {
        this.centerSlopeGeometry = centerSlopeGeometry;
    }

    public SlopeGeometry getInnerSlopeGeometry() {
        return innerSlopeGeometry;
    }

    public void setInnerSlopeGeometry(SlopeGeometry innerSlopeGeometry) {
        this.innerSlopeGeometry = innerSlopeGeometry;
    }
}
