package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ItemTypeService {
    private final HashMap<Integer, ItemType> itemBaseTypes = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        setBaseItemTypes(engineInitEvent.getGameEngineConfig().getBaseItemTypes());
    }

    public ItemType getItemType(int itemTypeId) throws NoSuchItemTypeException {
        ItemType itemType = itemBaseTypes.get(itemTypeId);
        if (itemType == null) {
            throw new NoSuchItemTypeException(itemTypeId);
        }
        return itemType;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) {
        return (BaseItemType) getItemType(baseItemTypeId);
    }

    public <T extends ItemType> Collection<T> getItemTypes(Class<T> filter) {
        Collection<T> result = new ArrayList<>();
        for (ItemType itemType : itemBaseTypes.values()) {
            if (filter == null || filter == itemType.getClass()) {
                result.add((T) itemType);
            }
        }
        return result;
    }

    public void override(BaseItemType baseItemType) {
        itemBaseTypes.put(baseItemType.getId(), baseItemType);
    }

    public void setBaseItemTypes(Collection<BaseItemType> itemTypes) {
        this.itemBaseTypes.clear();
        if (itemTypes != null) {
            for (ItemType itemType : itemTypes) {
                this.itemBaseTypes.put(itemType.getId(), itemType);
            }
        }
    }

    public void deleteBaseItemType(BaseItemType baseItemType) {
        itemBaseTypes.remove(baseItemType.getId());
    }

    // TODO public boolean areItemTypesLoaded()

    // TODO public List<BaseItemType> ableToBuild(BaseItemType toBeBuilt)

    // TODO public int getMaxItemRadius()

    // TODO public int getMaxItemDiameter()

}
