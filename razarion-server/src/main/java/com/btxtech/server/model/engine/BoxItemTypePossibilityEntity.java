package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.InventoryItemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BOX_ITEM_TYPE_POSSIBILITY")
public class BoxItemTypePossibilityEntity extends BaseEntity {

    private double possibility;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private InventoryItemEntity inventoryItem;
    private Integer crystals;

    public BoxItemTypePossibility toBoxItemTypePossibility() {
        BoxItemTypePossibility boxItemTypePossibility = new BoxItemTypePossibility().setPossibility(possibility).setCrystals(crystals);
        if (inventoryItem != null) {
            boxItemTypePossibility.setInventoryItemId(inventoryItem.getId());
        }
        return boxItemTypePossibility;
    }

    public void fromBoxItemTypePossibility(BoxItemTypePossibility boxItemTypePossibility, InventoryItemService inventoryItemService) {
        possibility = boxItemTypePossibility.getPossibility();
        crystals = boxItemTypePossibility.getCrystals();
        if (inventoryItem != null) {
            boxItemTypePossibility.setInventoryItemId(inventoryItem.getId());
        }
        inventoryItem = inventoryItemService.getEntity(boxItemTypePossibility.getInventoryItemId());
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
