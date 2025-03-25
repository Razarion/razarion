package com.btxtech.shared.gameengine.datatypes.itemtype;

/**
 * Created by Beat
 * on 25.08.2017.
 */
public class SpecialType {
    private boolean miniTerrain;

    public boolean isMiniTerrain() {
        return miniTerrain;
    }

    public SpecialType setMiniTerrain(boolean miniTerrain) {
        this.miniTerrain = miniTerrain;
        return this;
    }
}
