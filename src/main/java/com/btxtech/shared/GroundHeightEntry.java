package com.btxtech.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Portable
@Entity
@Table(name = "GROUND_HEIGHT_ENTRY")
public class GroundHeightEntry {
    @Id
    @GeneratedValue
    private Long id;
    private int xIndex;
    private int yIndex;
    private double height;

    /**
     * Used by GWT and errai
     */
    public GroundHeightEntry() {
    }

    public GroundHeightEntry(int xIndex, int yIndex, double height) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.height = height;
    }

    public Long getId() {
        return id;
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

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundHeightEntry that = (GroundHeightEntry) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
