package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeEntity;
import com.btxtech.shared.dto.BotHarvestCommandConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
 * 15.05.2017.
 */
@Entity
@Table(name = "SCENE_BOT_HARVEST_COMMAND")
public class BotHarvestCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer botAuxiliaryIdId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity harvesterItemType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ResourceItemTypeEntity resourceItemType;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity resourceSelection;

    public BotHarvestCommandConfig toBotHarvestCommandConfig() {
        BotHarvestCommandConfig botHarvestCommandConfig = new BotHarvestCommandConfig().setBotAuxiliaryId(botAuxiliaryIdId);
        if (harvesterItemType != null) {
            botHarvestCommandConfig.setHarvesterItemTypeId(harvesterItemType.getId());
        }
        if (resourceItemType != null) {
            botHarvestCommandConfig.setResourceItemTypeId(resourceItemType.getId());
        }
        if (resourceSelection != null) {
            botHarvestCommandConfig.setResourceSelection(resourceSelection.toPlaceConfig());
        }
        return botHarvestCommandConfig;
    }

    public void setBotAuxiliaryIdId(Integer botAuxiliaryIdId) {
        this.botAuxiliaryIdId = botAuxiliaryIdId;
    }

    public void setHarvesterItemType(BaseItemTypeEntity harvesterItemType) {
        this.harvesterItemType = harvesterItemType;
    }

    public void setResourceItemType(ResourceItemTypeEntity resourceItemType) {
        this.resourceItemType = resourceItemType;
    }

    public void setResourceSelection(PlaceConfigEntity resourceSelection) {
        this.resourceSelection = resourceSelection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotHarvestCommandEntity that = (BotHarvestCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
