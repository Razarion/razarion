package com.btxtech.server.persistence.backup;

import java.util.Date;

/**
 * Created by Beat
 * on 02.09.2017.
 */
public class BackupBaseOverview {
    private Date date;
    private int planetId;
    private int bases;
    private int items;

    public Date getDate() {
        return date;
    }

    public BackupBaseOverview setDate(Date date) {
        this.date = date;
        return this;
    }

    public int getPlanetId() {
        return planetId;
    }

    public BackupBaseOverview setPlanetId(int planetId) {
        this.planetId = planetId;
        return this;
    }

    public int getBases() {
        return bases;
    }

    public BackupBaseOverview setBases(int bases) {
        this.bases = bases;
        return this;
    }

    public int getItems() {
        return items;
    }

    public BackupBaseOverview setItems(int items) {
        this.items = items;
        return this;
    }
}
