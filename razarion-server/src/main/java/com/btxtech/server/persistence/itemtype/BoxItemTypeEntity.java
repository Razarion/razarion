package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 04.10.2016.
 */
@Entity
@Table(name = "BOX_ITEM_TYPE")
public class BoxItemTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    private double radius;
    private boolean fixVerticalNorm;
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;
    private Integer ttl; // seconds
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    List<BoxItemTypePossibilityEntity> boxItemTypePossibilities;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelPackConfigEntity threeJsModelPackConfigEntity;

    public Integer getId() {
        return id;
    }

    public BoxItemType toBoxItemType() {
        BoxItemType boxItemType = (BoxItemType)new BoxItemType()
                .radius(radius)
                .ttl(ttl)
                .fixVerticalNorm(fixVerticalNorm)
                .terrainType(terrainType)
                .id(id)
                .internalName(internalName)
                .thumbnail(extractId(thumbnail, ImageLibraryEntity::getId))
                .threeJsModelPackConfigId(extractId(threeJsModelPackConfigEntity, ThreeJsModelPackConfigEntity::getId));
        if (i18nName != null) {
            boxItemType.setI18nName(i18nName.toI18nString());
        }
        if (i18nDescription != null) {
            boxItemType.setI18nDescription(i18nDescription.toI18nString());
        }
        if (this.boxItemTypePossibilities != null && !this.boxItemTypePossibilities.isEmpty()) {
            List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
            for (BoxItemTypePossibilityEntity boxItemTypePossibility : this.boxItemTypePossibilities) {
                boxItemTypePossibilities.add(boxItemTypePossibility.toBoxItemTypePossibility());
            }
            boxItemType.boxItemTypePossibilities(boxItemTypePossibilities);
        }
        return boxItemType;
    }

    public void fromBoxItemType(BoxItemType boxItemType, InventoryItemCrudPersistence inventoryPersistence) {
        internalName = boxItemType.getInternalName();
        radius = boxItemType.getRadius();
        fixVerticalNorm = boxItemType.isFixVerticalNorm();
        terrainType = boxItemType.getTerrainType();
        ttl = boxItemType.getTtl();
        i18nName = I18nBundleEntity.fromI18nStringSafe(boxItemType.getI18nName(), i18nName);
        i18nDescription = I18nBundleEntity.fromI18nStringSafe(boxItemType.getI18nDescription(), i18nDescription);
        if (boxItemTypePossibilities == null) {
            boxItemTypePossibilities = new ArrayList<>();
        }
        boxItemTypePossibilities.clear();
        if (boxItemType.getBoxItemTypePossibilities() != null) {
            for (BoxItemTypePossibility boxItemTypePossibility : boxItemType.getBoxItemTypePossibilities()) {
                BoxItemTypePossibilityEntity boxItemTypePossibilityEntity = new BoxItemTypePossibilityEntity();
                boxItemTypePossibilityEntity.fromBoxItemTypePossibility(boxItemTypePossibility, inventoryPersistence);
                boxItemTypePossibilities.add(boxItemTypePossibilityEntity);
            }
        }
    }

    public void setThreeJsModelPackConfigEntity(ThreeJsModelPackConfigEntity threeJsModelPackConfigEntity) {
        this.threeJsModelPackConfigEntity = threeJsModelPackConfigEntity;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
