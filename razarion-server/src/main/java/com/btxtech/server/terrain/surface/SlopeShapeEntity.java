package com.btxtech.server.terrain.surface;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeShape;

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
@Table(name = "slope_shape_entry") // TODO rename
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

    public SlopeShape toSlopeShape() {
        return new SlopeShape(position, slopeFactor);
    }

    public void fromSlopeShape(SlopeShape slopeShape) {
        position = slopeShape.getPosition();
        slopeFactor = slopeShape.getSlopeFactor();
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
