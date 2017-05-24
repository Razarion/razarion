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
    private String imageUrl;
    private String imageUrl128;
    private String urlTagParam;
    private Date scheduleTimeStart;
    private Date scheduleTimeEnd;
    private boolean lifeTime;
    private Double dailyBudget;
    private Double lifeTimeBudget;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public CampaignJson setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getImageUrl128() {
        return imageUrl128;
    }

    public CampaignJson setImageUrl128(String imageUrl128) {
        this.imageUrl128 = imageUrl128;
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

    public Date getScheduleTimeStart() {
        return scheduleTimeStart;
    }

    public CampaignJson setScheduleTimeStart(Date scheduleTimeStart) {
        this.scheduleTimeStart = scheduleTimeStart;
        return this;
    }

    public Date getScheduleTimeEnd() {
        return scheduleTimeEnd;
    }

    public CampaignJson setScheduleTimeEnd(Date scheduleTimeEnd) {
        this.scheduleTimeEnd = scheduleTimeEnd;
        return this;
    }

    public boolean isLifeTime() {
        return lifeTime;
    }

    public CampaignJson setLifeTime(boolean lifeTime) {
        this.lifeTime = lifeTime;
        return this;
    }

    public Double getDailyBudget() {
        return dailyBudget;
    }

    public CampaignJson setDailyBudget(Double dailyBudget) {
        this.dailyBudget = dailyBudget;
        return this;
    }

    public Double getLifeTimeBudget() {
        return lifeTimeBudget;
    }

    public CampaignJson setLifeTimeBudget(Double lifeTimeBudget) {
        this.lifeTimeBudget = lifeTimeBudget;
        return this;
    }
}
