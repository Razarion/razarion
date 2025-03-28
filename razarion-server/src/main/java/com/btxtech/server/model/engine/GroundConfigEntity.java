package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.shared.dto.GroundConfig;
import jakarta.persistence.*;

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

    public GroundConfig toConfig() {
        return new GroundConfig()
                .id(getId())
                .internalName(getInternalName())
                .groundBabylonMaterialId(extractId(groundBabylonMaterial, BabylonMaterialEntity::getId))
                .waterBabylonMaterialId(extractId(waterBabylonMaterial, BabylonMaterialEntity::getId));
    }

    public void fromGroundConfig(GroundConfig config, BabylonMaterialService babylonMaterialService) {
        setInternalName(config.getInternalName());
        groundBabylonMaterial = babylonMaterialService.getEntity(config.getGroundBabylonMaterialId());
        waterBabylonMaterial = babylonMaterialService.getEntity(config.getWaterBabylonMaterialId());
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
