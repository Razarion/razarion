package com.btxtech.server.terrain.surface;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeleton;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity {
    @Id
    @GeneratedValue
    private Long id;
    private double bumpMapDepth;
    private double specularHardness;
    private double specularIntensity;
    private double splattingDistance;
    private double splattingFractalMin;
    private double splattingFractalMax;
    private double splattingFractalRoughness;
    private int splattingXCount;
    private int splattingYCount;
    private double heightFractalShift;
    private double heightFractalRoughness;
    private int heightXCount;
    private int heightYCount;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<GroundSplattingEntity> splattings;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<GroundHeightEntity> heights;

    public Long getId() {
        return id;
    }

    public GroundConfig toGroundConfig() {
        GroundConfig groundConfig = new GroundConfig();
        groundConfig.setGroundSkeleton(generateGroundSkeleton());
        groundConfig.setHeightFractalRoughness(heightFractalRoughness);
        groundConfig.setHeightFractalShift(heightFractalShift);
        groundConfig.setHeightFractalRoughness(heightFractalRoughness);
        groundConfig.setSplattingFractalRoughness(splattingFractalRoughness);
        groundConfig.setSplattingFractalMin(splattingFractalMin);
        groundConfig.setSplattingFractalMax(splattingFractalMax);
        return groundConfig;
    }

    public void fromGroundConfig(GroundConfig groundConfig) {
        bumpMapDepth = groundConfig.getGroundSkeleton().getBumpMapDepth();
        specularHardness = groundConfig.getGroundSkeleton().getSpecularHardness();
        specularIntensity = groundConfig.getGroundSkeleton().getSpecularIntensity();
        splattingDistance = groundConfig.getGroundSkeleton().getSplattingDistance();
        splattingFractalMin = groundConfig.getSplattingFractalMin();
        splattingFractalMax = groundConfig.getSplattingFractalMax();
        splattingFractalRoughness = groundConfig.getSplattingFractalRoughness();
        splattingXCount = groundConfig.getGroundSkeleton().getSplattingXCount();
        splattingYCount = groundConfig.getGroundSkeleton().getSplattingYCount();
        splattings.clear();
        for (int x = 0; x < splattingXCount; x++) {
            for (int y = 0; y < splattingYCount; y++) {
                splattings.add(new GroundSplattingEntity(x, y, groundConfig.getGroundSkeleton().getSplattings()[x][y]));
            }
        }
        heightFractalShift = groundConfig.getHeightFractalShift();
        heightFractalRoughness = groundConfig.getHeightFractalRoughness();
        heightXCount = groundConfig.getGroundSkeleton().getHeightXCount();
        heightYCount = groundConfig.getGroundSkeleton().getHeightYCount();
        heights.clear();
        for (int x = 0; x < heightXCount; x++) {
            for (int y = 0; y < heightYCount; y++) {
                heights.add(new GroundHeightEntity(x, y, groundConfig.getGroundSkeleton().getHeights()[x][y]));
            }
        }
    }

    public GroundSkeleton generateGroundSkeleton() {
        GroundSkeleton groundSkeleton = new GroundSkeleton();
        groundSkeleton.setId(id.intValue());
        groundSkeleton.setBumpMapDepth(bumpMapDepth);
        groundSkeleton.setSplattingDistance(splattingDistance);
        groundSkeleton.setSpecularHardness(specularHardness);
        groundSkeleton.setSpecularIntensity(specularIntensity);
        groundSkeleton.setSplattingXCount(splattingXCount);
        groundSkeleton.setSplattingYCount(splattingYCount);
        double[][] splattingNodes = new double[splattingXCount][splattingYCount];
        for (GroundSplattingEntity groundSplattingEntity : splattings) {
            splattingNodes[groundSplattingEntity.getXIndex()][groundSplattingEntity.getYIndex()] = groundSplattingEntity.getSplatting();
        }
        groundSkeleton.setSplattings(splattingNodes);
        groundSkeleton.setHeightXCount(heightXCount);
        groundSkeleton.setHeightYCount(heightYCount);
        double[][] heightNodes = new double[heightXCount][heightYCount];
        for (GroundHeightEntity groundHeightEntity : heights) {
            heightNodes[groundHeightEntity.getXIndex()][groundHeightEntity.getYIndex()] = groundHeightEntity.getHeight();
        }
        groundSkeleton.setHeights(heightNodes);
        return groundSkeleton;
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
