package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerResourceRegionConfigEntity;
import com.btxtech.server.persistence.server.StartRegionConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.test.JsonAssert;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;

public class ServerGameEngineEditorControllerTest extends AbstractCrudTest<ServerGameEngineEditorController, ServerGameEngineConfig> {
    public ServerGameEngineEditorControllerTest() {
        super(ServerGameEngineEditorController.class, ServerGameEngineConfig.class);
    }

    @Before
    public void fillTables() {
        setupPlanetDb();
        setupLevelDb();
    }

    @After
    public void cleanTables() {
        cleanTable(StartRegionConfigEntity.class);
        cleanTable(ServerResourceRegionConfigEntity.class);
        cleanTable(ServerGameEngineConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        JsonAssert.IdSuppressor[] idSuppressor = new JsonAssert.IdSuppressor[]{
                new JsonAssert.IdSuppressor("/resourceRegionConfigs", "id", true),
                new JsonAssert.IdSuppressor("/startRegionConfigs", "id", true)};
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_1_ID));
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.planetConfigId(PLANET_2_ID).setResourceRegionConfigs(Collections.singletonList(
                new ResourceRegionConfig().region(new PlaceConfig().position(new DecimalPosition(1, 1)).radius(9.0)))), idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.startRegionConfigs(Collections.singletonList(new StartRegionConfig().minimalLevelId(LEVEL_1_ID).internalName("xxxx").region(new Polygon2D(Arrays.asList(new DecimalPosition(1, 1),
                new DecimalPosition(2, 1),
                new DecimalPosition(2, 2)))))),
                idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().add(new StartRegionConfig().minimalLevelId(LEVEL_2_ID).internalName("yyy").region(new Polygon2D(Arrays.asList(new DecimalPosition(10, 10),
                new DecimalPosition(20, 10),
                new DecimalPosition(20, 20))))),
                idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().remove(1), idSuppressor);
        registerUpdate(serverGameEngineConfig -> serverGameEngineConfig.getStartRegionConfigs().remove(0), idSuppressor);
    }
}