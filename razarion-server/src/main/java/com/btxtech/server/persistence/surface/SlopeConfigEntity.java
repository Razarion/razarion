package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.SpecularLightConfigEmbeddable;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Embedded
    private SpecularLightConfigEmbeddable specularLightConfigEmbeddable;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlopeSkeletonConfig.Type type;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private double fractalMin;
    private double fractalMax;
    private double fractalClampMin;
    private double fractalClampMax;
    private double fractalRoughness;
    private double horizontalSpace;
    private int segments;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<SlopeNodeEntity> slopeSkeletonEntries;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double textureScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bm;
    private double bmDepth;

    public Integer getId() {
        return id;
    }

    public SlopeSkeletonConfig toSlopeSkeleton() {
        SlopeSkeletonConfig slopeSkeletonConfig = new SlopeSkeletonConfig();
        slopeSkeletonConfig.setId(id);
        slopeSkeletonConfig.setInternalName(internalName);
        slopeSkeletonConfig.setSegments(segments);
        slopeSkeletonConfig.setInnerLineGameEngine(innerLineGameEngine);
        slopeSkeletonConfig.setCoastDelimiterLineGameEngine(coastDelimiterLineGameEngine);
        slopeSkeletonConfig.setOuterLineGameEngine(outerLineGameEngine);
        Shape shape = new Shape(toSlopeShapes());
        slopeSkeletonConfig.setSpecularLightConfig(specularLightConfigEmbeddable.toLightConfig());
        slopeSkeletonConfig.setRows(shape.getVertexCount());
        slopeSkeletonConfig.setWidth(shape.getDistance());
        slopeSkeletonConfig.setHeight(shape.getZInner());
        slopeSkeletonConfig.setHorizontalSpace(horizontalSpace);
        slopeSkeletonConfig.setType(type);
        slopeSkeletonConfig.setSlopeTextureId(PersistenceUtil.getImageIdSafe(texture));
        slopeSkeletonConfig.setSlopeTextureScale(textureScale);
        slopeSkeletonConfig.setSlopeBumpMapId(PersistenceUtil.getImageIdSafe(bm));
        slopeSkeletonConfig.setSlopeBumpMapDepth(bmDepth);
        SlopeNode[][] slopeNodes = new SlopeNode[segments][shape.getVertexCount()];
        for (SlopeNodeEntity slopeSkeletonEntry : slopeSkeletonEntries) {
            slopeNodes[slopeSkeletonEntry.getSegmentIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry.toSlopeNode();
        }
        slopeSkeletonConfig.setSlopeNodes(slopeNodes);
        // TODO --------------
        slopeSkeletonConfig.setSlopeWaterSplattingId(20).setSlopeWaterSplattingScale(0.1).setSlopeWaterSplattingFactor(0.5).setSlopeWaterSplattingHeight(-0.22).setSlopeWaterSplattingFadeThreshold(0.2);
        // TODO ends --------------
        return slopeSkeletonConfig;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setId(id);
        slopeConfig.setFractalMin(fractalMin);
        slopeConfig.setFractalMax(fractalMax);
        slopeConfig.setFractalClampMin(fractalClampMin);
        slopeConfig.setFractalClampMax(fractalClampMax);
        slopeConfig.setFractalRoughness(fractalRoughness);
        slopeConfig.setInternalName(internalName);
        slopeConfig.setSlopeSkeletonConfig(toSlopeSkeleton());
        slopeConfig.setSlopeShapes(toSlopeShapes());
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig, ImagePersistence imagePersistence) {
        shape.clear();
        for (SlopeShape slopeShape : slopeConfig.getSlopeShapes()) {
            SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
            slopeShapeEntity.fromSlopeShape(slopeShape);
            shape.add(slopeShapeEntity);
        }
        internalName = slopeConfig.getInternalName();
        specularLightConfigEmbeddable.fromLightConfig(slopeConfig.getSlopeSkeletonConfig().getSpecularLightConfig());
        fractalMin = slopeConfig.getFractalMin();
        fractalMax = slopeConfig.getFractalMax();
        fractalClampMin = slopeConfig.getFractalClampMin();
        fractalClampMax = slopeConfig.getFractalClampMax();
        fractalRoughness = slopeConfig.getFractalRoughness();
        type = slopeConfig.getSlopeSkeletonConfig().getType();
        innerLineGameEngine = slopeConfig.getSlopeSkeletonConfig().getInnerLineGameEngine();
        coastDelimiterLineGameEngine = slopeConfig.getSlopeSkeletonConfig().getCoastDelimiterLineGameEngine();
        outerLineGameEngine = slopeConfig.getSlopeSkeletonConfig().getOuterLineGameEngine();
        texture = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeSkeletonConfig().getSlopeTextureId());
        textureScale = slopeConfig.getSlopeSkeletonConfig().getSlopeTextureScale();
        bm = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeSkeletonConfig().getSlopeBumpMapId());
        bmDepth = slopeConfig.getSlopeSkeletonConfig().getSlopeBumpMapDepth();
        horizontalSpace = slopeConfig.getSlopeSkeletonConfig().getHorizontalSpace();
        segments = slopeConfig.getSlopeSkeletonConfig().getSegments();
        slopeSkeletonEntries.clear();
        for (int x = 0; x < segments; x++) {
            for (int y = 0; y < shape.size(); y++) {
                SlopeNodeEntity slopeNodeEntity = new SlopeNodeEntity();
                slopeNodeEntity.fromSlopeNode(x, y, slopeConfig.getSlopeSkeletonConfig().getSlopeNodes()[x][y]);
                slopeSkeletonEntries.add(slopeNodeEntity);
            }
        }
    }

    public void setDefault() {
        segments = 1;
        horizontalSpace = 0.5;
        type = SlopeSkeletonConfig.Type.LAND;
        shape = new ArrayList<>();
        shape.add(new SlopeShapeEntity().setPosition(new DecimalPosition(0, 0)).setSlopeFactor(1));
        shape.add(new SlopeShapeEntity().setPosition(new DecimalPosition(1, 1)).setSlopeFactor(1));
        specularLightConfigEmbeddable = new SpecularLightConfigEmbeddable();
        slopeSkeletonEntries = new ArrayList<>();
    }

    private List<SlopeShape> toSlopeShapes() {
        List<SlopeShape> slopeShapes = new ArrayList<>();
        for (SlopeShapeEntity slopeShapeEntity : shape) {
            slopeShapes.add(slopeShapeEntity.toSlopeShape());
        }
        return slopeShapes;
    }

    @Override
    public String toString() {
        return "SlopeConfigEntity{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                ", specularLightConfigEmbeddable=" + specularLightConfigEmbeddable +
                ", shape=" + shape +
                ", bmDepth=" + bmDepth +
                ", fractalMin=" + fractalMin +
                ", fractalMax=" + fractalMax +
                ", fractalClampMin=" + fractalClampMin +
                ", fractalClampMax=" + fractalClampMax +
                ", fractalRoughness=" + fractalRoughness +
                ", horizontalSpace=" + horizontalSpace +
                ", segments=" + segments +
                ", type=" + type +
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
