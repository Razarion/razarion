package com.btxtech.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by Beat
 * 21.11.2015.
 */
@Portable
@Bindable
@Entity
@Table(name = "SLOPE_CONFIG")
public class SlopeConfigEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private SlopeSkeletonEntity slopeSkeletonEntity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlopeSkeletonEntity.Type type;
    private String internalName;
    private double slopeGroundSplattingBumpDepth;
    private double slopeFactorDistance;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;
    private double fractalShift;
    private double fractalRoughness;
    private int verticalSpace;
    private int segments;

    public Long getId() {
        return id;
    }

    public boolean hasId() {
        return id != null;
    }

    public List<SlopeShapeEntity> getShape() {
        return shape;
    }

    public void setShape(List<SlopeShapeEntity> shape) {
        this.shape = shape;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public double getSlopeGroundSplattingBumpDepth() {
        return slopeGroundSplattingBumpDepth;
    }

    public void setSlopeGroundSplattingBumpDepth(double slopeGroundSplattingBumpDepth) {
        this.slopeGroundSplattingBumpDepth = slopeGroundSplattingBumpDepth;
    }

    public double getSlopeFactorDistance() {
        return slopeFactorDistance;
    }

    public void setSlopeFactorDistance(double slopeFactorDistance) {
        this.slopeFactorDistance = slopeFactorDistance;
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

    public int getSegments() {
        return segments;
    }

    public void setSegments(int segments) {
        this.segments = segments;
    }

    public SlopeNameId createSlopeNameId() {
        return new SlopeNameId(id.intValue(), internalName);
    }

    public SlopeSkeletonEntity getSlopeSkeletonEntity() {
        return slopeSkeletonEntity;
    }

    public void setSlopeSkeletonEntity(SlopeSkeletonEntity slopeSkeletonEntity) {
        this.slopeSkeletonEntity = slopeSkeletonEntity;
    }

    public SlopeSkeletonEntity.Type getType() {
        return type;
    }

    public void setType(SlopeSkeletonEntity.Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SlopeConfigEntity{" +
                "id=" + id +
                ", shape=" + shape +
                ", internalName='" + internalName + '\'' +
                ", slopeGroundSplattingBumpDepth=" + slopeGroundSplattingBumpDepth +
                ", slopeFactorDistance=" + slopeFactorDistance +
                ", bumpMapDepth=" + bumpMapDepth +
                ", specularIntensity=" + specularIntensity +
                ", specularHardness=" + specularHardness +
                ", fractalShift=" + fractalShift +
                ", fractalRoughness=" + fractalRoughness +
                ", verticalSpace=" + verticalSpace +
                ", segments=" + segments +
                ", type=" + type +
                ", slopeSkeletonEntity=" + (slopeSkeletonEntity != null ? slopeSkeletonEntity.getId() : "-") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeConfigEntity that = (SlopeConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
