package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
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

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "SERVER_RESOURCE_REGION_CONFIG")
public class ServerResourceRegionConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private int count;
    private double minDistanceToItems;
    @OneToOne
    private ResourceItemTypeEntity resourceItemType;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity region;

    public ResourceRegionConfig toResourceRegionConfig() {
        return new ResourceRegionConfig().setCount(count).setMinDistanceToItems(minDistanceToItems).setRegion(region.toPlaceConfig()).setResourceItemTypeId(resourceItemType.getId());
    }

    public void fromResourceRegionConfig(ItemTypePersistence itemTypePersistence, ResourceRegionConfig resourceRegionConfig) {
        count = resourceRegionConfig.getCount();
        minDistanceToItems = resourceRegionConfig.getMinDistanceToItems();
        resourceItemType = itemTypePersistence.readResourceItemTypeEntity(resourceRegionConfig.getResourceItemTypeId());
        region = new PlaceConfigEntity();
        region.fromPlaceConfig(resourceRegionConfig.getRegion());
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
