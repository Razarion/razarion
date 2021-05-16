package com.btxtech.shared.dto;

import java.util.List;

public class ServerGameEngineConfig implements Config {
    private int id;
    private String internalName;
    private Integer planetConfigId;
    private List<ResourceRegionConfig> resourceRegionConfigs;

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

    public ServerGameEngineConfig id(int id) {
        this.id = id;
        return this;
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
}
