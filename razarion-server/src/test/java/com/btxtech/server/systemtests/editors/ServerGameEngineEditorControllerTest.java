package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.test.JsonAssert;
import org.junit.After;
import org.junit.Before;

import java.util.Collections;

public class ServerGameEngineEditorControllerTest extends AbstractCrudTest<ServerGameEngineEditorController, ServerGameEngineConfig> {
    public ServerGameEngineEditorControllerTest() {
        super(ServerGameEngineEditorController.class, ServerGameEngineConfig.class);
    }

    @Before
    public void fillTables() {
        setupPlanetDb();
    }

    @After
    public void cleanTables() {
        cleanTable(ServerGameEngineConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_1_ID));
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_2_ID).setResourceRegionConfigs(Collections.singletonList(
                new ResourceRegionConfig().region(new PlaceConfig().position(new DecimalPosition(1, 1)).radius(9.0)))), new JsonAssert.IdSuppressor("/resourceRegionConfigs", "id", true));

    }
}