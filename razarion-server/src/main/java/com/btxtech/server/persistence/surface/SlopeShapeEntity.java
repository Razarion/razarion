package com.btxtech.server.persistence.surface;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlopeShape;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 13.02.2016.
 */
@Entity
@Table(name = "SLOPE_SHAPE")
public class SlopeShapeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Embedded
    private DecimalPosition position;
    /**
     * 1.0 is slope 0.0 is ground
     */
    private double slopeFactor;

    public SlopeShape toSlopeShape() {
        return new SlopeShape().position(position).slopeFactor(slopeFactor);
    }

    public void fromSlopeShape(SlopeShape slopeShape) {
        position = slopeShape.getPosition();
        slopeFactor = slopeShape.getSlopeFactor();
    }

    public SlopeShapeEntity setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public SlopeShapeEntity setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
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

        SlopeShapeEntity that = (SlopeShapeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
