package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BoxItemPosition;

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
@Table(name = "SCENE_BOX_ITEM_POSITION")
public class BoxItemPositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BoxItemTypeEntity boxItemType;
    private DecimalPosition position;
    private double rotationZ;

    public BoxItemPosition toBoxItemPosition() {
        BoxItemPosition resourceItemPosition = new BoxItemPosition().setPosition(position).setRotationZ(rotationZ);
        if (boxItemType != null) {
            resourceItemPosition.setBoxItemTypeId(boxItemType.getId());
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

        BoxItemPositionEntity that = (BoxItemPositionEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
