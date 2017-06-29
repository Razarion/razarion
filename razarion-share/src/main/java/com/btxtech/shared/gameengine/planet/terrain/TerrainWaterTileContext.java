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
    private int waterNodeCount;

    public void init(TerrainTileContext terrainTileContext) {
        this.terrainTileContext = terrainTileContext;
    }

    public void insertNode(Index nodeIndex, double waterLevel) {
        Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
        // Triangle 1
        triangleCorners.add(new Vertex(rect.cornerBottomLeft(), waterLevel));
        triangleCorners.add(new Vertex(rect.cornerBottomRight(), waterLevel));
        triangleCorners.add(new Vertex(rect.cornerTopLeft(), waterLevel));
        // Triangle 2
        triangleCorners.add(new Vertex(rect.cornerBottomRight(), waterLevel));
        triangleCorners.add(new Vertex(rect.cornerTopRight(), waterLevel));
        triangleCorners.add(new Vertex(rect.cornerTopLeft(), waterLevel));
        waterNodeCount++;
    }

    public void insertWaterRim(Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        triangleCorners.add(vertexA);
        triangleCorners.add(vertexB);
        triangleCorners.add(vertexC);
    }

    public void complete() {
        if (triangleCorners.isEmpty()) {
            return;
        }
        TerrainWaterTile terrainWaterTile = jsInteropObjectFactory.generateTerrainWaterTile();
        terrainWaterTile.initArray(triangleCorners.size() * Vertex.getComponentsPerVertex());
        for (int i = 0; i < triangleCorners.size(); i++) {
            Vertex vertex = triangleCorners.get(i);
            terrainWaterTile.setTriangleCorner(i, vertex.getX(), vertex.getY(), vertex.getZ());
        }
        terrainWaterTile.setVertexCount(triangleCorners.size());
        terrainTileContext.setTerrainWaterTile(terrainWaterTile);
    }

    public int getWaterNodeCount() {
        return waterNodeCount;
    }
}
