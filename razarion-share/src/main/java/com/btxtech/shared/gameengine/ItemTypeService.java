package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ItemTypeService {
    private final HashMap<Integer, BaseItemType> baseItemTypes = new HashMap<>();
    private final HashMap<Integer, ResourceItemType> resourceItemTypes = new HashMap<>();
    private final HashMap<Integer, BoxItemType> boxItemTypes = new HashMap<>();

    public void onGameEngineInit(@Observes GameEngineInitEvent engineInitEvent) {
        init(engineInitEvent.getGameEngineConfig());
    }

    public void init(GameEngineConfig gameEngineConfig) {
        setBaseItemTypes(gameEngineConfig.getBaseItemTypes());
        setResourceItemTypes(gameEngineConfig.getResourceItemTypes());
        setBoxItemTypes(gameEngineConfig.getBoxItemTypes());
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

    public BoxItemType getBoxItemType(int boxItemTypeId) {
        BoxItemType boxItemType = boxItemTypes.get(boxItemTypeId);
        if (boxItemType == null) {
            throw new NoSuchItemTypeException(BaseItemType.class, boxItemTypeId);
        }
        return boxItemType;
    }

    public Collection<BaseItemType> getBaseItemTypes() {
        return baseItemTypes.values();
    }

    public Collection<ResourceItemType> getResourceItemTypes() {
        return resourceItemTypes.values();
    }

    public Collection<BoxItemType> getBoxItemTypes() {
        return boxItemTypes.values();
    }

    public void overrideBaseItemType(BaseItemType baseItemType) {
        baseItemTypes.put(baseItemType.getId(), baseItemType);
    }

    public void overrideResourceItemType(ResourceItemType resourceItemType) {
        resourceItemTypes.put(resourceItemType.getId(), resourceItemType);
    }

    public void overrideBoxItemType(BoxItemType boxItemType) {
        boxItemTypes.put(boxItemType.getId(), boxItemType);
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

    public void setBoxItemTypes(List<BoxItemType> boxItemTypes) {
        this.boxItemTypes.clear();
        if (boxItemTypes != null) {
            for (BoxItemType boxItemType : boxItemTypes) {
                this.boxItemTypes.put(boxItemType.getId(), boxItemType);
            }
        }
    }

    public void deleteBaseItemType(BaseItemType baseItemType) {
        baseItemTypes.remove(baseItemType.getId());
    }

    public void deleteResourceItemType(ResourceItemType resourceItemType) {
        resourceItemTypes.remove(resourceItemType.getId());
    }

    public void deleteBoxItemType(BoxItemType boxItemType) {
        boxItemTypes.remove(boxItemType.getId());
    }

    // TODO public boolean areItemTypesLoaded()

    // TODO public List<BaseItemType> ableToBuild(BaseItemType toBeBuilt)

    // TODO public int getMaxItemRadius()

    // TODO public int getMaxItemDiameter()

}
