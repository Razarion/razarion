package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;

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
 * 13.05.2017.
 */
@Entity
@Table(name = "BOX_ITEM_TYPE_POSSIBILITY")
public class BoxItemTypePossibilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private double possibility;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private InventoryItemEntity inventoryItem;

    public BoxItemTypePossibility toBoxItemTypePossibility() {
        BoxItemTypePossibility boxItemTypePossibility = new BoxItemTypePossibility().setPossibility(possibility);
        if (inventoryItem != null) {
            boxItemTypePossibility.setInventoryItemId(inventoryItem.getId());
        }
        return boxItemTypePossibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoxItemTypePossibilityEntity that = (BoxItemTypePossibilityEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}