package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public interface GameTestContent {
    int GROUND_SKELETON_ID = 1;

    static StaticGameConfig setupStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundSkeletonConfig(new GroundSkeletonConfig().setId(GROUND_SKELETON_ID).setHeights(new double[][]{{0.0}}).setHeightXCount(1).setHeightYCount(1));
        staticGameConfig.setLevelConfigs(BaseItemServiceBase.setupLevelConfigs());
        staticGameConfig.setBaseItemTypes(BaseItemServiceBase.setupBaseItemType());
        return staticGameConfig;
    }

    static PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setItemTypeLimitation(BaseItemServiceBase.setupItemTypeLimitations());
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1000, 1000));
        planetConfig.setPlayGround(new Rectangle2D(0, 0, 6, 6));
        planetConfig.setStartBaseItemTypeId(BaseItemServiceBase.BUILDER_ITEM_TYPE_ID);
        return planetConfig;
    }

}
