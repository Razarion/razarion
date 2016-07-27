package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpawnItemType;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class ItemTypeEmulation {
    public enum Id {
        SPAWN_BASE_ITEM_TYPE,
        SIMPLE_MOVABLE
    }

    public List<ItemType> createItemTypes() {
        List<ItemType> itemTypes = new ArrayList<>();
        SpawnItemType spawnItemType = createSpawnItemType();
        itemTypes.add(spawnItemType);
        itemTypes.add(createSimpleMovable(spawnItemType));
        return itemTypes;
    }

    public BaseItemType createSimpleMovable(SpawnItemType spawnItemType) {
        BaseItemType builder = (BaseItemType) new BaseItemType().setName("Builder Emulation").setId(Id.SIMPLE_MOVABLE.ordinal()).setTerrainType(TerrainType.LAND);
        builder.setVertexContainer(new VertexContainer().setVertices(Arrays.asList(new Vertex(0, 0, 0), new Vertex(20, 0, 0), new Vertex(20, 20, 0))));
        return builder.setMovableType(new MovableType().setSpeed(10)).setHealth(100).setSpawnItemType(spawnItemType).setRadius(50);
    }

    public SpawnItemType createSpawnItemType() {
        SpawnItemType spawnItemType = new SpawnItemType().setDuration(3);
        spawnItemType.setName("Spawn Base Item Type").setId(Id.SPAWN_BASE_ITEM_TYPE.ordinal());
        spawnItemType.setVertexContainer(new VertexContainer().setVertices(Arrays.asList(new Vertex(0, 0, 0), new Vertex(40, 0, 0), new Vertex(40, 40, 0))));
        return spawnItemType;
    }

}
