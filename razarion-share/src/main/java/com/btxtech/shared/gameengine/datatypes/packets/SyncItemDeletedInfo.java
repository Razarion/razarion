package com.btxtech.shared.gameengine.datatypes.packets;

import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * 24.04.2017.
 */
@JSONMapper
public class SyncItemDeletedInfo {
    private int id;
    private boolean explode;

    public int getId() {
        return id;
    }

    public SyncItemDeletedInfo setId(int id) {
        this.id = id;
        return this;
    }

    public boolean isExplode() {
        return explode;
    }

    public SyncItemDeletedInfo setExplode(boolean explode) {
        this.explode = explode;
        return this;
    }

    @Override
    public String toString() {
        return "SyncItemDeletedInfo{" +
                "id=" + id +
                ", explode=" + explode +
                '}';
    }
}
