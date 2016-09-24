package com.btxtech.test;

import com.btxtech.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.planet.model.ItemLifecycle;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.Collections;

/**
 * Created by Beat
 * 23.09.2016.
 */
public class SimpleTestEnvironment {
    public static final BaseItemType SIMPLE_MOVABLE_ITEM_TYPE;
    private ItemTypeService itemTypeService;

    public SimpleTestEnvironment() {
        itemTypeService = new ItemTypeService();
        itemTypeService.setBaseItemTypes(Collections.singletonList(SIMPLE_MOVABLE_ITEM_TYPE));
    }

    public void injectItemTypeService(Object service) {
        injectItemTypeService("itemTypeService", service);
    }

    public void injectItemTypeService(String fieldName, Object service) {
        try {
            TestHelper.setPrivateField(service, fieldName, itemTypeService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SyncBaseItem createSimpleSyncBaseItem(PlayerBase playerBase) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        syncBaseItem.init(1, SIMPLE_MOVABLE_ITEM_TYPE, new SyncPhysicalMovable(syncBaseItem, new PhysicalAreaConfig().setAngularVelocity(Math.toDegrees(30)).setSpeed(80.0).setMinTurnSpeed(10.0).setAcceleration(100.0).setRadius(20), new Vertex(0, 0, 0), new Vertex(0, 0, 1), 0.0, new DecimalPosition(0, 0)));
        syncBaseItem.setup(playerBase, ItemLifecycle.ALIVE);
        return syncBaseItem;
    }

    public PlayerBase createHumanPlayerBase(UserContext userContext) {
        return new PlayerBase(1, userContext.getName(), Character.HUMAN, userContext);
    }

    static {
        BaseItemType simpleMovable = new BaseItemType();
        simpleMovable.setHealth(100).setSpawnDurationMillis(1000);
        simpleMovable.setId(1);
        simpleMovable.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(40.0).setSpeed(40.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(10));
        SIMPLE_MOVABLE_ITEM_TYPE = simpleMovable;
    }
}
