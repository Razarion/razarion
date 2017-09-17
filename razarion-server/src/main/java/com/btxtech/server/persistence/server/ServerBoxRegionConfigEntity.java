package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.shared.dto.BoxRegionConfig;

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
public class ServerBoxRegionConfigEntity {
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

    public BoxRegionConfig toBoxRegionConfig() {
        BoxRegionConfig boxRegionConfig = new BoxRegionConfig().setId(id).setInternalName(internalName).setCount(count).setMinInterval(minInterval).setMaxInterval(maxInterval).setMinDistanceToItems(minDistanceToItems);
        if (boxItemTypeId != null) {
            boxRegionConfig.setBoxItemTypeId(boxItemTypeId.getId());
        }
        if (region != null) {
            boxRegionConfig.setRegion(region.toPlaceConfig());
        }
        return boxRegionConfig;
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
