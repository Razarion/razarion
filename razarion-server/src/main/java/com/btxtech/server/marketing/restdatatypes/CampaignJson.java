package com.btxtech.server.marketing.restdatatypes;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 28.04.2017.
 */
public class CampaignJson {
    private String adId;
    private Date dateStart;
    private Date dateStop;
    private int clicks;
    private int impressions;
    private double spent;
    private String title;
    private String body;
    private String urlTagParam;
    private List<AdInterestJson> adInterests;
    private List<ClicksPerHourJson> clicksPerHour;


    public String getAdId() {
        return adId;
    }

    public CampaignJson setAdId(String adId) {
        this.adId = adId;
        return this;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public CampaignJson setDateStart(Date dateStart) {
        this.dateStart = dateStart;
        return this;
    }

    public Date getDateStop() {
        return dateStop;
    }

    public CampaignJson setDateStop(Date dateStop) {
        this.dateStop = dateStop;
        return this;
    }

    public int getClicks() {
        return clicks;
    }

    public CampaignJson setClicks(int clicks) {
        this.clicks = clicks;
        return this;
    }

    public int getImpressions() {
        return impressions;
    }

    public CampaignJson setImpressions(int impressions) {
        this.impressions = impressions;
        return this;
    }

    public double getSpent() {
        return spent;
    }

    public CampaignJson setSpent(double spent) {
        this.spent = spent;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CampaignJson setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public CampaignJson setBody(String body) {
        this.body = body;
        return this;
    }

    public String getUrlTagParam() {
        return urlTagParam;
    }

    public CampaignJson setUrlTagParam(String urlTagParam) {
        this.urlTagParam = urlTagParam;
        return this;
    }

    public List<AdInterestJson> getAdInterests() {
        return adInterests;
    }

    public CampaignJson setAdInterests(List<AdInterestJson> adInterests) {
        this.adInterests = adInterests;
        return this;
    }

    public List<ClicksPerHourJson> getClicksPerHour() {
        return clicksPerHour;
    }

    public CampaignJson setClicksPerHour(List<ClicksPerHourJson> clicksPerHour) {
        this.clicksPerHour = clicksPerHour;
        return this;
    }
}
