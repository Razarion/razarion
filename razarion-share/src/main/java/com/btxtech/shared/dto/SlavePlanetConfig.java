package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

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
