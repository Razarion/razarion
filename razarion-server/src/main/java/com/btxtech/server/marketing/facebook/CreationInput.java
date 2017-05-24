package com.btxtech.server.marketing.facebook;

import com.btxtech.server.marketing.Interest;

import java.util.Date;
import java.util.List;
import java.util.function.DoubleFunction;

/**
 * Created by Beat
 * 02.05.2017.
 */
public class CreationInput {
    private String title;
    private String body;
    private FbAdImage fbAdImage;
    private List<Interest> interests;
    private String urlTagParam;
    private Date scheduleStartTime;
    private Date scheduleEndTime;
    private boolean lifeTime;
    private Double dailyBudget;
    private Double lifeTimeBudget;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public FbAdImage getFbAdImage() {
        return fbAdImage;
    }

    public void setFbAdImage(FbAdImage fbAdImage) {
        this.fbAdImage = fbAdImage;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests;
    }

    public String getUrlTagParam() {
        return urlTagParam;
    }

    public void setUrlTagParam(String urlTagParam) {
        this.urlTagParam = urlTagParam;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(Date scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

    public boolean isLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(boolean lifeTime) {
        this.lifeTime = lifeTime;
    }

    public Double getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(Double dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public Double getLifeTimeBudget() {
        return lifeTimeBudget;
    }

    public void setLifeTimeBudget(Double lifeTimeBudget) {
        this.lifeTimeBudget = lifeTimeBudget;
    }
}
