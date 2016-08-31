package com.btxtech.server.emulation;

import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class ItemTypeEmulation {
    public enum Id {
        SIMPLE_MOVABLE
    }

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    public List<ItemType> createItemTypes() {
        List<ItemType> itemTypes = new ArrayList<>();
        itemTypes.add(createSimpleMovable());
        return itemTypes;
    }

    public BaseItemType createSimpleMovable() {
        BaseItemType builder = (BaseItemType) new BaseItemType().setName("Builder Emulation").setId(Id.SIMPLE_MOVABLE.ordinal()).setTerrainType(TerrainType.LAND);
        builder.setShape3DId(1).setRadius(50);
        builder.setSpawnShape3DId(2).setSpawnDurationMillis(5000);
        return builder.setMovableType(new MovableType().setSpeed(10)).setHealth(100);
    }

}
