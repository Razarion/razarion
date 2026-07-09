package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifact;
import com.btxtech.shared.gameengine.datatypes.Rareness;
import jakarta.persistence.*;

/**
 * Collectible artifact. Ported from the legacy controltheland project
 * (DbInventoryArtifact).
 */
@Entity
@Table(name = "INVENTORY_ARTIFACT")
public class InventoryArtifactEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Rareness rareness;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    private Integer crystalCost;

    public InventoryArtifact toInventoryArtifact() {
        InventoryArtifact inventoryArtifact = new InventoryArtifact()
                .id(getId())
                .internalName(getInternalName())
                .rareness(rareness)
                .crystalCost(crystalCost);
        if (image != null) {
            inventoryArtifact.imageId(image.getId());
        }
        return inventoryArtifact;
    }

    public void fromInventoryArtifact(InventoryArtifact inventoryArtifact) {
        setInternalName(inventoryArtifact.getInternalName());
        rareness = inventoryArtifact.getRareness();
        crystalCost = inventoryArtifact.getCrystalCost();
    }

    public void setImage(ImageLibraryEntity image) {
        this.image = image;
    }

    public Integer getCrystalCost() {
        return crystalCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryArtifactEntity that = (InventoryArtifactEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
