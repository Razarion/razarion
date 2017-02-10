package com.btxtech.shared.gameengine.datatypes.itemtype;

import java.util.List;

/**
 * Created by Beat
 * 20.12.2016.
 */
public class DemolitionStepEffect {
    private List<DemolitionParticleConfig> demolitionParticleConfigs;

    public List<DemolitionParticleConfig> getDemolitionParticleConfigs() {
        return demolitionParticleConfigs;
    }

    public DemolitionStepEffect setDemolitionParticleConfigs(List<DemolitionParticleConfig> demolitionParticleConfigs) {
        this.demolitionParticleConfigs = demolitionParticleConfigs;
        return this;
    }
}
