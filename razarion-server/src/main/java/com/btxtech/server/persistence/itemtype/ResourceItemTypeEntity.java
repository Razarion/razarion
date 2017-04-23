package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 04.10.2016.
 */
@Entity
@Table(name = "RESOURCE_ITEM_TYPE")
public class ResourceItemTypeEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3DId;
    private String name;
    private double radius;
    private int amount;

    public ResourceItemType toResourceItemType() {
        ResourceItemType resourceItemType = new ResourceItemType();
        resourceItemType.setRadius(radius).setAmount(amount).setId(id).setName(name);
        if (shape3DId != null) {
            resourceItemType.setShape3DId(shape3DId.getId());
        }
        return resourceItemType;
    }

    public void fromBaseItemType(ResourceItemType resourceItemType) {
        name = resourceItemType.getName();
        radius = resourceItemType.getRadius();
        amount = resourceItemType.getAmount();
    }

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
