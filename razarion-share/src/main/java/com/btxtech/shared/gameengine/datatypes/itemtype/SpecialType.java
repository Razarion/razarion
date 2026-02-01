package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

/**
 * Created by Beat
 * on 25.08.2017.
 */
@JsonPersistable
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
