package com.btxtech.server.marketing;

import java.util.Date;

/**
 * Created by Beat
 * 19.03.2017.
 */
public class AdSetInsight {
    private Date dateStart;
    private Date dateStop; // Wrong data from facebook
    private int clicks;
    private int impressions;
    private double spent;

    public Date getDateStart() {
        return dateStart;
    }

    public AdSetInsight setDateStart(Date dateStart) {
        this.dateStart = dateStart;
        return this;
    }

    public Date getDateStop() {
        return dateStop;
    }

    public AdSetInsight setDateStop(Date dateStop) {
        this.dateStop = dateStop;
        return this;
    }

    public int getClicks() {
        return clicks;
    }

    public AdSetInsight setClicks(int clicks) {
        this.clicks = clicks;
        return this;
    }

    public int getImpressions() {
        return impressions;
    }

    public AdSetInsight setImpressions(int impressions) {
        this.impressions = impressions;
        return this;
    }

    public double getSpent() {
        return spent;
    }

    public AdSetInsight setSpent(double spent) {
        this.spent = spent;
        return this;
    }

    @Override
    public String toString() {
        return "AdSetInsight{" +
                "dateStart=" + dateStart +
                ", dateStop=" + dateStop +
                ", clicks=" + clicks +
                ", impressions=" + impressions +
                ", spent=" + spent +
                '}';
    }
}
