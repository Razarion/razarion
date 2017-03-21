package com.btxtech.server.web;

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
    private String currentAds;
    private String insights;
    private String adSetId;
    private String state;

    public String getAdSetId() {
        return adSetId;
    }

    public void setAdSetId(String adSetId) {
        this.adSetId = adSetId;
    }

    public String getState() {
        return state;
    }

    public Object readCurrentAds() {
        try {
            currentAds = marketingService.getCurrentAdAsString();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            currentAds = t.getMessage();
        }
        return null;
    }

    public Object getCurrentAds() {
        return currentAds;
    }

    public Object readInsights() {
        try {
            insights = marketingService.getAdInsight(Long.parseLong(adSetId));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            insights = t.getMessage();
        }
        return null;
    }

    public String getInsights() {
        return insights;
    }

    public Object stopAd() {
        try {
            marketingService.stopAd(Long.parseLong(adSetId));
            state = "OK";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            state = t.getMessage();
        }
        return null;
    }

    public Object deleteAdAndHistorize() {
        try {
            marketingService.deleteAdAndHistorize(Long.parseLong(adSetId));
            state = "OK";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            state = t.getMessage();
        }
        return null;
    }
}
