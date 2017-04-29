package com.btxtech.server.marketing.restdatatypes;

import java.util.Date;

/**
 * Created by Beat
 * 28.04.2017.
 */
public class ClicksPerHourJson {
    private Date date;
    private int clicks;

    public Date getDate() {
        return date;
    }

    public ClicksPerHourJson setDate(Date date) {
        this.date = date;
        return this;
    }

    public int getClicks() {
        return clicks;
    }

    public ClicksPerHourJson setClicks(int clicks) {
        this.clicks = clicks;
        return this;
    }
}
