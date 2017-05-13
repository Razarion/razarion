package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
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
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3DId;
    private String name;
    private double radius;
    private Integer ttl;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    List<BoxItemTypePossibilityEntity> boxItemTypePossibilities;

    public BoxItemType toBoxItemType() {
        BoxItemType boxItemType = new BoxItemType();
        boxItemType.setRadius(radius).setTtl(ttl).setId(id).setName(name);
        if (shape3DId != null) {
            boxItemType.setShape3DId(shape3DId.getId());
        }
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        if(this.boxItemTypePossibilities != null) {
            for (BoxItemTypePossibilityEntity boxItemTypePossibility : this.boxItemTypePossibilities) {
                boxItemTypePossibilities.add(boxItemTypePossibility.toBoxItemTypePossibility());
            }
        }
        boxItemType.setBoxItemTypePossibilities(boxItemTypePossibilities);
        return boxItemType;
    }

    public void fromBoxItemType(BoxItemType boxItemType) {
        name = boxItemType.getName();
        radius = boxItemType.getRadius();
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

        BoxItemTypeEntity that = (BoxItemTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
