package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.gameengine.datatypes.packets.BackupPlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 31.08.2017.
 */
public class BackupBaseInfo {
    private Date date;
    private int planetId;
    private List<SyncBaseItemInfo> syncBaseItemInfos;
    private List<BackupPlayerBaseInfo> playerBaseInfos;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncBaseItemInfos;
    }

    public void setSyncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        this.syncBaseItemInfos = syncBaseItemInfos;
    }

    public List<BackupPlayerBaseInfo> getPlayerBaseInfos() {
        return playerBaseInfos;
    }

    public void setPlayerBaseInfos(List<BackupPlayerBaseInfo> playerBaseInfos) {
        this.playerBaseInfos = playerBaseInfos;
    }
}
