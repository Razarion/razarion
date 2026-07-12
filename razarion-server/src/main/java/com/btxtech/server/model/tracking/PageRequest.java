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

}
