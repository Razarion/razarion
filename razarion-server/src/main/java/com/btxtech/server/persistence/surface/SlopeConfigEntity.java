package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "materialTextureId")),
            @AssociationOverride(name = "bumpMap", joinColumns = @JoinColumn(name = "materialBumpMapId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "materialScale")),
            @AttributeOverride(name = "bumpMapDepth", column = @Column(name = "materialBumpMapDepth")),
            @AttributeOverride(name = "shininess", column = @Column(name = "materialShininess")),
            @AttributeOverride(name = "specularStrength", column = @Column(name = "materialSpecularStrength")),
    })
    @Embedded
    private PhongMaterialConfigEmbeddable material;
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
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private double horizontalSpace;

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
                .shallowWaterConfig(ShallowWaterConfigEmbeddable.to(shallowWaterConfig));
        if (groundConfig != null) {
            slopeConfig.setGroundConfigId(groundConfig.getId());
        }
        if (waterConfig != null) {
            slopeConfig.setWaterConfigId(waterConfig.getId());
        }
        if (shape != null && !shape.isEmpty()) {
            slopeConfig.setSlopeShapes(shape.stream().map(SlopeShapeEntity::toSlopeShape).collect(Collectors.toList()));
        }
        if (material != null) {
            slopeConfig.setMaterial(material.to());
        }
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig, ImagePersistence imagePersistence, GroundConfigEntity groundConfigEntity, WaterConfigEntity waterConfigEntity) {
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
        material = factorize(slopeConfig.getMaterial(), imagePersistence);
        shallowWaterConfig = ShallowWaterConfigEmbeddable.factorize(slopeConfig.getShallowWaterConfig(), imagePersistence);
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
