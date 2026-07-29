package com.btxtech.server.model.tracking;

import java.util.Date;

public class PageRequest {
    private PageRequestType pageRequestType;
    private Date serverTime;
    private String rdtCid;
    private String twclid;
    private String utmCampaign;
    private String utmSource;
    private String utmMedium;
    private String rawQueryString;
    private String httpSessionId;
    /**
     * Browser and device of the visitor, straight from the request header. Without it a landing
     * page visit that never continues to the game is indistinguishable from a crawler or an ad
     * network's click verification, which fetch the pixel just like a browser does.
     */
    private String userAgent;
    /** Where the visitor came from, as far as the browser reveals it. */
    private String referer;
    /**
     * How long the visitor had the landing page open, in milliseconds. Only set on
     * {@link PageRequestType#HOME_EXIT}; null everywhere else.
     */
    private Integer dwellMillis;

    public PageRequestType getPageRequestType() {
        return pageRequestType;
    }

    public void setPageRequestType(PageRequestType pageRequestType) {
        this.pageRequestType = pageRequestType;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public String getRdtCid() {
        return rdtCid;
    }

    public void setRdtCid(String rdtCid) {
        this.rdtCid = rdtCid;
    }

    public String getTwclid() {
        return twclid;
    }

    public void setTwclid(String twclid) {
        this.twclid = twclid;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getRawQueryString() {
        return rawQueryString;
    }

    public void setRawQueryString(String rawQueryString) {
        this.rawQueryString = rawQueryString;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Integer getDwellMillis() {
        return dwellMillis;
    }

    public void setDwellMillis(Integer dwellMillis) {
        this.dwellMillis = dwellMillis;
    }

    public PageRequest pageRequestType(PageRequestType pageRequestType) {
        setPageRequestType(pageRequestType);
        return this;
    }

    public PageRequest serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public PageRequest rdtCid(String rdtCid) {
        setRdtCid(rdtCid);
        return this;
    }

    public PageRequest twclid(String twclid) {
        setTwclid(twclid);
        return this;
    }

    public PageRequest utmCampaign(String utmCampaign) {
        setUtmCampaign(utmCampaign);
        return this;
    }

    public PageRequest utmSource(String utmSource) {
        setUtmSource(utmSource);
        return this;
    }

    public PageRequest utmMedium(String utmMedium) {
        setUtmMedium(utmMedium);
        return this;
    }

    public PageRequest rawQueryString(String rawQueryString) {
        setRawQueryString(rawQueryString);
        return this;
    }

    public PageRequest httpSessionId(String httpSessionId) {
        setHttpSessionId(httpSessionId);
        return this;
    }

    public PageRequest userAgent(String userAgent) {
        setUserAgent(userAgent);
        return this;
    }

    public PageRequest referer(String referer) {
        setReferer(referer);
        return this;
    }

    public PageRequest dwellMillis(Integer dwellMillis) {
        setDwellMillis(dwellMillis);
        return this;
    }

}
