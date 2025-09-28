package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.shared.dto.GroundConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity groundBabylonMaterial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity waterBabylonMaterial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity underWaterBabylonMaterialId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity botBabylonMaterialId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity botWallBabylonMaterialId;

    public GroundConfig toConfig() {
        return new GroundConfig()
                .id(getId())
                .internalName(getInternalName())
                .groundBabylonMaterialId(extractId(groundBabylonMaterial, BabylonMaterialEntity::getId))
                .waterBabylonMaterialId(extractId(waterBabylonMaterial, BabylonMaterialEntity::getId))
                .underWaterBabylonMaterialId(extractId(underWaterBabylonMaterialId, BabylonMaterialEntity::getId))
                .botBabylonMaterialId(extractId(botBabylonMaterialId, BabylonMaterialEntity::getId))
                .botWallBabylonMaterialId(extractId(botWallBabylonMaterialId, BabylonMaterialEntity::getId));
    }

    public void fromGroundConfig(GroundConfig config, BabylonMaterialService babylonMaterialService) {
        setInternalName(config.getInternalName());
        groundBabylonMaterial = babylonMaterialService.getEntity(config.getGroundBabylonMaterialId());
        waterBabylonMaterial = babylonMaterialService.getEntity(config.getWaterBabylonMaterialId());
        underWaterBabylonMaterialId = babylonMaterialService.getEntity(config.getUnderWaterBabylonMaterialId());
        botBabylonMaterialId = babylonMaterialService.getEntity(config.getBotBabylonMaterialId());
        botWallBabylonMaterialId = babylonMaterialService.getEntity(config.getBotWallBabylonMaterialId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundConfigEntity that = (GroundConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
