package com.btxtech.server.marketing;

import com.btxtech.server.marketing.restdatatypes.AdInterestJson;

import javax.persistence.Embeddable;

/**
 * Created by Beat
 * 20.03.2017.
 */
@Embeddable
public class Interest {
    private String fbId; // Can not be called id: join does not work. See MarketingService
    private String name;
    private Long audienceSize;

    public Interest() {
    }

    public Interest(Interest interest) {
        fbId = interest.getFbId();
        name = interest.getName();
        audienceSize = interest.getAudienceSize();
    }

    public String getFbId() {
        return fbId;
    }

    public Interest setFbId(String id) {
        this.fbId = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Interest setName(String name) {
        this.name = name;
        return this;
    }

    public Long getAudienceSize() {
        return audienceSize;
    }

    public void setAudienceSize(Long audienceSize) {
        this.audienceSize = audienceSize;
    }

    public AdInterestJson generateAdInterestJson() {
        AdInterestJson adInterestJson = new AdInterestJson().setId(fbId).setName(getName());
        if(audienceSize != null) {
            adInterestJson.setAudienceSize(audienceSize.intValue());
        }
        return adInterestJson;
    }

    @Override
    public String toString() {
        return "Interest{" +
                "id='" + fbId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
