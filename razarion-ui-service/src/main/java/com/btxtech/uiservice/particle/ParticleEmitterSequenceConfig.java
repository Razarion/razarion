package com.btxtech.uiservice.particle;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

import java.util.List;

/**
 * Created by Beat
 * 06.02.2017.
 */
public class ParticleEmitterSequenceConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private List<DependentParticleEmitterConfig> dependent;
    private List<AutonomousParticleEmitterConfig> autonomous;
    private List<Integer> audioIds;

    public int getId() {
        return id;
    }

    public ParticleEmitterSequenceConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ParticleEmitterSequenceConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public List<DependentParticleEmitterConfig> getDependent() {
        return dependent;
    }

    public ParticleEmitterSequenceConfig setDependent(List<DependentParticleEmitterConfig> dependent) {
        this.dependent = dependent;
        return this;
    }

    public List<AutonomousParticleEmitterConfig> getAutonomous() {
        return autonomous;
    }

    public ParticleEmitterSequenceConfig setAutonomous(List<AutonomousParticleEmitterConfig> autonomous) {
        this.autonomous = autonomous;
        return this;
    }

    public List<Integer> getAudioIds() {
        return audioIds;
    }

    public ParticleEmitterSequenceConfig setAudioIds(List<Integer> audioIds) {
        this.audioIds = audioIds;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
