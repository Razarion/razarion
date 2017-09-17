package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
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
public class ServerResourceRegionConfigEntity implements ObjectNameIdProvider {
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
        ResourceRegionConfig resourceRegionConfig = new ResourceRegionConfig().setId(id).setInternalName(internalName).setCount(count).setMinDistanceToItems(minDistanceToItems);
        if (region != null) {
            resourceRegionConfig.setRegion(region.toPlaceConfig());
        }
        if (resourceItemType != null) {
            resourceRegionConfig.setResourceItemTypeId(resourceItemType.getId());
        }
        return resourceRegionConfig;
    }

    public void fromResourceRegionConfig(ItemTypePersistence itemTypePersistence, ResourceRegionConfig resourceRegionConfig) {
        internalName = resourceRegionConfig.getInternalName();
        count = resourceRegionConfig.getCount();
        minDistanceToItems = resourceRegionConfig.getMinDistanceToItems();
        resourceItemType = itemTypePersistence.readResourceItemTypeEntity(resourceRegionConfig.getResourceItemTypeId());
        if(region != null && resourceRegionConfig.getRegion() != null) {
            region.fromPlaceConfig(resourceRegionConfig.getRegion());
        } else if(region != null && resourceRegionConfig.getRegion() == null) {
            region = null;
        } else if(region == null && resourceRegionConfig.getRegion() != null) {
            region = new PlaceConfigEntity();
            region.fromPlaceConfig(resourceRegionConfig.getRegion());
        }
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
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
