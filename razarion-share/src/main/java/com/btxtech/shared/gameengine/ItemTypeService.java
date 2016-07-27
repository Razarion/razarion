package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ItemTypeService {
    private final HashMap<Integer, ItemType> itemTypes = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        itemTypes.clear();
        for (ItemType itemType : engineInitEvent.getGameEngineConfig().getItemTypes()) {
            itemTypes.put(itemType.getId(), itemType);
        }
    }

    public ItemType getItemType(int itemTypeId) throws NoSuchItemTypeException {
        ItemType itemType = itemTypes.get(itemTypeId);
        if (itemType == null) {
            throw new NoSuchItemTypeException(itemTypeId);
        }
        return itemType;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) throws NoSuchItemTypeException {
        return (BaseItemType) getItemType(baseItemTypeId);
    }

    public <T extends ItemType> Collection<T> getItemTypes(Class<T> filter) {
        Collection<T> result = new ArrayList<>();
        for (ItemType itemType : itemTypes.values()) {
            if(filter == null || filter == itemType.getClass()) {
                result.add((T)itemType);
            }
        }
        return result;
    }

    // TODO public boolean areItemTypesLoaded()

    // TODO public List<BaseItemType> ableToBuild(BaseItemType toBeBuilt)

    // TODO public int getMaxItemRadius()

    // TODO public int getMaxItemDiameter()

}
