package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import org.junit.After;

public class TerrainObjectEditorControllerTest extends AbstractCrudTest<TerrainObjectEditorController, TerrainObjectConfig> {
    public TerrainObjectEditorControllerTest() {
        super(TerrainObjectEditorController.class, TerrainObjectConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(TerrainObjectEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(slopeConfig -> slopeConfig.model3DId(-9999987).radius(10)); // TODO remove
        registerUpdate(slopeConfig -> slopeConfig.model3DId(-1199987).radius(2.2)); // TODO remove
    }
}
