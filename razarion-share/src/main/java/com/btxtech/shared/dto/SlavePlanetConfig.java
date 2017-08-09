package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class SlavePlanetConfig {
    private Polygon2D startRegion;
    private QuestConfig activeQuest;

    public Polygon2D getStartRegion() {
        return startRegion;
    }

    public SlavePlanetConfig setStartRegion(Polygon2D startRegion) {
        this.startRegion = startRegion;
        return this;
    }

    public QuestConfig getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfig activeQuest) {
        this.activeQuest = activeQuest;
    }
}
