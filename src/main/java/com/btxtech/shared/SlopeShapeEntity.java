package com.btxtech.shared;

import com.btxtech.game.jsre.client.common.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 13.02.2016.
 */
@Entity
@Portable
@Table(name = "slope_shape_entry")
public class SlopeShapeEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Embedded
    private Index position;
    /**
     * 1.0 is slope 0.0 is ground
     */
    private float slopeFactor;

    /**
     * Used by Errai and JPA
     */
    public SlopeShapeEntity() {
    }

    public SlopeShapeEntity(Index position, float slopeFactor) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeShapeEntity that = (SlopeShapeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
