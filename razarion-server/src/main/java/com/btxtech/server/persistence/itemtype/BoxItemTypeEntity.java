package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
    private ColladaEntity shape3DId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    private double radius;
    private boolean fixVerticalNorm;
    private Integer ttl;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    List<BoxItemTypePossibilityEntity> boxItemTypePossibilities;

    public Integer getId() {
        return id;
    }

    public BoxItemType toBoxItemType() {
        BoxItemType boxItemType = new BoxItemType();
        boxItemType.setRadius(radius).setTtl(ttl).setFixVerticalNorm(fixVerticalNorm).setId(id).setInternalName(internalName);
        if (shape3DId != null) {
            boxItemType.setShape3DId(shape3DId.getId());
        }
        if (thumbnail != null) {
            boxItemType.setThumbnail(thumbnail.getId());
        }
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
            boxItemType.setBoxItemTypePossibilities(boxItemTypePossibilities);
        }
        return boxItemType;
    }

    public void fromBoxItemType(BoxItemType boxItemType, InventoryPersistence inventoryPersistence) {
        internalName = boxItemType.getInternalName();
        radius = boxItemType.getRadius();
        fixVerticalNorm = boxItemType.isFixVerticalNorm();
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

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
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
