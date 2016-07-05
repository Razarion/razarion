package com.btxtech.server.terrain.object;

import com.btxtech.shared.datatypes.Index;

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
    private Index position;
    private double scale;
    private double zRotation;

    public Long getId() {
        return id;
    }

    public TerrainObjectEntity getTerrainObjectEntity() {
        return terrainObjectEntity;
    }

    public void setTerrainObjectEntity(TerrainObjectEntity terrainObjectEntity) {
        this.terrainObjectEntity = terrainObjectEntity;
    }

    public Index getPosition() {
        return position;
    }

    public void setPosition(Index position) {
        this.position = position;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getZRotation() {
        return zRotation;
    }

    public void setZRotation(double zRotation) {
        this.zRotation = zRotation;
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
