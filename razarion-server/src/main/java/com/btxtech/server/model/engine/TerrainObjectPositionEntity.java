package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.engine.TerrainObjectService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectPosition;
import jakarta.persistence.*;

@Entity
@Table(name = "TERRAIN_OBJECT_POSITION")
public class TerrainObjectPositionEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private TerrainObjectEntity terrainObjectEntity;
    @Embedded
    private DecimalPosition position;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "scaleX")),
            @AttributeOverride(name = "y", column = @Column(name = "scaleY")),
            @AttributeOverride(name = "z", column = @Column(name = "scaleZ")),
    })
    private Vertex scale;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "rotationX")),
            @AttributeOverride(name = "y", column = @Column(name = "rotationY")),
            @AttributeOverride(name = "z", column = @Column(name = "rotationZ")),
    })
    private Vertex rotation;

    public TerrainObjectPosition toTerrainObjectPosition() {
        TerrainObjectPosition objectPosition = new TerrainObjectPosition();
        objectPosition.setId(getId());
        objectPosition.setTerrainObjectConfigId(terrainObjectEntity.getId());
        objectPosition.setPosition(position);
        objectPosition.setScale(scale);
        objectPosition.setRotation(rotation);
        return objectPosition;
    }

    public void fromTerrainObjectPosition(TerrainObjectPosition terrainObjectPosition, TerrainObjectService terrainObjectService) {
        terrainObjectEntity = terrainObjectService.getEntity(terrainObjectPosition.getTerrainObjectConfigId());
        position = terrainObjectPosition.getPosition();
        scale = terrainObjectPosition.getScale();
        rotation = terrainObjectPosition.getRotation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectPositionEntity that = (TerrainObjectPositionEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
