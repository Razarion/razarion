package com.btxtech.server.model.tracking;

import java.util.Date;

public class UserActivity {
    private Date serverTime;
    private UserActivityType userActivityType;
    private String userId;
    private String detail;
    private String detail2;
    private String httpSessionId;

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public UserActivityType getUserActivityType() {
        return userActivityType;
    }

    public void setUserActivityType(UserActivityType userActivityType) {
        this.userActivityType = userActivityType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail2() {
        return detail2;
    }

    public void setDetail2(String detail2) {
        this.detail2 = detail2;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public UserActivity serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public UserActivity userActivityType(UserActivityType userActivityType) {
        setUserActivityType(userActivityType);
        return this;
    }

    public UserActivity userId(String userId) {
        setUserId(userId);
        return this;
    }

    public UserActivity detail(String detail) {
        setDetail(detail);
        return this;
    }

    public UserActivity detail2(String detail2) {
        setDetail2(detail2);
        return this;
    }

    public UserActivity httpSessionId(String httpSessionId) {
        setHttpSessionId(httpSessionId);
        return this;
    }

    @Override
    public String toString() {
        return "UserActivity{" +
                "userActivityType=" + userActivityType +
                ", userId='" + userId + '\'' +
                ", detail='" + detail + '\'' +
                ", httpSessionId='" + httpSessionId + '\'' +
                '}';
    }
}
