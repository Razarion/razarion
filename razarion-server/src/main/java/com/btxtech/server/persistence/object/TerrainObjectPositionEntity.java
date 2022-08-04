package com.btxtech.server.persistence.object;

import com.btxtech.server.persistence.TerrainObjectCrudPersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainObjectPosition;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
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
 * 12.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT_POSITION")
public class TerrainObjectPositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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

    public Integer getId() {
        return id;
    }

    public TerrainObjectPosition toTerrainObjectPosition() {
        TerrainObjectPosition objectPosition = new TerrainObjectPosition();
        objectPosition.setId(id);
        objectPosition.setTerrainObjectConfigId(terrainObjectEntity.getId());
        objectPosition.setPosition(position);
        objectPosition.setScale(scale);
        objectPosition.setRotation(rotation);
        return objectPosition;
    }

    public void fromTerrainObjectPosition(TerrainObjectPosition terrainObjectPosition, TerrainObjectCrudPersistence terrainObjectCrudPersistence) {
        terrainObjectEntity = terrainObjectCrudPersistence.getEntity(terrainObjectPosition.getTerrainObjectConfigId());
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
