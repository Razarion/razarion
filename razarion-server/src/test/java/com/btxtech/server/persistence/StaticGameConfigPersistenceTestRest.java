package com.btxtech.server.persistence;

import com.btxtech.server.RestServerTestBase;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 13.05.2017.
 */
public class StaticGameConfigPersistenceTestRest extends RestServerTestBase {
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;

    @Test
    public void loadStaticGameConfig() throws Exception {
        setupPlanets();

        StaticGameConfig staticGameConfig = staticGameConfigPersistence.loadStaticGameConfig();
        Assert.assertNotNull(staticGameConfig.getGroundSkeletonConfig());
        Assert.assertNotNull(staticGameConfig.getSlopeConfigs());
        Assert.assertNotNull(staticGameConfig.getTerrainObjectConfigs());
        Assert.assertNotNull(staticGameConfig.getWaterConfig());
        Assert.assertNotNull(staticGameConfig.getBaseItemTypes());
        Assert.assertNotNull(staticGameConfig.getResourceItemTypes());
        Assert.assertNotNull(staticGameConfig.getBoxItemTypes());
        Assert.assertNotNull(staticGameConfig.getLevelConfigs());
        Assert.assertNotNull(staticGameConfig.getInventoryItems());

        cleanPlanets();
    }

}