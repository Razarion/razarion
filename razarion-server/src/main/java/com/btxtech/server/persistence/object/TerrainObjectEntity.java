package com.btxtech.server.persistence.object;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT")
public class TerrainObjectEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity colladaEntity;

    public Long getId() {
        return id;
    }

    public TerrainObjectConfig toTerrainObjectConfig() {
        TerrainObjectConfig terrainObjectConfig = new TerrainObjectConfig();
        terrainObjectConfig.setId(id.intValue()).setInternalName(internalName);
        if (colladaEntity != null) {
            terrainObjectConfig.setShape3DId(colladaEntity.getId().intValue());
        }
        return terrainObjectConfig;
    }

    public void fromTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig, ColladaEntity colladaEntity) {
        this.internalName = terrainObjectConfig.getInternalName();
        this.colladaEntity = colladaEntity;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
