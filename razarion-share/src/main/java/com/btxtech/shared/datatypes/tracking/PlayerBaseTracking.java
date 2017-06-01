package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class PlayerBaseTracking extends DetailedTracking {
    private PlayerBaseInfo playerBaseInfo;
    private Integer deletedBaseId;

    public PlayerBaseInfo getPlayerBaseInfo() {
        return playerBaseInfo;
    }

    public PlayerBaseTracking setPlayerBaseInfo(PlayerBaseInfo playerBaseInfo) {
        this.playerBaseInfo = playerBaseInfo;
        return this;
    }

    public Integer getDeletedBaseId() {
        return deletedBaseId;
    }

    public PlayerBaseTracking setDeletedBaseId(Integer deletedBaseId) {
        this.deletedBaseId = deletedBaseId;
        return this;
    }
}
