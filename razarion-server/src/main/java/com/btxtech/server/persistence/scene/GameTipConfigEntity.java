package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.BoxItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.GameTipConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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

/**
 * Created by Beat
 * 16.05.2017.
 */
@Entity
@Table(name = "SCENE_TIP_CONFIG")
public class GameTipConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private GameTipConfig.Tip tip;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity actor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity toCreatedItemType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ResourceItemTypeEntity resourceItemTypeEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BoxItemTypeEntity boxItemTypeEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private InventoryItemEntity inventoryItemEntity;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "terrainPositionHintX")),
            @AttributeOverride(name = "y", column = @Column(name = "terrainPositionHintY")),
    })
    private DecimalPosition terrainPositionHint;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity placeConfig;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity scrollMapImage;

    public GameTipConfig toGameTipConfig() {
        GameTipConfig gameTipConfig = new GameTipConfig().setTip(tip);
        if (actor != null) {
            gameTipConfig.setActor(actor.getId());
        }
        if (toCreatedItemType != null) {
            gameTipConfig.setToCreatedItemTypeId(toCreatedItemType.getId());
        }
        if (resourceItemTypeEntity != null) {
            gameTipConfig.setResourceItemTypeId(resourceItemTypeEntity.getId());
        }
        if (boxItemTypeEntity != null) {
            gameTipConfig.setBoxItemTypeId(boxItemTypeEntity.getId());
        }
        if (inventoryItemEntity != null) {
            gameTipConfig.setInventoryItemId(inventoryItemEntity.getId());
        }
        gameTipConfig.setTerrainPositionHint(terrainPositionHint);
        if (placeConfig != null) {
            gameTipConfig.setPlaceConfig(placeConfig.toPlaceConfig());
        }
        if (scrollMapImage != null) {
            gameTipConfig.setScrollMapImageId(scrollMapImage.getId());
        }
        return gameTipConfig;
    }

    public void setTip(GameTipConfig.Tip tip) {
        this.tip = tip;
    }

    public void setActor(BaseItemTypeEntity actor) {
        this.actor = actor;
    }

    public void setToCreatedItemType(BaseItemTypeEntity toCreatedItemTypeId) {
        this.toCreatedItemType = toCreatedItemTypeId;
    }

    public void setResourceItemTypeEntity(ResourceItemTypeEntity resourceItemTypeEntity) {
        this.resourceItemTypeEntity = resourceItemTypeEntity;
    }

    public void setBoxItemTypeEntity(BoxItemTypeEntity boxItemTypeEntity) {
        this.boxItemTypeEntity = boxItemTypeEntity;
    }

    public void setInventoryItemEntity(InventoryItemEntity inventoryItemEntity) {
        this.inventoryItemEntity = inventoryItemEntity;
    }

    public void setTerrainPositionHint(DecimalPosition terrainPositionHint) {
        this.terrainPositionHint = terrainPositionHint;
    }

    public void setPlaceConfig(PlaceConfigEntity placeConfig) {
        this.placeConfig = placeConfig;
    }

    public void setScrollMapImage(ImageLibraryEntity scrollMapImage) {
        this.scrollMapImage = scrollMapImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GameTipConfigEntity that = (GameTipConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
