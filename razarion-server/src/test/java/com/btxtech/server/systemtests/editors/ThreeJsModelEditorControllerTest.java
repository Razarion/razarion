package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.rest.ThreeJsModelEditorController;
import org.junit.After;

public class ThreeJsModelEditorControllerTest extends AbstractCrudTest<ThreeJsModelEditorController, ThreeJsModelConfig> {
    public ThreeJsModelEditorControllerTest() {
        super(ThreeJsModelEditorController.class, ThreeJsModelConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(ThreeJsModelConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
    }
}
