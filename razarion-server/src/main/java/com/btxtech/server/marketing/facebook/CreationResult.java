package com.btxtech.server.marketing.facebook;

/**
 * Created by Beat
 * 21.03.2017.
 */
public class CreationResult {
    private long campaignId;
    private long adSetId;
    private long adId;

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setAdSetId(long adSetId) {
        this.adSetId = adSetId;
    }

    public long getAdSetId() {
        return adSetId;
    }

    public void setAdId(long adId) {
        this.adId = adId;
    }

    public long getAdId() {
        return adId;
    }
}
