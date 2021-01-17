package com.btxtech.shared.datatypes.particle;

import com.btxtech.shared.dto.Config;

import java.util.List;

/**
 * Created by Beat
 * 06.02.2017.
 */
public class ParticleEmitterSequenceConfig implements Config {
    private int id;
    private String internalName;
    private List<DependentParticleEmitterConfig> dependent;
    private List<AutonomousParticleEmitterConfig> autonomous;
    private List<Integer> audioIds;

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

    public List<Integer> getAudioIds() {
        return audioIds;
    }

    public void setAudioIds(List<Integer> audioIds) {
        this.audioIds = audioIds;
    }

    public ParticleEmitterSequenceConfig id(int id) {
        this.id = id;
        return this;
    }

    public ParticleEmitterSequenceConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ParticleEmitterSequenceConfig dependent(List<DependentParticleEmitterConfig> dependent) {
        setDependent(dependent);
        return this;
    }

    public ParticleEmitterSequenceConfig autonomous(List<AutonomousParticleEmitterConfig> autonomous) {
        setAutonomous(autonomous);
        return this;
    }

    public ParticleEmitterSequenceConfig audioIds(List<Integer> audioIds) {
        setAudioIds(audioIds);
        return this;
    }
}
