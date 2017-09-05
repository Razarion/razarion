package com.btxtech.server.persistence.backup;

import java.util.Date;

/**
 * Created by Beat
 * on 02.09.2017.
 */
public class BackupPlanetOverview {
    private Date date;
    private int planetId;
    private int bases;
    private int items;
    private int quests;

    public Date getDate() {
        return date;
    }

    public BackupPlanetOverview setDate(Date date) {
        this.date = date;
        return this;
    }

    public int getPlanetId() {
        return planetId;
    }

    public BackupPlanetOverview setPlanetId(int planetId) {
        this.planetId = planetId;
        return this;
    }

    public int getBases() {
        return bases;
    }

    public BackupPlanetOverview setBases(int bases) {
        this.bases = bases;
        return this;
    }

    public int getItems() {
        return items;
    }

    public BackupPlanetOverview setItems(int items) {
        this.items = items;
        return this;
    }

    public int getQuests() {
        return quests;
    }

    public BackupPlanetOverview setQuests(int quests) {
        this.quests = quests;
        return this;
    }
}
