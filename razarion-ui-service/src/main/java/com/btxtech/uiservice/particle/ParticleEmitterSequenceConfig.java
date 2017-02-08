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

    public void setDependent(List<DependentParticleEmitterConfig> dependent) {
        this.dependent = dependent;
    }

    public List<AutonomousParticleEmitterConfig> getAutonomous() {
        return autonomous;
    }

    public void setAutonomous(List<AutonomousParticleEmitterConfig> autonomous) {
        this.autonomous = autonomous;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }
}
