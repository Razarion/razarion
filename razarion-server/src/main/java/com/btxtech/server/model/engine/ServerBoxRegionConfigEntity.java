package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.BoxItemTypeCrudPersistence;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import jakarta.persistence.*;

@Entity
@Table(name = "SERVER_BOX_REGION_CONFIG")
public class ServerBoxRegionConfigEntity extends BaseEntity implements ObjectNameIdProvider {
    @OneToOne(fetch = FetchType.LAZY)
    private BoxItemTypeEntity boxItemTypeId;
    private int minInterval; // seconds
    private int maxInterval; // seconds
    private int count;
    private double minDistanceToItems;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity region;

    public BoxRegionConfig toBoxRegionConfig() {
        BoxRegionConfig boxRegionConfig = new BoxRegionConfig()
                .id(getId())
                .internalName(getInternalName())
                .count(count)
                .minInterval(minInterval)
                .maxInterval(maxInterval)
                .minDistanceToItems(minDistanceToItems);
        if (boxItemTypeId != null) {
            boxRegionConfig.boxItemTypeId(boxItemTypeId.getId());
        }
        if (region != null) {
            boxRegionConfig.region(region.toPlaceConfig());
        }
        return boxRegionConfig;
    }

    public void fromBoxRegionConfig(BoxItemTypeCrudPersistence boxItemTypeCrudPersistence, BoxRegionConfig boxRegionConfig) {
        setInternalName(boxRegionConfig.getInternalName());
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerBoxRegionConfigEntity that = (ServerBoxRegionConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(getId(), getInternalName());
    }
}
