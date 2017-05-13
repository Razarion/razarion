package com.btxtech.server.persistence;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 13.05.2017.
 */
public class StaticGameConfigPersistenceTest extends ArquillianBaseTest {
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;

    @Test
    public void loadStaticGameConfig() throws Exception {
        setupPlanets();

        StaticGameConfig staticGameConfig = staticGameConfigPersistence.loadStaticGameConfig();
        Assert.assertNotNull(staticGameConfig.getGroundSkeletonConfig());
        Assert.assertNotNull(staticGameConfig.getSlopeSkeletonConfigs());
        Assert.assertNotNull(staticGameConfig.getTerrainObjectConfigs());
        Assert.assertEquals(-0.7, staticGameConfig.getWaterLevel(), 0.0001);
        Assert.assertNotNull(staticGameConfig.getBaseItemTypes());
        Assert.assertNotNull(staticGameConfig.getResourceItemTypes());
        Assert.assertNotNull(staticGameConfig.getBoxItemTypes());
        Assert.assertNotNull(staticGameConfig.getLevelConfigs());
        Assert.assertNotNull(staticGameConfig.getInventoryItems());

        cleanPlanets();
    }

}