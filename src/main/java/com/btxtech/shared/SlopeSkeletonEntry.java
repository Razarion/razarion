package com.btxtech.shared;

import com.btxtech.shared.primitives.Vertex;
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
@Portable
@Entity
@Table(name = "SLOPE_SKELETON_ENTRY")
public class SlopeSkeletonEntry {
    @Id
    @GeneratedValue
    private Long id;
    private int columnIndex;
    private int rowIndex;
    @Embedded
    private Vertex position;
    private float slopeFactor;
    private double normShift;

    /**
     * Used by JPA & errai
     */
    public SlopeSkeletonEntry() {
    }

    public SlopeSkeletonEntry(int columnIndex, int rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public float getSlopeFactor() {
        return slopeFactor;
    }

    public void setSlopeFactor(float slopeFactor) {
        this.slopeFactor = slopeFactor;
    }

    public double getNormShift() {
        return normShift;
    }

    public void setNormShift(double normShift) {
        this.normShift = normShift;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeSkeletonEntry that = (SlopeSkeletonEntry) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
