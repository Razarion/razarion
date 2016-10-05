package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ItemTypeService {
    private final HashMap<Integer, BaseItemType> baseItemTypes = new HashMap<>();
    private final HashMap<Integer, ResourceItemType> resourceItemTypes = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        setBaseItemTypes(engineInitEvent.getGameEngineConfig().getBaseItemTypes());
        setResourceItemTypes(engineInitEvent.getGameEngineConfig().getResourceItemTypes());
    }

    public ResourceItemType getResourceItemType(int resourceItemTypeId) {
        ResourceItemType resourceItemType = resourceItemTypes.get(resourceItemTypeId);
        if (resourceItemType == null) {
            throw new NoSuchItemTypeException(ResourceItemType.class, resourceItemTypeId);
        }
        return resourceItemType;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) {
        BaseItemType baseItemType = baseItemTypes.get(baseItemTypeId);
        if (baseItemType == null) {
            throw new NoSuchItemTypeException(BaseItemType.class, baseItemTypeId);
        }
        return baseItemType;
    }

    public Collection<BaseItemType> getBaseItemTypes() {
        return baseItemTypes.values();
    }

    public Collection<ResourceItemType> getResourceItemTypes() {
        return resourceItemTypes.values();
    }

    public void overrideBaseItemType(BaseItemType baseItemType) {
        baseItemTypes.put(baseItemType.getId(), baseItemType);
    }

    public void overrideResourceItemType(ResourceItemType resourceItemType) {
        resourceItemTypes.put(resourceItemType.getId(), resourceItemType);
    }

    public void setBaseItemTypes(Collection<BaseItemType> baseItemTypes) {
        this.baseItemTypes.clear();
        if (baseItemTypes != null) {
            for (BaseItemType baseItemType : baseItemTypes) {
                this.baseItemTypes.put(baseItemType.getId(), baseItemType);
            }
        }
    }

    public void setResourceItemTypes(Collection<ResourceItemType> resourceItemTypes) {
        this.resourceItemTypes.clear();
        if (resourceItemTypes != null) {
            for (ResourceItemType resourceItemType : resourceItemTypes) {
                this.resourceItemTypes.put(resourceItemType.getId(), resourceItemType);
            }
        }
    }

    public void deleteBaseItemType(BaseItemType baseItemType) {
        baseItemTypes.remove(baseItemType.getId());
    }

    public void deleteResourceItemType(ResourceItemType resourceItemType) {
        resourceItemTypes.remove(resourceItemType.getId());
    }

    // TODO public boolean areItemTypesLoaded()

    // TODO public List<BaseItemType> ableToBuild(BaseItemType toBeBuilt)

    // TODO public int getMaxItemRadius()

    // TODO public int getMaxItemDiameter()

}
