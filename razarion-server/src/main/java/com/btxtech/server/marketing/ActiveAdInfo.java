package com.btxtech.server.marketing;

import com.btxtech.server.marketing.restdatatypes.AdInterestJson;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 20.01.2018.
 */
public class ActiveAdInfo {
    private AdState adState;
    private String title;
    private String body;
    private List<AdInterestJson> adInterestJsons;
    private String url128;
    private Date scheduledDateStart;
    private Date scheduledDateEnd;
    private long campaignId;
    private long adSetId;
    private long adId;
    private String urlTagParam;

    public AdState getAdState() {
        return adState;
    }

    public ActiveAdInfo setAdState(AdState adState) {
        this.adState = adState;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ActiveAdInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public ActiveAdInfo setBody(String body) {
        this.body = body;
        return this;
    }

    public List<AdInterestJson> getAdInterestJsons() {
        return adInterestJsons;
    }

    public ActiveAdInfo setAdInterestJsons(List<AdInterestJson> adInterestJsons) {
        this.adInterestJsons = adInterestJsons;
        return this;
    }

    public String getUrl128() {
        return url128;
    }

    public ActiveAdInfo setUrl128(String url128) {
        this.url128 = url128;
        return this;
    }

    public Date getScheduledDateStart() {
        return scheduledDateStart;
    }

    public ActiveAdInfo setScheduledDateStart(Date scheduledDateStart) {
        this.scheduledDateStart = scheduledDateStart;
        return this;
    }

    public Date getScheduledDateEnd() {
        return scheduledDateEnd;
    }

    public ActiveAdInfo setScheduledDateEnd(Date scheduledDateEnd) {
        this.scheduledDateEnd = scheduledDateEnd;
        return this;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public ActiveAdInfo setCampaignId(long campaignId) {
        this.campaignId = campaignId;
        return this;
    }

    public long getAdSetId() {
        return adSetId;
    }

    public ActiveAdInfo setAdSetId(long adSetId) {
        this.adSetId = adSetId;
        return this;
    }

    public long getAdId() {
        return adId;
    }

    public ActiveAdInfo setAdId(long adId) {
        this.adId = adId;
        return this;
    }

    public String getUrlTagParam() {
        return urlTagParam;
    }

    public ActiveAdInfo setUrlTagParam(String urlTagParam) {
        this.urlTagParam = urlTagParam;
        return this;
    }
}
