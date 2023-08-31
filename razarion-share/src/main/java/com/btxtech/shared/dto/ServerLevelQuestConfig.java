package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.system.Nullable;

import java.util.List;

/**
 * Created by Beat
 * on 03.08.2017.
 */
public class ServerLevelQuestConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private Integer minimalLevelId;
    private List<QuestConfig> questConfigs;

    public int getId() {
        return id;
    }

    public ServerLevelQuestConfig id(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ServerLevelQuestConfig internalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public @Nullable Integer getMinimalLevelId() {
        return minimalLevelId;
    }

    public ServerLevelQuestConfig minimalLevelId(@Nullable Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
        return this;
    }

    public List<QuestConfig> getQuestConfigs() {
        return questConfigs;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setMinimalLevelId(Integer minimalLevelId) {
        this.minimalLevelId = minimalLevelId;
    }

    public void setQuestConfigs(List<QuestConfig> questConfigs) {
        this.questConfigs = questConfigs;
    }

    public ServerLevelQuestConfig questConfigs(List<QuestConfig> questConfigs) {
        this.questConfigs = questConfigs;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
