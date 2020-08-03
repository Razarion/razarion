package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.rest.DrivewayEditorController;
import org.junit.After;

public class DrivewayEditorControllerTest extends AbstractCrudTest<DrivewayEditorController, DrivewayConfig> {
    public DrivewayEditorControllerTest() {
        super(DrivewayEditorController.class, DrivewayConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(DrivewayConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(drivewayConfig -> drivewayConfig.angle(0.55));
        registerUpdate(drivewayConfig -> drivewayConfig.angle(1.83));
    }
}
