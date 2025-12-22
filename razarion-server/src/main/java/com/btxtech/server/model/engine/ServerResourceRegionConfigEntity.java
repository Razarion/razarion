package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.dto.ResourceRegionConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.model.engine.PlaceConfigEntity.toPlaceConfig;
import static com.btxtech.server.service.PersistenceUtil.extractId;
import static com.btxtech.server.service.PersistenceUtil.fromConfig;

@Entity
@Table(name = "SERVER_RESOURCE_REGION_CONFIG")
public class ServerResourceRegionConfigEntity extends BaseEntity {
    private int count;
    private double minDistanceToItems;
    @OneToOne(fetch = FetchType.LAZY)
    private ResourceItemTypeEntity resourceItemType;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity region;

    public ResourceRegionConfig toResourceRegionConfig() {
        return new ResourceRegionConfig()
                .id(getId())
                .internalName(getInternalName())
                .count(count)
                .minDistanceToItems(minDistanceToItems)
                .region(toPlaceConfig(region))
                .resourceItemTypeId(extractId(resourceItemType, ResourceItemTypeEntity::getId));
    }

    public ServerResourceRegionConfigEntity fromResourceRegionConfig(ResourceRegionConfig resourceRegionConfig) {
        setInternalName(resourceRegionConfig.getInternalName());
        count = resourceRegionConfig.getCount();
        minDistanceToItems = resourceRegionConfig.getMinDistanceToItems();
        if (resourceRegionConfig.getResourceItemTypeId() != null) {
            resourceItemType = (ResourceItemTypeEntity) new ResourceItemTypeEntity().id(resourceRegionConfig.getResourceItemTypeId());
        } else {
            resourceItemType = null;
        }
        region = fromConfig(region, resourceRegionConfig.getRegion(), PlaceConfigEntity::new, PlaceConfigEntity::fromPlaceConfig);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerResourceRegionConfigEntity that = (ServerResourceRegionConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId() : System.identityHashCode(this);
    }
}
