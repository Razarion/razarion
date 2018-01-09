package com.btxtech.server.persistence.item;

import java.util.Date;

/**
 * Created by Beat
 * on 08.01.2018.
 */
public class ItemTrackingSearch {
    private Date from;
    private Date to;

    public Date getFrom() {
        return from;
    }

    public ItemTrackingSearch setFrom(Date from) {
        this.from = from;
        return this;
    }

    public Date getTo() {
        return to;
    }

    public ItemTrackingSearch setTo(Date to) {
        this.to = to;
        return this;
    }
}
