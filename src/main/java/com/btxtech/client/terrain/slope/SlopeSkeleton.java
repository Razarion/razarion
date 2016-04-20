package com.btxtech.client.terrain.slope;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 18.04.2016.
 */
public class SlopeSkeleton {
    private SlopeSkeletonEntry[][] nodes;
    private int segments;
    private int rows;
    private int width;
    private int height;

    public SlopeSkeleton(SlopeSkeletonEntry[][] nodes, int width, int height) {
        this.nodes = nodes;
        this.width = width;
        this.height = height;
        segments = nodes.length;
        rows = nodes[0].length;
    }

    public void generateMesh(Mesh mesh, List<AbstractBorder> skeleton, List<Index> innerLineMeshIndex, List<Index> outerLineMeshIndex, GroundMesh groundMeshSplatting) {
        int templateSegment = 0;
        int meshColumn = 0;
        for (AbstractBorder abstractBorder : skeleton) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < rows; row++) {
                    SlopeSkeletonEntry slopeSkeletonEntry = nodes[templateSegment][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeSkeletonEntry.getPosition(), 1.0);
                    float splatting = setupSplatting(transformedPoint, slopeSkeletonEntry.getSlopeFactor(), groundMeshSplatting);
                    mesh.addVertex(meshColumn, row, transformedPoint, setupSlopeFactor(slopeSkeletonEntry), splatting);
                    if (row == 0) {
                        outerLineMeshIndex.add(new Index(meshColumn, row));
                    } else if (row + 1 == rows) {
                        innerLineMeshIndex.add(new Index(meshColumn, row));
                    }
                }
                templateSegment++;
                if (templateSegment >= segments) {
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
        return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        // return shapeTemplateEntry.getSlopeFactor();
    }

    private float setupSplatting(Vertex vertex, float slopeFactor, GroundMesh groundMesh) {
        if (MathHelper.compareWithPrecision(1.0, slopeFactor)) {
            return 0;
        } else {
            return (float) groundMesh.getInterpolatedSplatting(vertex.toXY());
        }
    }

    public int getRows() {
        return rows;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
