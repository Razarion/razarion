package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.SpecularLightConfigEmbeddable;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig_OLD;

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
    private SlopeConfig.Type type;
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

    public SlopeConfig toSlopeSkeleton() {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setId(id);
        slopeConfig.setInternalName(internalName);
        slopeConfig.setSegments(segments);
        slopeConfig.setInnerLineGameEngine(innerLineGameEngine);
        slopeConfig.setCoastDelimiterLineGameEngine(coastDelimiterLineGameEngine);
        slopeConfig.setOuterLineGameEngine(outerLineGameEngine);
        Shape shape = new Shape(toSlopeShapes());
        slopeConfig.setSpecularLightConfig(specularLightConfigEmbeddable.toLightConfig());
        slopeConfig.setRows(shape.getVertexCount());
        slopeConfig.setWidth(shape.getDistance());
        slopeConfig.setHeight(shape.getZInner());
        slopeConfig.setHorizontalSpace(horizontalSpace);
        slopeConfig.setType(type);
        slopeConfig.setSlopeTextureId(PersistenceUtil.getImageIdSafe(texture));
        slopeConfig.setSlopeTextureScale(textureScale);
        slopeConfig.setSlopeBumpMapId(PersistenceUtil.getImageIdSafe(bm));
        slopeConfig.setSlopeBumpMapDepth(bmDepth);
        SlopeNode[][] slopeNodes = new SlopeNode[segments][shape.getVertexCount()];
        for (SlopeNodeEntity slopeSkeletonEntry : slopeSkeletonEntries) {
            slopeNodes[slopeSkeletonEntry.getSegmentIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry.toSlopeNode();
        }
        slopeConfig.setSlopeNodes(slopeNodes);
        // TODO --------------
        slopeConfig.setSlopeWaterSplattingId(20).setSlopeWaterSplattingScale(0.1).setSlopeWaterSplattingFactor(0.5).setSlopeWaterSplattingHeight(-0.22).setSlopeWaterSplattingFadeThreshold(0.2);
        // TODO ends --------------
        return slopeConfig;
    }

    public SlopeConfig_OLD toSlopeConfig() {
        SlopeConfig_OLD slopeConfigOLD = new SlopeConfig_OLD();
        slopeConfigOLD.setId(id);
        slopeConfigOLD.setFractalMin(fractalMin);
        slopeConfigOLD.setFractalMax(fractalMax);
        slopeConfigOLD.setFractalClampMin(fractalClampMin);
        slopeConfigOLD.setFractalClampMax(fractalClampMax);
        slopeConfigOLD.setFractalRoughness(fractalRoughness);
        slopeConfigOLD.setInternalName(internalName);
        slopeConfigOLD.setSlopeConfig(toSlopeSkeleton());
        // TODO  slopeConfigOLD.setSlopeShapes(toSlopeShapes());
        return slopeConfigOLD;
    }

    public void fromSlopeConfig(SlopeConfig_OLD slopeConfigOLD, ImagePersistence imagePersistence) {
        shape.clear();
        // TODO  for (SlopeShape slopeShape : slopeConfigOLD.getSlopeShapes()) {
        // TODO     SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
        // TODO     slopeShapeEntity.fromSlopeShape(slopeShape);
        // TODO     shape.add(slopeShapeEntity);
        // TODO  }
        internalName = slopeConfigOLD.getInternalName();
        specularLightConfigEmbeddable.fromLightConfig(slopeConfigOLD.getSlopeConfig().getSpecularLightConfig());
        fractalMin = slopeConfigOLD.getFractalMin();
        fractalMax = slopeConfigOLD.getFractalMax();
        fractalClampMin = slopeConfigOLD.getFractalClampMin();
        fractalClampMax = slopeConfigOLD.getFractalClampMax();
        fractalRoughness = slopeConfigOLD.getFractalRoughness();
        type = slopeConfigOLD.getSlopeConfig().getType();
        innerLineGameEngine = slopeConfigOLD.getSlopeConfig().getInnerLineGameEngine();
        coastDelimiterLineGameEngine = slopeConfigOLD.getSlopeConfig().getCoastDelimiterLineGameEngine();
        outerLineGameEngine = slopeConfigOLD.getSlopeConfig().getOuterLineGameEngine();
        texture = imagePersistence.getImageLibraryEntity(slopeConfigOLD.getSlopeConfig().getSlopeTextureId());
        textureScale = slopeConfigOLD.getSlopeConfig().getSlopeTextureScale();
        bm = imagePersistence.getImageLibraryEntity(slopeConfigOLD.getSlopeConfig().getSlopeBumpMapId());
        bmDepth = slopeConfigOLD.getSlopeConfig().getSlopeBumpMapDepth();
        horizontalSpace = slopeConfigOLD.getSlopeConfig().getHorizontalSpace();
        segments = slopeConfigOLD.getSlopeConfig().getSegments();
        slopeSkeletonEntries.clear();
        for (int x = 0; x < segments; x++) {
            for (int y = 0; y < shape.size(); y++) {
                SlopeNodeEntity slopeNodeEntity = new SlopeNodeEntity();
                slopeNodeEntity.fromSlopeNode(x, y, slopeConfigOLD.getSlopeConfig().getSlopeNodes()[x][y]);
                slopeSkeletonEntries.add(slopeNodeEntity);
            }
        }
    }

    public void setDefault() {
        segments = 1;
        horizontalSpace = 0.5;
        type = SlopeConfig.Type.LAND;
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
