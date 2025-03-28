package com.btxtech.server.model.engine;


import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jakarta.persistence.*;

import static com.btxtech.server.service.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 04.10.2016.
 */
@Entity
@Table(name = "RESOURCE_ITEM_TYPE")
public class ResourceItemTypeEntity extends BaseEntity {
    private double radius;
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;
    private boolean fixVerticalNorm;
    private int amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity model3DEntity;


    public ResourceItemType toResourceItemType() {
        ResourceItemType resourceItemType = (ResourceItemType) new ResourceItemType()
                .setRadius(radius)
                .setAmount(amount)
                .setFixVerticalNorm(fixVerticalNorm)
                .setTerrainType(terrainType)
                .id(getId())
                .internalName(getInternalName())
                .model3DId(extractId(model3DEntity, Model3DEntity::getId));
        if (thumbnail != null) {
            resourceItemType.setThumbnail(thumbnail.getId());
        }
        return resourceItemType;
    }

    public void fromResourceItemType(ResourceItemType resourceItemType) {
        radius = resourceItemType.getRadius();
        fixVerticalNorm = resourceItemType.isFixVerticalNorm();
        terrainType = resourceItemType.getTerrainType();
        amount = resourceItemType.getAmount();
    }

    public void setThumbnail(ImageLibraryEntity thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setModel3DEntity(Model3DEntity model3DEntity) {
        this.model3DEntity = model3DEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceItemTypeEntity that = (ResourceItemTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
