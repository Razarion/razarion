package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.SpecularLightConfigEmbeddable;
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
    private String internalName;
    @Embedded
    private SpecularLightConfigEmbeddable specularLightConfigEmbeddable;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity topTexture;
    private double topTextureScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bottomTexture;
    private double bottomTextureScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bottomBm;
    private double bottomBmScale;
    private double bottomBmDepth;
    private double splattingFadeThreshold;
    private double splattingOffset;
    private double splattingGroundBmMultiplicator;
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
        // TODO groundConfig.setSplattingFractalRoughness(splattingFractalRoughness);
        // TODO groundConfig.setSplattingFractalMin(splattingFractalMin);
        // TODO groundConfig.setSplattingFractalMax(splattingFractalMax);
        // TODO groundConfig.setSplattingFractalClampMin(splattingFractalClampMin);
        // TODO groundConfig.setSplattingFractalClampMax(splattingFractalClampMax);
        return groundConfig;
    }

    public void fromGroundConfig(GroundConfig groundConfig, ImagePersistence imagePersistence) {
        // TODO topTexture = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getTopTextureId());
        // TODO topTextureScale = groundConfig.getGroundSkeletonConfig().getTopTextureScale();
        // TODO bottomTexture = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getBottomTextureId());
        // TODO bottomTextureScale = groundConfig.getGroundSkeletonConfig().getBottomTextureScale();
        // TODO bottomBm = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getBottomBmId());
        // TODO bottomBmScale = groundConfig.getGroundSkeletonConfig().getBottomBmScale();
        // TODO bottomBmDepth = groundConfig.getGroundSkeletonConfig().getBottomBmDepth();
        if (specularLightConfigEmbeddable == null) {
            specularLightConfigEmbeddable = new SpecularLightConfigEmbeddable();
        }
        // TODO specularLightConfigEmbeddable.fromLightConfig(groundConfig.getGroundSkeletonConfig().getSpecularLightConfig());
        // TODO splattingFractalMin = groundConfig.getSplattingFractalMin();
        // TODO splattingFractalMax = groundConfig.getSplattingFractalMax();
        // TODO splattingFractalClampMin = groundConfig.getSplattingFractalClampMin();
        // TODO splattingFractalClampMax = groundConfig.getSplattingFractalClampMax();
        // TODO splattingFractalRoughness = groundConfig.getSplattingFractalRoughness();
        splattingFadeThreshold = groundConfig.getGroundSkeletonConfig().getSplattingFadeThreshold();
        splattingOffset = groundConfig.getGroundSkeletonConfig().getSplattingOffset();
        // TODO splattingGroundBmMultiplicator = groundConfig.getGroundSkeletonConfig().getSplattingGroundBmMultiplicator();
        // TODO splattingXCount = groundConfig.getGroundSkeletonConfig().getSplattingXCount();
        // TODO splattingYCount = groundConfig.getGroundSkeletonConfig().getSplattingYCount();
        if (splattings == null) {
            splattings = new ArrayList<>();
        }
        splattings.clear();
        for (int x = 0; x < splattingXCount; x++) {
            for (int y = 0; y < splattingYCount; y++) {
                // TODO  splattings.add(new GroundSplattingEntity(x, y, groundConfig.getGroundSkeletonConfig().getSplattings()[x][y]));
            }
        }
        // TODO splatting = imagePersistence.getImageLibraryEntity(groundConfig.getGroundSkeletonConfig().getSplatting().getId());
        // TODO splattingScale = groundConfig.getGroundSkeletonConfig().getSplatting().getScale();
        heightFractalMin = groundConfig.getHeightFractalMin();
        heightFractalMax = groundConfig.getHeightFractalMax();
        heightFractalClampMin = groundConfig.getHeightFractalClampMin();
        heightFractalClampMax = groundConfig.getHeightFractalClampMax();
        heightFractalRoughness = groundConfig.getHeightFractalRoughness();
        heightXCount = groundConfig.getGroundSkeletonConfig().getHeightXCount();
        heightYCount = groundConfig.getGroundSkeletonConfig().getHeightYCount();
        if (heights == null) {
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
        // TODO groundSkeletonConfig.setTopTextureId(PersistenceUtil.getImageIdSafe(topTexture));
        // TODO groundSkeletonConfig.setTopTextureScale(topTextureScale);
        groundSkeletonConfig.setSplattingFadeThreshold(splattingFadeThreshold);
        groundSkeletonConfig.setSplattingOffset(splattingOffset);
        // TODO groundSkeletonConfig.setSplattingGroundBmMultiplicator(splattingGroundBmMultiplicator);
        // TODO groundSkeletonConfig.setBottomTextureId(PersistenceUtil.getImageIdSafe(bottomTexture));
        // TODO groundSkeletonConfig.setBottomTextureScale(bottomTextureScale);
        // TODO groundSkeletonConfig.setBottomBmId(PersistenceUtil.getImageIdSafe(bottomBm));
        // TODO groundSkeletonConfig.setBottomBmScale(bottomBmScale);
        // TODO groundSkeletonConfig.setBottomBmDepth(bottomBmDepth);
        // TODO groundSkeletonConfig.setSpecularLightConfig(specularLightConfigEmbeddable.toLightConfig());
        // TODO groundSkeletonConfig.setSplattingXCount(splattingXCount);
        // TODO groundSkeletonConfig.setSplattingYCount(splattingYCount);
        // TODO double[][] splattingNodes = new double[splattingXCount][splattingYCount];
        // TODO for (GroundSplattingEntity groundSplattingEntity : splattings) {
        // TODO     splattingNodes[groundSplattingEntity.getXIndex()][groundSplattingEntity.getYIndex()] = groundSplattingEntity.getSplatting();
        // TODO }
        // TODO groundSkeletonConfig.setSplattings(splattingNodes);
        groundSkeletonConfig.setHeightXCount(heightXCount);
        groundSkeletonConfig.setHeightYCount(heightYCount);
        // TODO groundSkeletonConfig.setSplattingId(PersistenceUtil.getImageIdSafe(splatting));
        // TODO groundSkeletonConfig.setSplattingScale(splattingScale);
        // TODO double[][] heightNodes = new double[heightXCount][heightYCount];
        // TODO for (GroundHeightEntity groundHeightEntity : heights) {
        // TODO     heightNodes[groundHeightEntity.getXIndex()][groundHeightEntity.getYIndex()] = groundHeightEntity.getHeight();
        // TODO }
        // TODO groundSkeletonConfig.setHeights(heightNodes);
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
