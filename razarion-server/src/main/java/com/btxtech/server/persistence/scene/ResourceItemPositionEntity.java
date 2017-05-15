package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.ResourceItemPosition;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 16.05.2017.
 */
@Entity
@Table(name = "SCENE_RESOURCE_ITEM_POSITION")
public class ResourceItemPositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ResourceItemTypeEntity resourceItemType;
    private DecimalPosition position;
    private double rotationZ;

    public ResourceItemPosition toResourceItemPosition() {
        ResourceItemPosition resourceItemPosition = new ResourceItemPosition().setPosition(position).setRotationZ(rotationZ);
        if (resourceItemType != null) {
            resourceItemPosition.setResourceItemTypeId(resourceItemType.getId());
        }
        return resourceItemPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceItemPositionEntity that = (ResourceItemPositionEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
