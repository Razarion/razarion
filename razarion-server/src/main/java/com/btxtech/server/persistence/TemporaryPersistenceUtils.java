package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 18.04.2017.
 */
public class TemporaryPersistenceUtils {
    public static void completePlanetConfigTutorial(PlanetConfig planetConfig) {
        planetConfig.setHouseSpace(10);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER, 5);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 1);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY, 1);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
        planetConfig.setPlayGround(new Rectangle2D(50, 40, 310, 320));
        planetConfig.setWaterLevel(-0.7);
        planetConfig.setStartRazarion(550);
    }

    public static void completePlanetConfigMultiPlayer(PlanetConfig planetConfig) {
        planetConfig.setHouseSpace(10);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER, 20);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 3);
        itemTypeLimitation.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY, 1);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 2500, 2500));
        planetConfig.setPlayGround(new Rectangle2D(50, 40, 19900, 19920));
        planetConfig.setWaterLevel(-0.7);
        planetConfig.setStartRazarion(550);
    }

}
