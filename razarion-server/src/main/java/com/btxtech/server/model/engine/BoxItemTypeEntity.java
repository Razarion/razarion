package com.btxtech.server.model.engine;


import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.service.engine.InventoryItemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;

@Entity
@Table(name = "BOX_ITEM_TYPE")
public class BoxItemTypeEntity extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    private double radius;
    private boolean fixVerticalNorm;
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity model3DEntity;
    private Integer ttl; // seconds
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<BoxItemTypePossibilityEntity> boxItemTypePossibilities;


    public BoxItemType toBoxItemType() {
        BoxItemType boxItemType = (BoxItemType) new BoxItemType()
                .radius(radius)
                .ttl(ttl)
                .fixVerticalNorm(fixVerticalNorm)
                .terrainType(terrainType)
                .id(getId())
                .internalName(getInternalName())
                .thumbnail(extractId(thumbnail, ImageLibraryEntity::getId))
                .model3DId(extractId(model3DEntity, Model3DEntity::getId));
        if (this.boxItemTypePossibilities != null && !this.boxItemTypePossibilities.isEmpty()) {
            List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
            for (BoxItemTypePossibilityEntity boxItemTypePossibility : this.boxItemTypePossibilities) {
                boxItemTypePossibilities.add(boxItemTypePossibility.toBoxItemTypePossibility());
            }
            boxItemType.boxItemTypePossibilities(boxItemTypePossibilities);
        }
        return boxItemType;
    }

    public void fromBoxItemType(BoxItemType boxItemType, InventoryItemService inventoryItemService) {
        radius = boxItemType.getRadius();
        fixVerticalNorm = boxItemType.isFixVerticalNorm();
        terrainType = boxItemType.getTerrainType();
        ttl = boxItemType.getTtl();
        if (boxItemTypePossibilities == null) {
            boxItemTypePossibilities = new ArrayList<>();
        }
        boxItemTypePossibilities.clear();
        if (boxItemType.getBoxItemTypePossibilities() != null) {
            for (BoxItemTypePossibility boxItemTypePossibility : boxItemType.getBoxItemTypePossibilities()) {
                BoxItemTypePossibilityEntity boxItemTypePossibilityEntity = new BoxItemTypePossibilityEntity();
                boxItemTypePossibilityEntity.fromBoxItemTypePossibility(boxItemTypePossibility, inventoryItemService);
                boxItemTypePossibilities.add(boxItemTypePossibilityEntity);
            }
        }
    }

    public void setModel3DEntity(Model3DEntity model3DEntity) {
        this.model3DEntity = model3DEntity;
    }

    public void setThumbnail(ImageLibraryEntity thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoxItemTypeEntity that = (BoxItemTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
