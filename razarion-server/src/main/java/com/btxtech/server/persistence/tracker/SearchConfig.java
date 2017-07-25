package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 29.05.2017.
 */
public class SearchConfig {
    private Date fromDate;
    private boolean botFilter;

    public Date getFromDate() {
        return fromDate;
    }

    public SearchConfig setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public boolean isBotFilter() {
        return botFilter;
    }

    public SearchConfig setBotFilter(boolean botFilter) {
        this.botFilter = botFilter;
        return this;
    }
}
