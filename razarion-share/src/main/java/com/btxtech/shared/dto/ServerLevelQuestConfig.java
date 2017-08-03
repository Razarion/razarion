package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 03.08.2017.
 */
public class ServerLevelQuestConfig {
    private int Id;
    private String internalName;
    private Integer minimalLevelId;

    public int getId() {
        return Id;
    }

    public ServerLevelQuestConfig setId(int id) {
        Id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ServerLevelQuestConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public ServerLevelQuestConfig setMinimalLevelId(Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
        return this;
    }
}
