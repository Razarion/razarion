package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifactCount;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.service.PersistenceUtil.extractId;

/**
 * Required amount of a given {@link InventoryArtifactEntity}, used as the
 * assemble cost of an {@link InventoryItemEntity}. Ported from the legacy
 * controltheland project (DbInventoryArtifactCount).
 */
@Entity
@Table(name = "INVENTORY_ITEM_ARTIFACT_COUNT")
public class InventoryArtifactCountEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private InventoryArtifactEntity inventoryArtifact;
    private int count;

    public InventoryArtifactCount toInventoryArtifactCount() {
        return new InventoryArtifactCount()
                .inventoryArtifactId(extractId(inventoryArtifact, InventoryArtifactEntity::getId))
                .count(count);
    }

    public void fromInventoryArtifactCount(InventoryArtifactCount inventoryArtifactCount, InventoryArtifactEntity inventoryArtifact) {
        this.inventoryArtifact = inventoryArtifact;
        this.count = inventoryArtifactCount.getCount();
    }

    public InventoryArtifactEntity getInventoryArtifact() {
        return inventoryArtifact;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryArtifactCountEntity that = (InventoryArtifactCountEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
