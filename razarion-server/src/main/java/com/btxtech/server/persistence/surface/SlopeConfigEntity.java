package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.LightConfigEmbeddable;
import com.btxtech.shared.Shape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
    @GeneratedValue
    private Long id;
    private String internalName;
    @Embedded
    private LightConfigEmbeddable lightConfigEmbeddable;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlopeSkeletonConfig.Type type;
    private double bumpMapDepth;
    private double fractalMin;
    private double fractalMax;
    private double fractalClampMin;
    private double fractalClampMax;
    private double fractalRoughness;
    private int verticalSpace;
    private int segments;
    private boolean slopeOriented;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<SlopeNodeEntity> slopeSkeletonEntries;

    public Long getId() {
        return id;
    }

    public SlopeSkeletonConfig toSlopeSkeleton() {
        SlopeSkeletonConfig slopeSkeletonConfig = new SlopeSkeletonConfig();
        slopeSkeletonConfig.setId(id.intValue());
        slopeSkeletonConfig.setSegments(segments);
        Shape shape = new Shape(toSlopeShapes());
        slopeSkeletonConfig.setLightConfig(lightConfigEmbeddable.toLightConfig());
        slopeSkeletonConfig.setRows(shape.getVertexCount());
        slopeSkeletonConfig.setWidth((int) shape.getDistance());
        slopeSkeletonConfig.setHeight((int) shape.getZInner());
        slopeSkeletonConfig.setVerticalSpace(verticalSpace);
        slopeSkeletonConfig.setBumpMapDepth(bumpMapDepth);
        slopeSkeletonConfig.setType(type);
        slopeSkeletonConfig.setSlopeOriented(slopeOriented);
        SlopeNode[][] slopeNodes = new SlopeNode[segments][shape.getVertexCount()];
        for (SlopeNodeEntity slopeSkeletonEntry : slopeSkeletonEntries) {
            slopeNodes[slopeSkeletonEntry.getSegmentIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry.toSlopeNode();
        }
        slopeSkeletonConfig.setSlopeNodes(slopeNodes);
        return slopeSkeletonConfig;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setId(id.intValue());
        slopeConfig.setFractalMin(fractalMin);
        slopeConfig.setFractalMax(fractalMax);
        slopeConfig.setFractalClampMin(fractalClampMin);
        slopeConfig.setFractalClampMax(fractalClampMax);
        slopeConfig.setFractalRoughness(fractalRoughness);
        slopeConfig.setInternalName(internalName);
        slopeConfig.setSlopeSkeletonConfig(toSlopeSkeleton());
        slopeConfig.setShape(toSlopeShapes());
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig) {
        shape.clear();
        for (SlopeShape slopeShape : slopeConfig.getShape()) {
            SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
            slopeShapeEntity.fromSlopeShape(slopeShape);
            shape.add(slopeShapeEntity);
        }
        internalName = slopeConfig.getInternalName();
        lightConfigEmbeddable.fromLightConfig(slopeConfig.getSlopeSkeletonConfig().getLightConfig());
        fractalMin = slopeConfig.getFractalMin();
        fractalMax = slopeConfig.getFractalMax();
        fractalClampMin = slopeConfig.getFractalClampMin();
        fractalClampMax = slopeConfig.getFractalClampMax();
        fractalRoughness = slopeConfig.getFractalRoughness();
        type = slopeConfig.getSlopeSkeletonConfig().getType();
        bumpMapDepth = slopeConfig.getSlopeSkeletonConfig().getBumpMapDepth();
        verticalSpace = slopeConfig.getSlopeSkeletonConfig().getVerticalSpace();
        slopeOriented = slopeConfig.getSlopeSkeletonConfig().getSlopeOriented();
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
                ", lightConfigEmbeddable=" + lightConfigEmbeddable +
                ", shape=" + shape +
                ", bumpMapDepth=" + bumpMapDepth +
                ", fractalMin=" + fractalMin +
                ", fractalMax=" + fractalMax +
                ", fractalClampMin=" + fractalClampMin +
                ", fractalClampMax=" + fractalClampMax +
                ", fractalRoughness=" + fractalRoughness +
                ", verticalSpace=" + verticalSpace +
                ", slopeOriented=" + slopeOriented +
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
