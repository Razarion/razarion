package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class SlavePlanetConfig {
    private Polygon2D startRegion;

    public Polygon2D getStartRegion() {
        return startRegion;
    }

    public SlavePlanetConfig setStartRegion(Polygon2D startRegion) {
        this.startRegion = startRegion;
        return this;
    }
}
