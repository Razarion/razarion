package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class FacebookUserLoginInfo {
    private String accessToken;
    private int expiresIn;
    private String signedRequest;
    private String userId;

    public String getAccessToken() {
        return accessToken;
    }

    public FacebookUserLoginInfo setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public FacebookUserLoginInfo setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public String getSignedRequest() {
        return signedRequest;
    }

    public FacebookUserLoginInfo setSignedRequest(String signedRequest) {
        this.signedRequest = signedRequest;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public FacebookUserLoginInfo setUserId(String userId) {
        this.userId = userId;
        return this;
    }
}
