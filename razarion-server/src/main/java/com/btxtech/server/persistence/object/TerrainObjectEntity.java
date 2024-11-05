package com.btxtech.server.persistence.object;

import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelPackCrudPersistence;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT")
public class TerrainObjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelPackConfigEntity threeJsModelPackConfig;
    private double radius;

    public Integer getId() {
        return id;
    }

    public TerrainObjectConfig toTerrainObjectConfig() {
        return new TerrainObjectConfig()
                .id(id)
                .internalName(internalName)
                .radius(radius)
                .threeJsModelPackConfigId(extractId(threeJsModelPackConfig, ThreeJsModelPackConfigEntity::getId));
    }

    public void fromTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig, ThreeJsModelPackCrudPersistence threeJsModelPackCrudPersistence) {
        this.internalName = terrainObjectConfig.getInternalName();
        radius = terrainObjectConfig.getRadius();
        threeJsModelPackConfig = threeJsModelPackCrudPersistence.getEntity(terrainObjectConfig.getThreeJsModelPackConfigId());
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
