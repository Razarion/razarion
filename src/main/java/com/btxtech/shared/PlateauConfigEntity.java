package com.btxtech.shared;

import com.btxtech.game.jsre.client.common.Index;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;

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
    @ElementCollection
    @OrderColumn(name = "orderColumn")
    @CollectionTable(name = "TERRAIN_PLATEAU_CONFIG_SHAPE")
    private List<Index> shape;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;
    private double fractalShift;
    private double fractalRoughness;
    private int verticalSpace;

    public List<Index> getShape() {
        return shape;
    }

    public void setShape(List<Index> shape) {
        this.shape = shape;
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

    public double getFractalShift() {
        return fractalShift;
    }

    public void setFractalShift(double fractalShift) {
        this.fractalShift = fractalShift;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public void setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
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
