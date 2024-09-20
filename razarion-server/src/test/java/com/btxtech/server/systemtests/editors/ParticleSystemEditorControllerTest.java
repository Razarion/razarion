package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ParticleSystemEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.rest.ParticleSystemEditorController;
import org.junit.After;

import static org.junit.Assert.fail;

public class ParticleSystemEditorControllerTest extends AbstractCrudTest<ParticleSystemEditorController, ParticleSystemConfig> {
    public ParticleSystemEditorControllerTest() {
        super(ParticleSystemEditorController.class, ParticleSystemConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(ParticleSystemEntity.class);
    }

    @Override
    protected void setupUpdate() {
        fail("ParticleSystemEditorControllerTest not testes");
    }

}
