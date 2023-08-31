package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class SlavePlanetConfig {
    private PlaceConfig startRegion;
    private DecimalPosition noBaseViewPosition;

    public PlaceConfig getStartRegion() {
        return startRegion;
    }

    public void setStartRegion(PlaceConfig startRegion) {
        this.startRegion = startRegion;
    }

    public SlavePlanetConfig startRegion(PlaceConfig startRegion) {
        setStartRegion(startRegion);
        return this;
    }

    public DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public void setNoBaseViewPosition(DecimalPosition noBaseViewPosition) {
        this.noBaseViewPosition = noBaseViewPosition;
    }

    public SlavePlanetConfig noBaseViewPosition(DecimalPosition noBaseViewPosition) {
        setNoBaseViewPosition(noBaseViewPosition);
        return this;
    }
}
