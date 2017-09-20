package com.btxtech.shared.gameengine.datatypes.packets;

import java.util.Map;

/**
 * Created by Beat
 * on 31.08.2017.
 */
public class BackupPlayerBaseInfo extends PlayerBaseInfo {
    private int level;
    private Map<Integer, Integer> unlockedItemLimit;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public void setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
    }
}
