package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelPackCrudPersistence;
import com.btxtech.server.persistence.ui.Model3DEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 04.10.2016.
 */
@Entity
@Table(name = "RESOURCE_ITEM_TYPE")
public class ResourceItemTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private double radius;
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;
    private boolean fixVerticalNorm;
    private int amount;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity model3DEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelPackConfigEntity threeJsModelPackConfigEntity;

    public Integer getId() {
        return id;
    }

    public ResourceItemType toResourceItemType() {
        ResourceItemType resourceItemType = (ResourceItemType) new ResourceItemType()
                .setRadius(radius)
                .setAmount(amount)
                .setFixVerticalNorm(fixVerticalNorm)
                .setTerrainType(terrainType)
                .id(id)
                .internalName(internalName)
                .model3DId(extractId(model3DEntity, Model3DEntity::getId));
        if (i18nName != null) {
            resourceItemType.setI18nName(i18nName.toI18nString());
        }
        if (i18nDescription != null) {
            resourceItemType.setI18nDescription(i18nDescription.toI18nString());
        }
        if (thumbnail != null) {
            resourceItemType.setThumbnail(thumbnail.getId());
        }
        return resourceItemType;
    }

    public void fromResourceItemType(ResourceItemType resourceItemType, ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence) {
        internalName = resourceItemType.getInternalName();
        radius = resourceItemType.getRadius();
        fixVerticalNorm = resourceItemType.isFixVerticalNorm();
        terrainType = resourceItemType.getTerrainType();
        amount = resourceItemType.getAmount();
        i18nName = I18nBundleEntity.fromI18nStringSafe(resourceItemType.getI18nName(), i18nName);
        i18nDescription = I18nBundleEntity.fromI18nStringSafe(resourceItemType.getI18nDescription(), i18nDescription);
        threeJsModelPackConfigEntity = threeJsModelPackCrudPersistence.getEntity(resourceItemType.getThreeJsModelPackConfigId());
    }

    public void setThumbnail(ImageLibraryEntity thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setModel3DEntity(Model3DEntity model3DEntity) {
        this.model3DEntity = model3DEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceItemTypeEntity that = (ResourceItemTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
