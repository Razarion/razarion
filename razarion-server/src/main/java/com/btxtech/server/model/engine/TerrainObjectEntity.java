package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.shared.dto.TerrainObjectConfig;
import jakarta.persistence.*;

import static com.btxtech.server.service.PersistenceUtil.extractId;


/**
 * Created by Beat
 * 10.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT")
public class TerrainObjectEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity model3DId;
    private double radius;

    public TerrainObjectConfig toTerrainObjectConfig() {
        return new TerrainObjectConfig()
                .id(getId())
                .internalName(getInternalName())
                .radius(radius)
                .model3DId(extractId(model3DId, Model3DEntity::getId));
    }

    public void fromTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig,
                                        Model3DService model3DService) {
        setInternalName(terrainObjectConfig.getInternalName());
        radius = terrainObjectConfig.getRadius();
        model3DId = model3DService.getEntity(terrainObjectConfig.getModel3DId());
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectEntity that = (TerrainObjectEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
