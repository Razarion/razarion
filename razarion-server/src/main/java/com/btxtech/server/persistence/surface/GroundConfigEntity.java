package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.LightConfigEmbeddable;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Embedded
    private LightConfigEmbeddable lightConfigEmbeddable;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity topTexture;
    private double topTextureScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity topBm;
    private double topBmScale;
    private double topBmDepth;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bottomTexture;
    private double bottomTextureScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bottomBm;
    private double bottomBmScale;
    private double bottomBmDepth;
    private double splattingFractalMin;
    private double splattingFractalMax;
    private double splattingFractalClampMin;
    private double splattingFractalClampMax;
    private double splattingFractalRoughness;
    private int splattingXCount;
    private int splattingYCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity splatting;
    private double splattingScale;
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

    public Integer getId() {
        return id;
    }

    public GroundConfig toGroundConfig() {
        GroundConfig groundConfig = new GroundConfig();
        groundConfig.setGroundSkeletonConfig(generateGroundSkeleton());
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

    public void fromGroundConfig(GroundConfig groundConfig, ImagePersistence imagePersistence) {
        topTexture = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getTopTextureId());
        topTextureScale = groundConfig.getGroundSkeletonConfig().getTopTextureScale();
        topBm = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getTopBmId());
        topBmScale = groundConfig.getGroundSkeletonConfig().getTopBmScale();
        topBmDepth = groundConfig.getGroundSkeletonConfig().getTopBmDepth();
        bottomTexture = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getBottomTextureId());
        bottomTextureScale = groundConfig.getGroundSkeletonConfig().getBottomTextureScale();
        bottomBm = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getBottomBmId());
        bottomBmScale = groundConfig.getGroundSkeletonConfig().getBottomBmScale();
        bottomBmDepth = groundConfig.getGroundSkeletonConfig().getBottomBmDepth();
        if(lightConfigEmbeddable == null) {
            lightConfigEmbeddable = new LightConfigEmbeddable();
        }
        lightConfigEmbeddable.fromLightConfig(groundConfig.getGroundSkeletonConfig().getLightConfig());
        splattingFractalMin = groundConfig.getSplattingFractalMin();
        splattingFractalMax = groundConfig.getSplattingFractalMax();
        splattingFractalClampMin = groundConfig.getSplattingFractalClampMin();
        splattingFractalClampMax = groundConfig.getSplattingFractalClampMax();
        splattingFractalRoughness = groundConfig.getSplattingFractalRoughness();
        splattingXCount = groundConfig.getGroundSkeletonConfig().getSplattingXCount();
        splattingYCount = groundConfig.getGroundSkeletonConfig().getSplattingYCount();
        if(splattings == null) {
            splattings = new ArrayList<>();
        }
        splattings.clear();
        for (int x = 0; x < splattingXCount; x++) {
            for (int y = 0; y < splattingYCount; y++) {
                splattings.add(new GroundSplattingEntity(x, y, groundConfig.getGroundSkeletonConfig().getSplattings()[x][y]));
            }
        }
        splatting = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getSplattingId());
        splattingScale = groundConfig.getGroundSkeletonConfig().getSplattingScale();
        heightFractalMin = groundConfig.getHeightFractalMin();
        heightFractalMax = groundConfig.getHeightFractalMax();
        heightFractalClampMin = groundConfig.getHeightFractalClampMin();
        heightFractalClampMax = groundConfig.getHeightFractalClampMax();
        heightFractalRoughness = groundConfig.getHeightFractalRoughness();
        heightXCount = groundConfig.getGroundSkeletonConfig().getHeightXCount();
        heightYCount = groundConfig.getGroundSkeletonConfig().getHeightYCount();
        if(heights == null) {
            heights = new ArrayList<>();
        }
        heights.clear();
        for (int x = 0; x < heightXCount; x++) {
            for (int y = 0; y < heightYCount; y++) {
                heights.add(new GroundHeightEntity(x, y, groundConfig.getGroundSkeletonConfig().getHeights()[x][y]));
            }
        }
    }

    public GroundSkeletonConfig generateGroundSkeleton() {
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setId(id);
        groundSkeletonConfig.setTopTextureId(PersistenceUtil.getImageIdSafe(topTexture));
        groundSkeletonConfig.setTopTextureScale(topTextureScale);
        groundSkeletonConfig.setTopBmId(PersistenceUtil.getImageIdSafe(topBm));
        groundSkeletonConfig.setTopBmScale(topBmScale);
        groundSkeletonConfig.setTopBmDepth(topBmDepth);
        groundSkeletonConfig.setBottomTextureId(PersistenceUtil.getImageIdSafe(bottomTexture));
        groundSkeletonConfig.setBottomTextureScale(bottomTextureScale);
        groundSkeletonConfig.setBottomBmId(PersistenceUtil.getImageIdSafe(bottomBm));
        groundSkeletonConfig.setBottomBmScale(bottomBmScale);
        groundSkeletonConfig.setBottomBmDepth(bottomBmDepth);
        groundSkeletonConfig.setLightConfig(lightConfigEmbeddable.toLightConfig());
        groundSkeletonConfig.setSplattingXCount(splattingXCount);
        groundSkeletonConfig.setSplattingYCount(splattingYCount);
        double[][] splattingNodes = new double[splattingXCount][splattingYCount];
        for (GroundSplattingEntity groundSplattingEntity : splattings) {
            splattingNodes[groundSplattingEntity.getXIndex()][groundSplattingEntity.getYIndex()] = groundSplattingEntity.getSplatting();
        }
        groundSkeletonConfig.setSplattings(splattingNodes);
        groundSkeletonConfig.setHeightXCount(heightXCount);
        groundSkeletonConfig.setHeightYCount(heightYCount);
        groundSkeletonConfig.setSplattingId(PersistenceUtil.getImageIdSafe(splatting));
        groundSkeletonConfig.setSplattingScale(splattingScale);
        double[][] heightNodes = new double[heightXCount][heightYCount];
        for (GroundHeightEntity groundHeightEntity : heights) {
            heightNodes[groundHeightEntity.getXIndex()][groundHeightEntity.getYIndex()] = groundHeightEntity.getHeight();
        }
        groundSkeletonConfig.setHeights(heightNodes);
        return groundSkeletonConfig;
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
