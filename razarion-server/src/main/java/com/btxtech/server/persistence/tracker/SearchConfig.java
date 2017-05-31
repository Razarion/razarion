package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 29.05.2017.
 */
public class SearchConfig {
    private Date fromDate;

    public Date getFromDate() {
        return fromDate;
    }

    public SearchConfig setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        return this;
    }
}
