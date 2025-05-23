package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 29.08.2017.
 */
public class ResourceServiceTest extends DaggerMasterBaseTest {
    private List<ResourceRegionConfig> resourceRegionConfigs;

    @Override
    protected MasterPlanetConfig setupMasterPlanetConfig() {
        resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().resourceItemTypeId(FallbackConfig.RESOURCE_ITEM_TYPE_ID).count(5).minDistanceToItems(1).region(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 20, 20))));
        return super.setupMasterPlanetConfig().setResourceRegionConfigs(resourceRegionConfigs);
    }

    @Test
    public void test() {
        setupMasterEnvironment();
        getResourceService().startResourceRegions();
        Assert.assertEquals(5, getTestGameLogicListener().getResourceCreated().size());
        Assert.assertEquals(0, getTestGameLogicListener().getResourceDeleted().size());
        getTestGameLogicListener().clearAll();
        getWeldBean(ResourceService.class).reloadResourceRegions(resourceRegionConfigs);
        Assert.assertEquals(5, getTestGameLogicListener().getResourceCreated().size());
        Assert.assertEquals(5, getTestGameLogicListener().getResourceDeleted().size());
    }

}