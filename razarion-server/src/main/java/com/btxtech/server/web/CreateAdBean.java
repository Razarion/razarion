package com.btxtech.server.web;

import com.btxtech.server.marketing.Interest;
import com.btxtech.server.marketing.MarketingService;
import com.btxtech.server.marketing.facebook.AdInterest;
import com.btxtech.server.marketing.facebook.CreationData;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 27.03.2017.
 */
@Named
@SessionScoped
public class CreateAdBean implements Serializable {
    @Inject
    private MarketingService marketingService;
    @Inject
    transient  private ExceptionHandler exceptionHandler;
    private List<AdInterest> selectedAdInterest = new ArrayList<>();
    private List<AdInterest> availableAdInterest = new ArrayList<>();
    private String interestQuery;
    private String campaignCreationError;

    public List<AdInterest> getSelectedAdInterest() {
        return selectedAdInterest;
    }

    public void setSelectedAdInterest(List<AdInterest> selectedAdInterest) {
        this.selectedAdInterest = selectedAdInterest;
    }

    public String getInterestQuery() {
        return interestQuery;
    }

    public void setInterestQuery(String interestQuery) {
        this.interestQuery = interestQuery;
    }

    public List<AdInterest> getAvailableAdInterest() {
        return availableAdInterest;
    }

    public Object queryInterest() {
        availableAdInterest = marketingService.queryAdInterest(interestQuery);
        return null;
    }

    public void addInterest(AdInterest adInterest) {
        if (!selectedAdInterest.contains(adInterest)) {
            selectedAdInterest.add(adInterest);
        }
    }

    public void removeInterest(AdInterest adInterest) {
        selectedAdInterest.remove(adInterest);
    }

    public Object deepQueryInterest() {
        List<AdInterest> deepInterests = new ArrayList<>(availableAdInterest);
        for (AdInterest adInterest : availableAdInterest) {
            for (AdInterest interest : marketingService.queryAdInterest(adInterest.getName())) {
                if (!deepInterests.contains(interest)) {
                    deepInterests.add(interest);
                }
            }
        }
        availableAdInterest = deepInterests;
        return null;
    }


    public Object createCampaign() {
        if (selectedAdInterest.isEmpty()) {
            campaignCreationError = "No interests";
            return null;
        }

        try {
            List<Interest> interests = new ArrayList<>();
            for (AdInterest selected : selectedAdInterest) {
                Interest interest = new Interest();
                interest.setName(selected.getName());
                interest.setId(selected.getId());
                interests.add(interest);
            }
            marketingService.startCampaign(interests);
            return "marketing";
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            campaignCreationError = t.getMessage();
            return null;
        }
    }

    public String getCampaignCreationError() {
        return campaignCreationError;
    }
}
