package com.btxtech.server.persistence.object;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 12.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT_POSITION")
public class TerrainObjectPositionEntity {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private TerrainObjectEntity terrainObjectEntity;
    @Embedded
    private DecimalPosition position;
    private double scale;
    private double rotationZ;

    public Long getId() {
        return id;
    }

    public TerrainObjectPosition toTerrainObjectPosition() {
        TerrainObjectPosition objectPosition = new TerrainObjectPosition();
        objectPosition.setId(id.intValue());
        objectPosition.setTerrainObjectId(terrainObjectEntity.getId().intValue());
        objectPosition.setScale(scale);
        objectPosition.setRotationZ(rotationZ);
        objectPosition.setPosition(position);
        return objectPosition;
    }

    public void setTerrainObjectEntity(TerrainObjectEntity terrainObjectEntity) {
        this.terrainObjectEntity = terrainObjectEntity;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setRotationZ(double rotationZ) {
        this.rotationZ = rotationZ;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
