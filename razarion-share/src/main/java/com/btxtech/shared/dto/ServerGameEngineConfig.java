package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import java.util.List;

public class ServerGameEngineConfig implements Config {
    private int id;
    private String internalName;
    private Integer planetConfigId;
    private List<ResourceRegionConfig> resourceRegionConfigs;
    private List<StartRegionConfig> startRegionConfigs;
    private List<BotConfig> botConfigs;
    private List<ServerLevelQuestConfig> serverLevelQuestConfigs;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getPlanetConfigId() {
        return planetConfigId;
    }

    public void setPlanetConfigId(Integer planetConfigId) {
        this.planetConfigId = planetConfigId;
    }

    public List<ResourceRegionConfig> getResourceRegionConfigs() {
        return resourceRegionConfigs;
    }

    public void setResourceRegionConfigs(List<ResourceRegionConfig> resourceRegionConfigs) {
        this.resourceRegionConfigs = resourceRegionConfigs;
    }

    public List<StartRegionConfig> getStartRegionConfigs() {
        return startRegionConfigs;
    }

    public void setStartRegionConfigs(List<StartRegionConfig> startRegionConfigs) {
        this.startRegionConfigs = startRegionConfigs;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public void setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
    }

    public ServerGameEngineConfig id(int id) {
        this.id = id;
        return this;
    }

    public List<ServerLevelQuestConfig> getServerLevelQuestConfigs() {
        return serverLevelQuestConfigs;
    }

    public void setServerLevelQuestConfigs(List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        this.serverLevelQuestConfigs = serverLevelQuestConfigs;
    }

    public ServerGameEngineConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ServerGameEngineConfig planetConfigId(Integer planetConfigId) {
        setPlanetConfigId(planetConfigId);
        return this;
    }

    public ServerGameEngineConfig resourceRegionConfigs(List<ResourceRegionConfig> resourceRegionConfigs) {
        setResourceRegionConfigs(resourceRegionConfigs);
        return this;
    }

    public ServerGameEngineConfig startRegionConfigs(List<StartRegionConfig> startRegionConfigs) {
        setStartRegionConfigs(startRegionConfigs);
        return this;
    }

    public ServerGameEngineConfig botConfigs(List<BotConfig> botConfigs) {
        setBotConfigs(botConfigs);
        return this;
    }

    public ServerGameEngineConfig serverLevelQuestConfig(List<ServerLevelQuestConfig> serverLevelQuestConfigs) {
        setServerLevelQuestConfigs(serverLevelQuestConfigs);
        return this;
    }
}
