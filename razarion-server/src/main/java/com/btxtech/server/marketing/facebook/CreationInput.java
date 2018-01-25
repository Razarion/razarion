package com.btxtech.server.marketing.facebook;

import com.btxtech.server.marketing.Interest;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    private Integer scheduleHelperStartHour;
    private Integer scheduleHelperDuration;
    private String custom;

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

    public Integer getScheduleHelperStartHour() {
        return scheduleHelperStartHour;
    }

    public void setScheduleHelperStartHour(Integer scheduleHelperStartHour) {
        this.scheduleHelperStartHour = scheduleHelperStartHour;
    }

    public Integer getScheduleHelperDuration() {
        return scheduleHelperDuration;
    }

    public void setScheduleHelperDuration(Integer scheduleHelperDuration) {
        this.scheduleHelperDuration = scheduleHelperDuration;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public boolean hasCustom() {
        return custom != null && !custom.trim().isEmpty();
    }

    public void scheduleHelperGenerate() {
        if (scheduleHelperStartHour != null && scheduleHelperDuration != null) {
            Calendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, scheduleHelperStartHour);
            gregorianCalendar.set(Calendar.MINUTE, 0);
            gregorianCalendar.set(Calendar.SECOND, 0);
            gregorianCalendar.set(Calendar.MILLISECOND, 0);
            scheduleStartTime = gregorianCalendar.getTime();
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, scheduleHelperStartHour + scheduleHelperDuration);
            scheduleEndTime = gregorianCalendar.getTime();
            scheduleHelperStartHour = null;
            scheduleHelperDuration = null;
        }
    }
}
