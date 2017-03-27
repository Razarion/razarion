package com.btxtech.server.web;

import com.btxtech.server.marketing.Interest;
import com.btxtech.server.marketing.MarketingService;
import com.btxtech.server.marketing.facebook.AdInterest;
import com.btxtech.server.marketing.facebook.CreationData;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    private String queryInterest;
    private String interestResult;
    private String createAdInterest;

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

    public void setQueryInterest(String queryInterest) {
        this.queryInterest = queryInterest;
    }

    public String getQueryInterest() {
        return queryInterest;
    }

    public Object queryAdInterest() {
        try {
            List<AdInterest> adInterests = marketingService.queryAdInterest(queryInterest);
            if (adInterests.isEmpty()) {
                interestResult = "No interest for: " + queryInterest;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (AdInterest adInterest : adInterests) {
                    stringBuilder.append(adInterest.toNiceString());
                    stringBuilder.append("<br />");
                }
                interestResult = stringBuilder.toString();
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            interestResult = t.getMessage();
        }
        return null;
    }

    public String getInterestResult() {
        return interestResult;
    }

    public void setCreateCampaignInterest(String createCampaignInterest) {
        this.createAdInterest = createCampaignInterest;
    }

    public String getCreateCampaignInterest() {
        return createAdInterest;
    }

    public Object createCampaign() {
        try {
            List<Interest> interests = new ArrayList<>();
            for (String line : createAdInterest.split("\\n")) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, "|");
                Interest interest = new Interest();
                interest.setName(stringTokenizer.nextToken());
                interest.setId(stringTokenizer.nextToken());
                interests.add(interest);
            }
            CreationData creationData = marketingService.startCampaign(interests);
            state = "OK Campaign: " + creationData.getCampaignId() + " Ad Set: " + creationData.getAdSetId() + " Ad: " + creationData.getAdId();
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
