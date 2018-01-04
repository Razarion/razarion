package com.btxtech.server.mgmt;

import java.util.Date;

/**
 * Created by Beat
 * on 04.01.2018.
 */
public class GameHistoryEntry {
    private Date date;
    private String description;

    public Date getDate() {
        return date;
    }

    public GameHistoryEntry setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GameHistoryEntry setDescription(String description) {
        this.description = description;
        return this;
    }
}
