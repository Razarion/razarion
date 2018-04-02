package com.btxtech.server.mgmt;

/**
 * Created by Beat
 * on 06.09.2017.
 */
public class QuestBackendInfo {
    private int id;
    private String internalName;
    private Integer levelNumber;
    private Integer levelId;

    public int getId() {
        return id;
    }

    public QuestBackendInfo setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public QuestBackendInfo setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public QuestBackendInfo setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
        return this;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public QuestBackendInfo setLevelId(Integer levelId) {
        this.levelId = levelId;
        return this;
    }
}
