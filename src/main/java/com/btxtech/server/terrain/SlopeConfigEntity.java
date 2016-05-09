package com.btxtech.server.terrain;

import com.btxtech.shared.Shape;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeleton;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlopeSkeleton.Type type;
    private String internalName;
    private double slopeGroundSplattingBumpDepth;
    private double slopeFactorDistance;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;
    private double fractalShift;
    private double fractalRoughness;
    private int verticalSpace;
    private int segments;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<SlopeNodeEntity> slopeSkeletonEntries;

    public Long getId() {
        return id;
    }

    public SlopeSkeleton toSlopeSkeleton() {
        SlopeSkeleton slopeSkeleton = new SlopeSkeleton();
        slopeSkeleton.setId(id.intValue());
        slopeSkeleton.setSegments(segments);
        Shape shape = new Shape(toSlopeShapes());
        slopeSkeleton.setRows(shape.getVertexCount());
        slopeSkeleton.setWidth((int) shape.getDistance());
        slopeSkeleton.setHeight((int) shape.getZInner());
        slopeSkeleton.setVerticalSpace(verticalSpace);
        slopeSkeleton.setSlopeGroundSplattingBumpDepth(slopeGroundSplattingBumpDepth);
        slopeSkeleton.setSlopeFactorDistance(slopeFactorDistance);
        slopeSkeleton.setBumpMapDepth(bumpMapDepth);
        slopeSkeleton.setSpecularIntensity(specularIntensity);
        slopeSkeleton.setSpecularHardness(specularHardness);
        slopeSkeleton.setType(type);
        SlopeNode[][] slopeNodes = new SlopeNode[segments][shape.getVertexCount()];
        for (SlopeNodeEntity slopeSkeletonEntry : slopeSkeletonEntries) {
            slopeNodes[slopeSkeletonEntry.getSegmentIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry.toSlopeNode();
        }
        slopeSkeleton.setSlopeNodes(slopeNodes);
        return slopeSkeleton;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setId(id.intValue());
        slopeConfig.setFractalShift(fractalShift);
        slopeConfig.setFractalRoughness(fractalRoughness);
        slopeConfig.setInternalName(internalName);
        slopeConfig.setSlopeSkeleton(toSlopeSkeleton());
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
        fractalShift = slopeConfig.getFractalShift();
        fractalRoughness = slopeConfig.getFractalRoughness();
        type = slopeConfig.getSlopeSkeleton().getType();
        slopeGroundSplattingBumpDepth = slopeConfig.getSlopeSkeleton().getSlopeGroundSplattingBumpDepth();
        slopeFactorDistance = slopeConfig.getSlopeSkeleton().getSlopeFactorDistance();
        bumpMapDepth = slopeConfig.getSlopeSkeleton().getBumpMapDepth();
        specularIntensity = slopeConfig.getSlopeSkeleton().getSpecularIntensity();
        specularHardness = slopeConfig.getSlopeSkeleton().getSpecularHardness();
        verticalSpace = slopeConfig.getSlopeSkeleton().getVerticalSpace();
        segments = slopeConfig.getSlopeSkeleton().getSegments();
        slopeSkeletonEntries.clear();
        for (int x = 0; x < segments; x++) {
            for (int y = 0; y < shape.size(); y++) {
                SlopeNodeEntity slopeNodeEntity = new SlopeNodeEntity();
                slopeNodeEntity.fromSlopeNode(x, y, slopeConfig.getSlopeSkeleton().getSlopeNodes()[x][y]);
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
                ", shape=" + shape +
                ", internalName='" + internalName + '\'' +
                ", slopeGroundSplattingBumpDepth=" + slopeGroundSplattingBumpDepth +
                ", slopeFactorDistance=" + slopeFactorDistance +
                ", bumpMapDepth=" + bumpMapDepth +
                ", specularIntensity=" + specularIntensity +
                ", specularHardness=" + specularHardness +
                ", fractalShift=" + fractalShift +
                ", fractalRoughness=" + fractalRoughness +
                ", verticalSpace=" + verticalSpace +
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
