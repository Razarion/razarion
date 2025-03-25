package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 24.01.2018.
 */
public class FbAuthResponse {
    private String accessToken;
    private Integer expiresIn;
    private String signedRequest;
    private String userID;

    public String getAccessToken() {
        return accessToken;
    }

    public FbAuthResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public FbAuthResponse setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public String getSignedRequest() {
        return signedRequest;
    }

    public FbAuthResponse setSignedRequest(String signedRequest) {
        this.signedRequest = signedRequest;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public FbAuthResponse setUserID(String userID) {
        this.userID = userID;
        return this;
    }
}
