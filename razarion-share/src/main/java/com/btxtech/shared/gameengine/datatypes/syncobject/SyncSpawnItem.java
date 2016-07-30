package com.btxtech.shared.gameengine.datatypes.syncobject;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpawnItemType;
import com.btxtech.shared.gameengine.planet.PlanetService;

/**
 * Created by Beat
 * 25.07.2016.
 */
public class SyncSpawnItem extends SyncItem {
    private double progress;
    private ItemType toBeCreated;
    private PlayerBase playerBase;

    public void setup(ItemType toBeCreated, PlayerBase playerBase) {
        this.toBeCreated = toBeCreated;
        this.playerBase = playerBase;
        progress = 0;
    }

    public void tick() {
        progress += PlanetService.TICK_FACTOR / ((SpawnItemType) getItemType()).getDuration();
        if (progress > 1.0) {
            progress = 1.0;
        }
    }

    public boolean isFinished() {
        return progress >= 1.0;
    }

    public double getProgress() {
        return progress;
    }

    public ItemType getToBeCreated() {
        return toBeCreated;
    }

    public PlayerBase getPlayerBase() {
        return playerBase;
    }

    @Override
    public boolean isAlive() {
        return !isFinished();
    }


}
