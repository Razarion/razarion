package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Beat
 * 15.07.2016.
 */
@JsType
@Singleton
public class ItemTypeService {
    private final HashMap<Integer, BaseItemType> baseItemTypes = new HashMap<>();
    private final HashMap<Integer, ResourceItemType> resourceItemTypes = new HashMap<>();
    private final HashMap<Integer, BoxItemType> boxItemTypes = new HashMap<>();
    private double maxRadius;
    private double maxVelocity;

    @Inject
    public ItemTypeService(InitializeService initializeService) {
        initializeService.receiveStaticGameConfig(this::init);
    }

    public void init(StaticGameConfig staticGameConfig) {
        setBaseItemTypes(staticGameConfig.getBaseItemTypes());
        setResourceItemTypes(staticGameConfig.getResourceItemTypes());
        setBoxItemTypes(staticGameConfig.getBoxItemTypes());
        maxRadius = -1.0;
        maxRadius = Math.max(maxRadius, baseItemTypes.values().stream().map(baseItemType -> baseItemType.getPhysicalAreaConfig().getRadius()).max(Comparator.naturalOrder()).orElse(-1.0));
        maxRadius = Math.max(maxRadius, resourceItemTypes.values().stream().map(ResourceItemType::getRadius).max(Comparator.naturalOrder()).orElse(-1.0));
        maxRadius = Math.max(maxRadius, boxItemTypes.values().stream().map(BoxItemType::getRadius).max(Comparator.naturalOrder()).orElse(-1.0));
        maxVelocity = baseItemTypes.values().stream().filter(baseItemType -> baseItemType.getPhysicalAreaConfig().fulfilledMovable()).map(baseItemType -> baseItemType.getPhysicalAreaConfig().getSpeed()).max(Comparator.naturalOrder()).orElse(-1.0);
    }

    public ResourceItemType getResourceItemType(Integer resourceItemTypeId) {
        ResourceItemType resourceItemType = resourceItemTypes.get(resourceItemTypeId);
        if (resourceItemType == null) {
            throw new NoSuchItemTypeException(ResourceItemType.class, resourceItemTypeId);
        }
        return resourceItemType;
    }

    @SuppressWarnings("unused") // Called by Angular
    public ResourceItemType getResourceItemTypeAngular(int resourceItemTypeId) {
        return getResourceItemType(resourceItemTypeId);
    }


    public BaseItemType getBaseItemType(Integer baseItemTypeId) {
        BaseItemType baseItemType = baseItemTypes.get(baseItemTypeId);
        if (baseItemType == null) {
            throw new NoSuchItemTypeException(BaseItemType.class, baseItemTypeId);
        }
        return baseItemType;
    }

    @SuppressWarnings("unused") // Called by Angular
    public BaseItemType getBaseItemTypeAngular(int baseItemTypeId) {
        return getBaseItemType(baseItemTypeId);
    }

    public BoxItemType getBoxItemType(Integer boxItemTypeId) {
        BoxItemType boxItemType = boxItemTypes.get(boxItemTypeId);
        if (boxItemType == null) {
            throw new NoSuchItemTypeException(BoxItemType.class, boxItemTypeId);
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

    public double getMaxRadius() {
        return maxRadius;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }
}
