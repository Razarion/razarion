package com.btxtech.server.marketing.facebook;

import com.btxtech.server.marketing.Interest;

import java.util.Date;
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

    public String getTitle() {
        return title;
    }

    public CreationInput setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public CreationInput setBody(String body) {
        this.body = body;
        return this;
    }

    public FbAdImage getFbAdImage() {
        return fbAdImage;
    }

    public CreationInput setFbAdImage(FbAdImage fbAdImage) {
        this.fbAdImage = fbAdImage;
        return this;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public CreationInput setInterests(List<Interest> interests) {
        this.interests = interests;
        return this;
    }

    public String getUrlTagParam() {
        return urlTagParam;
    }

    public CreationInput setUrlTagParam(String urlTagParam) {
        this.urlTagParam = urlTagParam;
        return this;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public CreationInput setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
        return this;
    }

    public Date getScheduleEndTime() {
        return scheduleEndTime;
    }

    public CreationInput setScheduleEndTime(Date scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
        return this;
    }

    public boolean isLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(boolean lifeTime) {
        this.lifeTime = lifeTime;
    }
}
