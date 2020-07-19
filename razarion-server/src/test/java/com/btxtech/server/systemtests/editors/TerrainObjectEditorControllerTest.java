package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import org.junit.After;
import org.junit.Before;

public class TerrainObjectEditorControllerTest extends AbstractCrudTest<TerrainObjectEditorController, TerrainObjectConfig> {
    public TerrainObjectEditorControllerTest() {
        super(TerrainObjectEditorController.class, TerrainObjectConfig.class);
    }

    @Before
    public void fillTables() {
        setupShape3dConfig();
    }

    @After
    public void cleanTables() {
        cleanTable(TerrainObjectEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(slopeConfig -> slopeConfig.shape3DId(SHAPE_3D_1_ID).radius(10));
        registerUpdate(slopeConfig -> slopeConfig.shape3DId(SHAPE_3D_2_ID).radius(2.2));
    }
}
