package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.rest.SlopeEditorController;
import org.junit.After;
import org.junit.Before;

public class SlopeEditorControllerTest extends AbstractCrudTest<SlopeEditorController, SlopeConfig> {
    public SlopeEditorControllerTest() {
        super(SlopeEditorController.class, SlopeConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTable(SlopeConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        // registerUpdate(groundConfig -> groundConfig.setSplatting(null));
    }
}
