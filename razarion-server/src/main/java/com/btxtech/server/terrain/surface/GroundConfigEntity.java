package com.btxtech.server.terrain.surface;

import com.btxtech.server.terrain.LightConfigEmbeddable;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeleton;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
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
    @Embedded
    private LightConfigEmbeddable lightConfigEmbeddable;
    private double bumpMapDepth;
    private double splattingDistance;
    private double splattingFractalMin;
    private double splattingFractalMax;
    private double splattingFractalClampMin;
    private double splattingFractalClampMax;
    private double splattingFractalRoughness;
    private int splattingXCount;
    private int splattingYCount;
    private double heightFractalMin;
    private double heightFractalMax;
    private double heightFractalClampMin;
    private double heightFractalClampMax;
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
        groundConfig.setHeightFractalMin(heightFractalMin);
        groundConfig.setHeightFractalMax(heightFractalMax);
        groundConfig.setHeightFractalClampMin(heightFractalClampMin);
        groundConfig.setHeightFractalClampMax(heightFractalClampMax);
        groundConfig.setSplattingFractalRoughness(splattingFractalRoughness);
        groundConfig.setSplattingFractalMin(splattingFractalMin);
        groundConfig.setSplattingFractalMax(splattingFractalMax);
        groundConfig.setSplattingFractalClampMin(splattingFractalClampMin);
        groundConfig.setSplattingFractalClampMax(splattingFractalClampMax);
        return groundConfig;
    }

    public void fromGroundConfig(GroundConfig groundConfig) {
        bumpMapDepth = groundConfig.getGroundSkeleton().getBumpMapDepth();
        lightConfigEmbeddable.fromLightConfig(groundConfig.getGroundSkeleton().getLightConfig());
        splattingDistance = groundConfig.getGroundSkeleton().getSplattingDistance();
        splattingFractalMin = groundConfig.getSplattingFractalMin();
        splattingFractalMax = groundConfig.getSplattingFractalMax();
        splattingFractalClampMin = groundConfig.getSplattingFractalClampMin();
        splattingFractalClampMax = groundConfig.getSplattingFractalClampMax();
        splattingFractalRoughness = groundConfig.getSplattingFractalRoughness();
        splattingXCount = groundConfig.getGroundSkeleton().getSplattingXCount();
        splattingYCount = groundConfig.getGroundSkeleton().getSplattingYCount();
        splattings.clear();
        for (int x = 0; x < splattingXCount; x++) {
            for (int y = 0; y < splattingYCount; y++) {
                splattings.add(new GroundSplattingEntity(x, y, groundConfig.getGroundSkeleton().getSplattings()[x][y]));
            }
        }
        heightFractalMin= groundConfig.getHeightFractalMin();
        heightFractalMax= groundConfig.getHeightFractalMax();
        heightFractalClampMin= groundConfig.getHeightFractalClampMin();
        heightFractalClampMax= groundConfig.getHeightFractalClampMax();
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
        groundSkeleton.setLightConfig(lightConfigEmbeddable.toLightConfig());
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
