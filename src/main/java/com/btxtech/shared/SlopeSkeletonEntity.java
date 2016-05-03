package com.btxtech.shared;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.AbstractBorder;
import com.btxtech.client.terrain.slope.Mesh;
import com.btxtech.client.terrain.slope.VerticalSegment;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

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
 * 18.04.2016.
 */
@Portable
@Entity
@Table(name = "SLOPE_SKELETON")
public class SlopeSkeletonEntity {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<SlopeSkeletonEntry> slopeSkeletonEntries;
    private int segmentCount;
    private int rowCount;
    private int width;
    private int height;
    private int verticalSpace;
    private double slopeGroundSplattingBumpDepth;
    private double slopeFactorDistance;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;

    public Long getId() {
        return id;
    }

    public void setValues(List<SlopeSkeletonEntry> slopeSkeletonEntries, int width, int height, int segmentCount, int rowCount, SlopeConfigEntity slopeConfigEntity) {
        this.slopeSkeletonEntries = slopeSkeletonEntries;
        this.width = width;
        this.height = height;
        this.segmentCount = segmentCount;
        this.rowCount = rowCount;
        setValues(slopeConfigEntity);
    }

    public void setValues(SlopeConfigEntity slopeConfigEntity) {
        verticalSpace = slopeConfigEntity.getVerticalSpace();
        slopeGroundSplattingBumpDepth = slopeConfigEntity.getSlopeGroundSplattingBumpDepth();
        slopeFactorDistance = slopeConfigEntity.getSlopeFactorDistance();
        bumpMapDepth = slopeConfigEntity.getBumpMapDepth();
        specularIntensity = slopeConfigEntity.getSpecularIntensity();
        specularHardness = slopeConfigEntity.getSpecularHardness();

    }

    public void generateMesh(Mesh mesh, List<AbstractBorder> skeleton, List<Index> innerLineMeshIndex, List<Index> outerLineMeshIndex, GroundMesh groundMesh) {
        SlopeSkeletonEntry[][] nodes = new SlopeSkeletonEntry[segmentCount][rowCount];
        for (SlopeSkeletonEntry slopeSkeletonEntry : slopeSkeletonEntries) {
            nodes[slopeSkeletonEntry.getColumnIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry;
        }

        int templateSegment = 0;
        int meshColumn = 0;
        for (AbstractBorder abstractBorder : skeleton) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < rowCount; row++) {
                    SlopeSkeletonEntry slopeSkeletonEntry = nodes[templateSegment][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeSkeletonEntry.getPosition(), 1.0);
                    float splatting = setupSplatting(transformedPoint, slopeSkeletonEntry.getSlopeFactor(), groundMesh);
                    mesh.addVertex(meshColumn, row, transformedPoint, setupSlopeFactor(slopeSkeletonEntry), splatting);
                    if (row == 0) {
                        outerLineMeshIndex.add(new Index(meshColumn, row));
                    } else if (row + 1 == rowCount) {
                        innerLineMeshIndex.add(new Index(meshColumn, row));
                    }
                }
                templateSegment++;
                if (templateSegment >= segmentCount) {
                    templateSegment = 0;
                }
                meshColumn++;
            }
        }
    }

    private float setupSlopeFactor(SlopeSkeletonEntry slopeSkeletonEntry) {
        if (MathHelper.compareWithPrecision(1.0, slopeSkeletonEntry.getSlopeFactor())) {
            return 1;
        } else if (MathHelper.compareWithPrecision(0.0, slopeSkeletonEntry.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return slopeSkeletonEntry.getSlopeFactor();
    }

    private float setupSplatting(Vertex vertex, float slopeFactor, GroundMesh groundMesh) {
        return (float) groundMesh.getInterpolatedSplatting(vertex.toXY());
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getSlopeGroundSplattingBumpDepth() {
        return slopeGroundSplattingBumpDepth;
    }

    public void setSlopeGroundSplattingBumpDepth(double slopeGroundSplattingBumpDepth) {
        this.slopeGroundSplattingBumpDepth = slopeGroundSplattingBumpDepth;
    }

    public double getSlopeFactorDistance() {
        return slopeFactorDistance;
    }

    public void setSlopeFactorDistance(double slopeFactorDistance) {
        this.slopeFactorDistance = slopeFactorDistance;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeSkeletonEntity that = (SlopeSkeletonEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
