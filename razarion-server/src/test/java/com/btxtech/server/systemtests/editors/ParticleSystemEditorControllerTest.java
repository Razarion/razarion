package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ParticleSystemEntity;
import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.rest.ParticleSystemEditorController;
import com.btxtech.shared.rest.ThreeJsModelPackEditorController;
import org.junit.After;

import java.util.Arrays;

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
