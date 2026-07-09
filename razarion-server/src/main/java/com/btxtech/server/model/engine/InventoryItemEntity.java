package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.service.engine.InventoryArtifactService;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifactCount;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;


/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "INVENTORY_ITEM")
public class InventoryItemEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    private double itemFreeRange;
    private Integer razarion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    private Integer crystalCost;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<InventoryArtifactCountEntity> inventoryArtifactCosts;


    public InventoryItem toInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem()
                .id(getId())
                .internalName(getInternalName())
                .baseItemTypeId(extractId(baseItemType, BaseItemTypeEntity::getId))
                .baseItemTypeCount(baseItemTypeCount)
                .baseItemTypeFreeRange(itemFreeRange)
                .razarion(razarion)
                .crystalCost(crystalCost);

        if (image != null) {
            inventoryItem.imageId(image.getId());
        }
        if (inventoryArtifactCosts != null) {
            List<InventoryArtifactCount> costs = new ArrayList<>();
            for (InventoryArtifactCountEntity costEntity : inventoryArtifactCosts) {
                costs.add(costEntity.toInventoryArtifactCount());
            }
            inventoryItem.setInventoryArtifactCosts(costs);
        }
        return inventoryItem;
    }

    public void fromInventoryItem(InventoryItem inventoryItem, InventoryArtifactService inventoryArtifactService) {
        setInternalName(inventoryItem.getInternalName());
        baseItemTypeCount = inventoryItem.getBaseItemTypeCount();
        itemFreeRange = inventoryItem.getBaseItemTypeFreeRange();
        razarion = inventoryItem.getRazarion();
        crystalCost = inventoryItem.getCrystalCost();
        if (inventoryArtifactCosts == null) {
            inventoryArtifactCosts = new ArrayList<>();
        }
        inventoryArtifactCosts.clear();
        if (inventoryItem.getInventoryArtifactCosts() != null) {
            for (InventoryArtifactCount cost : inventoryItem.getInventoryArtifactCosts()) {
                InventoryArtifactCountEntity costEntity = new InventoryArtifactCountEntity();
                costEntity.fromInventoryArtifactCount(cost, inventoryArtifactService.getEntity(cost.getInventoryArtifactId()));
                inventoryArtifactCosts.add(costEntity);
            }
        }
    }

    public void setBaseItemType(BaseItemTypeEntity baseItemType) {
        this.baseItemType = baseItemType;
    }

    public void setImage(ImageLibraryEntity image) {
        this.image = image;
    }

    public Integer getCrystalCost() {
        return crystalCost;
    }

    public List<InventoryArtifactCountEntity> getInventoryArtifactCosts() {
        return inventoryArtifactCosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryItemEntity that = (InventoryItemEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
