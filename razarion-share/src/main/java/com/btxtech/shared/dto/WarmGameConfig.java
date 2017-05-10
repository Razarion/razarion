package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

/**
 * Created by Beat
 * 08.05.2017.
 */
public class WarmGameConfig {
    private SlavePlanetConfig slavePlanetConfig;
    private SlaveSyncItemInfo slaveSyncItemInfo;
    private PlanetConfig planetConfig;

    public SlavePlanetConfig getSlavePlanetConfig() {
        return slavePlanetConfig;
    }

    public WarmGameConfig setSlavePlanetConfig(SlavePlanetConfig slavePlanetConfig) {
        this.slavePlanetConfig = slavePlanetConfig;
        return this;
    }

    public SlaveSyncItemInfo getSlaveSyncItemInfo() {
        return slaveSyncItemInfo;
    }

    public WarmGameConfig setSlaveSyncItemInfo(SlaveSyncItemInfo slaveSyncItemInfo) {
        this.slaveSyncItemInfo = slaveSyncItemInfo;
        return this;
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public WarmGameConfig setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        return this;
    }
}
