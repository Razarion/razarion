package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.BoxItemTypeCrudPersistence;
import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

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
 * on 16.09.2017.
 */
@Entity
@Table(name = "SERVER_BOX_REGION_CONFIG")
public class ServerBoxRegionConfigEntity implements ObjectNameIdProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToOne(fetch = FetchType.LAZY)
    private BoxItemTypeEntity boxItemTypeId;
    private int minInterval; // seconds
    private int maxInterval; // seconds
    private int count;
    private double minDistanceToItems;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity region;

    public Integer getId() {
        return id;
    }

    public BoxRegionConfig toBoxRegionConfig() {
        BoxRegionConfig boxRegionConfig = new BoxRegionConfig().id(id).internalName(internalName).count(count).minInterval(minInterval).maxInterval(maxInterval).minDistanceToItems(minDistanceToItems);
        if (boxItemTypeId != null) {
            boxRegionConfig.boxItemTypeId(boxItemTypeId.getId());
        }
        if (region != null) {
            boxRegionConfig.region(region.toPlaceConfig());
        }
        return boxRegionConfig;
    }

    public void fromBoxRegionConfig(BoxItemTypeCrudPersistence boxItemTypeCrudPersistence, BoxRegionConfig boxRegionConfig) {
        internalName = boxRegionConfig.getInternalName();
        boxItemTypeId = boxItemTypeCrudPersistence.getEntity(boxRegionConfig.getBoxItemTypeId());
        minInterval = boxRegionConfig.getMinInterval();
        maxInterval = boxRegionConfig.getMaxInterval();
        count = boxRegionConfig.getCount();
        minDistanceToItems = boxRegionConfig.getMinDistanceToItems();
        if (region != null && boxRegionConfig.getRegion() != null) {
            region.fromPlaceConfig(boxRegionConfig.getRegion());
        } else if (region != null && boxRegionConfig.getRegion() == null) {
            region = null;
        } else if (region == null && boxRegionConfig.getRegion() != null) {
            region = new PlaceConfigEntity();
            region.fromPlaceConfig(boxRegionConfig.getRegion());
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

        ServerBoxRegionConfigEntity that = (ServerBoxRegionConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
