package com.btxtech.shared.gameengine.datatypes.packets;

/**
 * Created by Beat
 * on 31.08.2017.
 */
public class BackupPlayerBaseInfo extends PlayerBaseInfo {
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
