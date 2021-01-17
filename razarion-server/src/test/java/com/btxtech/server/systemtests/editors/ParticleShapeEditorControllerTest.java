package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.particle.ParticleShapeEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.rest.ParticleShapeEditorController;
import org.junit.After;
import org.junit.Before;

public class ParticleShapeEditorControllerTest extends AbstractCrudTest<ParticleShapeEditorController, ParticleShapeConfig> {

    public ParticleShapeEditorControllerTest() {
        super(ParticleShapeEditorController.class, ParticleShapeConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTableNative("PARTICLE_SHAPE_COLOR_RAMP_OFFSET");
        cleanTable(ParticleShapeEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(particleShapeConfig -> particleShapeConfig.edgeLength(1.5).textureOffsetScope(0.25).colorRampXOffsets(new double[]{0.25, 0.5, 0.75}).shadowAlphaCutOff(0.75));
        registerUpdate(particleShapeConfig -> particleShapeConfig.colorRampImageId(IMAGE_1_ID).setAlphaOffsetImageId(IMAGE_2_ID));
        registerUpdate(particleShapeConfig -> particleShapeConfig.edgeLength(2.5).textureOffsetScope(0.35).colorRampXOffsets(new double[]{0.75}).shadowAlphaCutOff(0.1));
        registerUpdate(particleShapeConfig -> particleShapeConfig.colorRampImageId(IMAGE_2_ID).setAlphaOffsetImageId(IMAGE_3_ID));
    }

}
