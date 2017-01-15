package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "PLANET")
public class PlanetEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<TerrainSlopePositionEntity> terrainSlopePositionEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<TerrainObjectPositionEntity> terrainObjectPositionEntities;

    public PlanetConfig toPlanetConfig() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (TerrainSlopePositionEntity terrainSlopePositionEntity : terrainSlopePositionEntities) {
            terrainSlopePositions.add(terrainSlopePositionEntity.toTerrainSlopePosition());
        }
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        for (TerrainObjectPositionEntity terrainObjectPositionEntity : terrainObjectPositionEntities) {
            terrainObjectPositions.add(terrainObjectPositionEntity.toTerrainObjectPosition());
        }

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);
        return planetConfig;
    }

    public List<TerrainSlopePositionEntity> getTerrainSlopePositionEntities() {
        return terrainSlopePositionEntities;
    }

    public void setTerrainObjectPositionEntities(List<TerrainObjectPositionEntity> terrainObjectPositionEntities) {
        this.terrainObjectPositionEntities.clear();
        this.terrainObjectPositionEntities.addAll(terrainObjectPositionEntities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlanetEntity that = (PlanetEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
