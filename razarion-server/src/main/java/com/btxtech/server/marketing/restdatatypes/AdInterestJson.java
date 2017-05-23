package com.btxtech.server.marketing.restdatatypes;

/**
 * Created by Beat
 * 30.04.2017.
 */
public class AdInterestJson {
    private String id;
    private String name;
    private Integer audienceSize;

    public String getId() {
        return id;
    }

    public AdInterestJson setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AdInterestJson setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAudienceSize() {
        return audienceSize;
    }

    public AdInterestJson setAudienceSize(Integer audienceSize) {
        this.audienceSize = audienceSize;
        return this;
    }
}
