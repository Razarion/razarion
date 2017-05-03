package com.btxtech.server.web.marketing;

import com.btxtech.server.marketing.MarketingService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 21.03.2017.
 */
@Named
@RequestScoped
public class MarketingPageBean {
    @Inject
    private MarketingService marketingService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private String currentCampaigns;
    private String insights;
    private String campaignId;
    private String state;

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getState() {
        return state;
    }

    public Object readCurrentCampaigns() {
        try {
            currentCampaigns = marketingService.getCurrentCampaignsString();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            currentCampaigns = t.getMessage();
        }
        return null;
    }

    public String getCurrentCampaigns() {
        return currentCampaigns;
    }

    public Object readInsights() {
        try {
            insights = marketingService.getAdInsight(Long.parseLong(campaignId));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            insights = t.getMessage();
        }
        return null;
    }

    public String getInsights() {
        return insights;
    }

    public Object stopCampaigns() {
        try {
            marketingService.stopCampaigns(Long.parseLong(campaignId));
            state = "OK";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            state = t.getMessage();
        }
        return null;
    }

    public Object archiveCampaignAndHistorize() {
        try {
            marketingService.archiveCampaignAndHistorize(Long.parseLong(campaignId));
            state = "OK";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            state = t.getMessage();
        }
        return null;
    }

    public Object archiveCampaign() {
        try {
            marketingService.archiveCampaign(Long.parseLong(campaignId));
            state = "OK";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            state = t.getMessage();
        }
        return null;
    }
}
