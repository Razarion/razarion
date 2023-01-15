package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;
import static com.btxtech.server.persistence.surface.PhongMaterialConfigEmbeddable.factorize;

/**
 * Created by Beat
 * 21.11.2015.
 */
@Entity
@Table(name = "SLOPE_CONFIG")
public class SlopeConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    private boolean interpolateNorm;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private GroundConfigEntity groundConfig;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private WaterConfigEntity waterConfig;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity threeJsMaterial;
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "shallowWaterTextureId")),
            @AssociationOverride(name = "distortion", joinColumns = @JoinColumn(name = "shallowWaterDistortionId")),
            @AssociationOverride(name = "stencil", joinColumns = @JoinColumn(name = "shallowWaterStencilId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "shallowWaterScale")),
            @AttributeOverride(name = "distortionStrength", column = @Column(name = "shallowWaterDistortionStrength")),
            @AttributeOverride(name = "durationSeconds", column = @Column(name = "shallowWaterDurationSeconds")),
    })
    @Embedded
    private ShallowWaterConfigEmbeddable shallowWaterConfig;
    @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "outerSplattingTextureId"))
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "outerSplattingScale")),
            @AttributeOverride(name = "impact", column = @Column(name = "outerSplattingImpact")),
            @AttributeOverride(name = "blur", column = @Column(name = "outerSplattingBlur")),
            @AttributeOverride(name = "offset", column = @Column(name = "outerSplattingOffset")),
    })
    @Embedded
    private SlopeSplattingConfigEmbeddable outerSplatting;
    @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "innerSplattingTextureId"))
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "innerSplattingScale")),
            @AttributeOverride(name = "impact", column = @Column(name = "innerSplattingImpact")),
            @AttributeOverride(name = "blur", column = @Column(name = "innerSplattingBlur")),
            @AttributeOverride(name = "offset", column = @Column(name = "innerSplattingOffset")),
    })
    @Embedded
    private SlopeSplattingConfigEmbeddable innerSplatting;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private double horizontalSpace;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity shallowWaterThreeJsMaterial;

    public Integer getId() {
        return id;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig()
                .id(id)
                .internalName(internalName)
                .horizontalSpace(horizontalSpace)
                .interpolateNorm(interpolateNorm)
                .innerLineGameEngine(innerLineGameEngine)
                .outerLineGameEngine(outerLineGameEngine)
                .coastDelimiterLineGameEngine(coastDelimiterLineGameEngine)
                .outerSlopeSplattingConfig(SlopeSplattingConfigEmbeddable.to(outerSplatting))
                .innerSlopeSplattingConfig(SlopeSplattingConfigEmbeddable.to(innerSplatting))
                .threeJsMaterial(extractId(threeJsMaterial, ThreeJsModelConfigEntity::getId))
                .shallowWaterThreeJsMaterial(extractId(shallowWaterThreeJsMaterial, ThreeJsModelConfigEntity::getId));
        if (groundConfig != null) {
            slopeConfig.setGroundConfigId(groundConfig.getId());
        }
        if (waterConfig != null) {
            slopeConfig.setWaterConfigId(waterConfig.getId());
        }
        if (shape != null && !shape.isEmpty()) {
            slopeConfig.setSlopeShapes(shape.stream().map(SlopeShapeEntity::toSlopeShape).collect(Collectors.toList()));
        }
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig, ImagePersistence imagePersistence, GroundConfigEntity groundConfigEntity, WaterConfigEntity waterConfigEntity, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = slopeConfig.getInternalName();
        innerLineGameEngine = slopeConfig.getInnerLineGameEngine();
        coastDelimiterLineGameEngine = slopeConfig.getCoastDelimiterLineGameEngine();
        outerLineGameEngine = slopeConfig.getOuterLineGameEngine();
        groundConfig = groundConfigEntity;
        if (shape == null) {
            shape = new ArrayList<>();
        }
        shape.clear();
        if (slopeConfig.getSlopeShapes() != null) {
            shape.addAll(slopeConfig.getSlopeShapes().stream().map(slopeShape -> {
                SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
                slopeShapeEntity.fromSlopeShape(slopeShape);
                return slopeShapeEntity;
            }).collect(Collectors.toList()));
        }
        interpolateNorm = slopeConfig.isInterpolateNorm();
        horizontalSpace = slopeConfig.getHorizontalSpace();
        waterConfig = waterConfigEntity;
        threeJsMaterial = threeJsModelCrudPersistence.getEntity(slopeConfig.getThreeJsMaterial());
        outerSplatting = SlopeSplattingConfigEmbeddable.factorize(slopeConfig.getOuterSlopeSplattingConfig(), imagePersistence);
        innerSplatting = SlopeSplattingConfigEmbeddable.factorize(slopeConfig.getInnerSlopeSplattingConfig(), imagePersistence);
        shallowWaterThreeJsMaterial = threeJsModelCrudPersistence.getEntity(slopeConfig.getShallowWaterThreeJsMaterial());
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
