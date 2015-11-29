package com.btxtech.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 21.11.2015.
 */
@Entity
@Portable
@Bindable
@Table(name = "TERRAIN_PLATEAU_CONFIG")
public class PlateauConfigEntity {
    @Id
    @GeneratedValue
    private Long id;
    private int top;
    private double slopeTopThreshold;
    private double slopeTopThresholdFading;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;
    private double fractal;

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public double getSlopeTopThreshold() {
        return slopeTopThreshold;
    }

    public void setSlopeTopThreshold(double slopeTopThreshold) {
        this.slopeTopThreshold = slopeTopThreshold;
    }

    public double getSlopeTopThresholdFading() {
        return slopeTopThresholdFading;
    }

    public void setSlopeTopThresholdFading(double slopeTopThresholdFading) {
        this.slopeTopThresholdFading = slopeTopThresholdFading;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }

    public double getFractal() {
        return fractal;
    }

    public void setFractal(double fractal) {
        this.fractal = fractal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlateauConfigEntity that = (PlateauConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
