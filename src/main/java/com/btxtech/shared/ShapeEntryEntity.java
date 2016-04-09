package com.btxtech.shared;

import com.btxtech.game.jsre.client.common.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 13.02.2016.
 */
@Entity
@Portable
@Table(name = "TERRAIN_PLATEAU_CONFIG_SHAPE")
public class ShapeEntryEntity {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private PlateauConfigEntity plateauConfigEntity;
    @Embedded
    private Index position;
    /**
     * 1.0 is slope 0.0 is ground
     */
    private float slopeFactor;

    /**
     * Used by Errai and JPA
     */
    public ShapeEntryEntity() {
    }

    public ShapeEntryEntity(Index position, float slopeFactor) {
        this.position = position;
        this.slopeFactor = slopeFactor;
    }

    public Index getPosition() {
        return position;
    }

    public void setPosition(Index position) {
        this.position = position;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }

    public void setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public ShapeEntryEntity(PlateauConfigEntity plateauConfigEntity) {
        this.plateauConfigEntity = plateauConfigEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShapeEntryEntity that = (ShapeEntryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
