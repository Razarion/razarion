package com.btxtech.server.marketing.facebook;

import java.util.Date;

/**
 * Created by Beat
 * 20.03.2017.
 */
public class AdSetInsight {
    private Date facebookDateStart;
    private Date facebookDateStop;
    private int clicks;
    private int impressions;
    private double spent;

    public void setFacebookDateStart(Date facebookDateStart) {
        this.facebookDateStart = facebookDateStart;
    }

    public Date getFacebookDateStart() {
        return facebookDateStart;
    }

    public void setFacebookDateStop(Date facebookDateStop) {
        this.facebookDateStop = facebookDateStop;
    }

    public Date getFacebookDateStop() {
        return facebookDateStop;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getClicks() {
        return clicks;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getSpent() {
        return spent;
    }

    @Override
    public String toString() {
        return "AdSetInsight{" +
                "facebookDateStart=" + facebookDateStart +
                ", facebookDateStop=" + facebookDateStop +
                ", clicks=" + clicks +
                ", impressions=" + impressions +
                ", spent=" + spent +
                '}';
    }
}
