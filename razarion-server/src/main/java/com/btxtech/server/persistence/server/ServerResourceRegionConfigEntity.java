package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.dto.ResourceRegionConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;
import static com.btxtech.server.persistence.PersistenceUtil.fromConfig;
import static com.btxtech.server.persistence.PlaceConfigEntity.toPlaceConfig;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "SERVER_RESOURCE_REGION_CONFIG")
public class ServerResourceRegionConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private int count;
    private double minDistanceToItems;
    @OneToOne(fetch = FetchType.LAZY)
    private ResourceItemTypeEntity resourceItemType;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity region;

    public Integer getId() {
        return id;
    }

    public ResourceRegionConfig toResourceRegionConfig() {
        return new ResourceRegionConfig()
                .id(id)
                .internalName(internalName)
                .count(count)
                .minDistanceToItems(minDistanceToItems)
                .region(toPlaceConfig(region))
                .resourceItemTypeId(extractId(resourceItemType, ResourceItemTypeEntity::getId));
    }

    public void fromResourceRegionConfig(ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence, ResourceRegionConfig resourceRegionConfig) {
        internalName = resourceRegionConfig.getInternalName();
        count = resourceRegionConfig.getCount();
        minDistanceToItems = resourceRegionConfig.getMinDistanceToItems();
        resourceItemType = resourceItemTypeCrudPersistence.getEntity(resourceRegionConfig.getResourceItemTypeId());
        region = fromConfig(region, resourceRegionConfig.getRegion(), PlaceConfigEntity::new, PlaceConfigEntity::fromPlaceConfig);
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
