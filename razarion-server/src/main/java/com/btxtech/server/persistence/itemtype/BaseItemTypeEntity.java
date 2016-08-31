package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2016.
 */
@Entity
@Table(name = "BASE_ITEM_TYPE")
public class BaseItemTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private double radius;
    private int health;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3DId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity spawnShape3DId;
    private int spawnDurationMillis;

    public Long getId() {
        return id;
    }

    public BaseItemType toBaseItemType() {
        BaseItemType baseItemType = (BaseItemType) new BaseItemType().setName(name).setId(id.intValue());
        baseItemType.setTerrainType(TerrainType.LAND); // TODO
        baseItemType.setMovableType(new MovableType().setSpeed(10)); // TODO
        if (shape3DId != null) {
            baseItemType.setShape3DId(shape3DId.getId().intValue());
        }
        baseItemType.setRadius(radius);
        if (spawnShape3DId != null) {
            baseItemType.setSpawnShape3DId(spawnShape3DId.getId().intValue());
        }
        baseItemType.setSpawnDurationMillis(spawnDurationMillis);
        return baseItemType.setHealth(health);
    }

    public void fromBaseItemType(BaseItemType baseItemType) {
        name = baseItemType.getName();
        radius = baseItemType.getRadius();
        health = baseItemType.getHealth();
        spawnDurationMillis = baseItemType.getSpawnDurationMillis();
    }

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
    }

    public void setSpawnShape3DId(ColladaEntity spawnShape3DId) {
        this.spawnShape3DId = spawnShape3DId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseItemTypeEntity that = (BaseItemTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
