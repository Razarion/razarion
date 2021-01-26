package com.btxtech.server.systemtests.editors;

import com.btxtech.server.JsonAssert;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.DependentParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.ParticleConfig;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.rest.ParticleEmitterSequenceEditorController;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;

public class ParticleEmitterSequenceEditorControllerTest extends AbstractCrudTest<ParticleEmitterSequenceEditorController, ParticleEmitterSequenceConfig> {

    public ParticleEmitterSequenceEditorControllerTest() {
        super(ParticleEmitterSequenceEditorController.class, ParticleEmitterSequenceConfig.class);
    }

    @Before
    public void fillTables() {
        setupAudios();
        setupParticleShapes();
    }

    @After
    public void cleanTables() {
        cleanTableNative("PARTICLE_EMITTER_SEQUENCE_AUDIO");
        cleanTableNative("PARTICLE_EMITTER_DEPENDENT");
        cleanTableNative("PARTICLE_EMITTER_AUTONOMOUS");
        cleanTable(ParticleEmitterSequenceEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.audioIds(Arrays.asList(AUDIO_1_ID, AUDIO_2_ID)));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.audioIds(Collections.singletonList(AUDIO_3_ID)));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.dependent(Collections.singletonList(
                new DependentParticleEmitterConfig().internalName("DependentParticleEmitterConfig1").particleConfig(
                        new ParticleConfig()))), new JsonAssert.IdSuppressor("/dependent/0", "id"));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.autonomous(Collections.singletonList(
                new AutonomousParticleEmitterConfig().internalName("AutonomousParticleEmitterConfig").particleConfig(
                        new ParticleConfig()))), new JsonAssert.IdSuppressor("/autonomous/0", "id"));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getDependent().get(0).emittingCount(100));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getDependent().get(0).getParticleConfig()
                .particleGrowTo(1.0).particleGrowFrom(2.0).particleShapeConfigId(PARTICLE_SHAPE_1_ID)
                .velocity(new Vertex(1, 2, 3)).acceleration(new Vertex(5, 6, 7)).velocityRandomPart(new Vertex(8, 2, 9)));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getDependent().get(0).getParticleConfig()
                .particleShapeConfigId(PARTICLE_SHAPE_1_ID)
                .velocity(null).acceleration(null).velocityRandomPart(new Vertex(8, 2, 9)).directedVelocity(11.0).directedAcceleration(12.0).directedVelocityRandomPart(34.0));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getAutonomous().get(0).timeToLive(112).startTime(1200).setVelocity(new Vertex(1, 2, 3)));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getAutonomous().add(
                new AutonomousParticleEmitterConfig().internalName("AutonomousParticleEmitterConfig").particleConfig(
                        new ParticleConfig())), new JsonAssert.IdSuppressor("/autonomous/1", "id"));
        registerUpdate(particleEmitterSequenceConfig -> particleEmitterSequenceConfig.getAutonomous().remove(0));
    }

}
