package com.btxtech.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Bindable
@Portable
@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity {
    @Id
    @GeneratedValue
    private Long id;
    private double splattingDistance;
    private double splattingFractalMin;
    private double splattingFractalMax;
    private double splattingFractalRoughness;
    private int splattingXCount;
    private int splattingYCount;
    private double bumpMapDepth;
    private double specularHardness;
    private double specularIntensity;
    private double heightFractalShift;
    private double heightFractalRoughness;
    private int heightXCount;
    private int heightYCount;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private GroundSkeletonEntity groundSkeletonEntity;

    public Long getId() {
        return id;
    }

    public double getSplattingDistance() {
        return splattingDistance;
    }

    public void setSplattingDistance(double splattingDistance) {
        this.splattingDistance = splattingDistance;
    }

    public double getSplattingFractalMin() {
        return splattingFractalMin;
    }

    public void setSplattingFractalMin(double splattingFractalMin) {
        this.splattingFractalMin = splattingFractalMin;
    }

    public double getSplattingFractalMax() {
        return splattingFractalMax;
    }

    public void setSplattingFractalMax(double splattingFractalMax) {
        this.splattingFractalMax = splattingFractalMax;
    }

    public double getSplattingFractalRoughness() {
        return splattingFractalRoughness;
    }

    public void setSplattingFractalRoughness(double splattingFractalRoughness) {
        this.splattingFractalRoughness = splattingFractalRoughness;
    }

    public int getSplattingXCount() {
        return splattingXCount;
    }

    public void setSplattingXCount(int splattingXCount) {
        this.splattingXCount = splattingXCount;
    }

    public int getSplattingYCount() {
        return splattingYCount;
    }

    public void setSplattingYCount(int splattingYCount) {
        this.splattingYCount = splattingYCount;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getHeightFractalShift() {
        return heightFractalShift;
    }

    public void setHeightFractalShift(double heightFractalShift) {
        this.heightFractalShift = heightFractalShift;
    }

    public double getHeightFractalRoughness() {
        return heightFractalRoughness;
    }

    public void setHeightFractalRoughness(double heightFractalRoughness) {
        this.heightFractalRoughness = heightFractalRoughness;
    }

    public int getHeightXCount() {
        return heightXCount;
    }

    public void setHeightXCount(int heightXCount) {
        this.heightXCount = heightXCount;
    }

    public int getHeightYCount() {
        return heightYCount;
    }

    public void setHeightYCount(int heightYCount) {
        this.heightYCount = heightYCount;
    }

    public GroundSkeletonEntity getGroundSkeletonEntity() {
        return groundSkeletonEntity;
    }

    public void setGroundSkeletonEntity(GroundSkeletonEntity groundSkeletonEntity) {
        this.groundSkeletonEntity = groundSkeletonEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundConfigEntity that = (GroundConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
