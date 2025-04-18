package com.btxtech.shared.dto;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2017.
 */
public class MasterPlanetConfig {
    private List<ResourceRegionConfig> resourceRegionConfigs;

    public List<ResourceRegionConfig> getResourceRegionConfigs() {
        return resourceRegionConfigs;
    }

    public MasterPlanetConfig setResourceRegionConfigs(List<ResourceRegionConfig> resourceRegionConfigs) {
        this.resourceRegionConfigs = resourceRegionConfigs;
        return this;
    }

}
