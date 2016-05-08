package com.btxtech.server.terrain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "GROUND_SPLATTING_ENTRY")
public class GroundSplattingEntity {
    @Id
    @GeneratedValue
    private Long id;
    private int xIndex;
    private int yIndex;
    private double splatting;

    /**
     * Used by GWT and errai
     */
    public GroundSplattingEntity() {
    }

    public GroundSplattingEntity(int xIndex, int yIndex, double splatting) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.splatting = splatting;
    }

    public int getXIndex() {
        return xIndex;
    }

    public void setXIndex(int xIndex) {
        this.xIndex = xIndex;
    }

    public int getYIndex() {
        return yIndex;
    }

    public void setYIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public double getSplatting() {
        return splatting;
    }

    public void setSplatting(double splatting) {
        this.splatting = splatting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundSplattingEntity that = (GroundSplattingEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
