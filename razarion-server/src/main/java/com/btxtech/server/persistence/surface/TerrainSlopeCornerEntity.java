package com.btxtech.server.persistence.surface;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * on 04.06.2017.
 */
@Entity
@Table(name = "TERRAIN_SLOPE_CORNER")
public class TerrainSlopeCornerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Embedded
    private DecimalPosition position;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private DrivewayConfigEntity drivewayConfigEntity;

    public TerrainSlopeCorner toTerrainSlopeCorner() {
        TerrainSlopeCorner terrainSlopeCorner = new TerrainSlopeCorner();
        terrainSlopeCorner.setPosition(position);
        if (drivewayConfigEntity != null) {
            terrainSlopeCorner.setSlopeDrivewayId(drivewayConfigEntity.getId());
        }
        return terrainSlopeCorner;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public void setDrivewayConfigEntity(DrivewayConfigEntity drivewayConfigEntity) {
        this.drivewayConfigEntity = drivewayConfigEntity;
    }

    public TerrainSlopeCornerEntity position(DecimalPosition position) {
        setPosition(position);
        return this;
    }

    public TerrainSlopeCornerEntity drivewayConfigEntity(DrivewayConfigEntity drivewayConfigEntity) {
        setDrivewayConfigEntity(drivewayConfigEntity);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainSlopeCornerEntity that = (TerrainSlopeCornerEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
