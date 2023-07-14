package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class SlavePlanetConfig {
    private Polygon2D startRegion;
    private DecimalPosition noBaseViewPosition;

    public Polygon2D getStartRegion() {
        return startRegion;
    }

    public void setStartRegion(Polygon2D startRegion) {
        this.startRegion = startRegion;
    }

    public SlavePlanetConfig startRegion(Polygon2D startRegion) {
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
