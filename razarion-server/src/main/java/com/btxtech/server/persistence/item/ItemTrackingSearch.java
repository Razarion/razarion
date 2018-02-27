package com.btxtech.server.persistence.item;

import java.util.Date;

/**
 * Created by Beat
 * on 08.01.2018.
 */
public class ItemTrackingSearch {
    private Date from;
    private Date to;
    private Integer humanPlayerId;
    private Integer count;

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

    public Integer getHumanPlayerId() {
        return humanPlayerId;
    }

    public ItemTrackingSearch setHumanPlayerId(Integer humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public ItemTrackingSearch setCount(Integer count) {
        this.count = count;
        return this;
    }
}
