package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
@Dependent
public class TerrainWaterTileContext {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    private TerrainTileContext terrainTileContext;
    private List<Vertex> triangleCorners = new ArrayList<>();
    private List<Double> offsetToOuterCorner = new ArrayList<>();
    private int waterNodeCount;

    public void init(TerrainTileContext terrainTileContext) {
        this.terrainTileContext = terrainTileContext;
    }

    public void insertNode(Index nodeIndex, double waterLevel, double[] offsetToOuter) {
        Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);


        Vertex bottomLeft = new Vertex(rect.cornerBottomLeft(), waterLevel);
        Vertex bottomRight = new Vertex(rect.cornerBottomRight(), waterLevel);
        Vertex topRight = new Vertex(rect.cornerTopRight(), waterLevel);
        Vertex topLeft = new Vertex(rect.cornerTopLeft(), waterLevel);
        Double offsetToOuterBottomLeft = offsetToOuter != null ? offsetToOuter[0] : null;
        Double offsetToOuterBottomRight = offsetToOuter != null ? offsetToOuter[1] : null;
        Double offsetToOuterTopRight = offsetToOuter != null ? offsetToOuter[2] : null;
        Double offsetToOuterTopLeft = offsetToOuter != null ? offsetToOuter[3] : null;

        if (!terrainTileContext.checkPlayGround(bottomLeft, bottomRight, topRight, topLeft)) {
            return;
        }

        // Triangle 1
        triangleCorners.add(bottomLeft);
        triangleCorners.add(bottomRight);
        triangleCorners.add(topLeft);
        offsetToOuterCorner.add(offsetToOuterBottomLeft);
        offsetToOuterCorner.add(offsetToOuterBottomRight);
        offsetToOuterCorner.add(offsetToOuterTopLeft);
        // Triangle 2
        triangleCorners.add(bottomRight);
        triangleCorners.add(topRight);
        triangleCorners.add(topLeft);
        offsetToOuterCorner.add(offsetToOuterBottomRight);
        offsetToOuterCorner.add(offsetToOuterTopRight);
        offsetToOuterCorner.add(offsetToOuterTopLeft);

        waterNodeCount++;
    }

    public void insertWaterRim(Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        if (!terrainTileContext.checkPlayGround(vertexA, vertexB, vertexC)) {
            return;
        }
        triangleCorners.add(vertexA);
        triangleCorners.add(vertexB);
        triangleCorners.add(vertexC);
        offsetToOuterCorner.add(null); // TODO fill
        offsetToOuterCorner.add(null); // TODO fill
        offsetToOuterCorner.add(null); // TODO fill
    }

    public void complete() {
        if (triangleCorners.isEmpty()) {
            return;
        }
        TerrainWaterTile terrainWaterTile = jsInteropObjectFactory.generateTerrainWaterTile();
        terrainWaterTile.initArray(triangleCorners.size() * Vertex.getComponentsPerVertex(), triangleCorners.size());
        for (int i = 0; i < triangleCorners.size(); i++) {
            Vertex vertex = triangleCorners.get(i);
            terrainWaterTile.setTriangleCorner(i, vertex.getX(), vertex.getY(), vertex.getZ(), offsetToOuterCorner.get(i));
        }
        terrainWaterTile.setVertexCount(triangleCorners.size());
        terrainTileContext.setTerrainWaterTile(terrainWaterTile);
    }

    public int getWaterNodeCount() {
        return waterNodeCount;
    }
}
