package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;

/**
 * Created by Beat
 * on 30.06.2017.
 */
public class QuadTreeAccess {
    public static TerrainSubNode getSubNode(DecimalPosition relativePosition, TerrainSubNode[][] terrainSubNodes) {
        return getSubNode(relativePosition, 0, terrainSubNodes);
    }

    private static TerrainSubNode getSubNode(DecimalPosition relativePosition, int depth, TerrainSubNode[][] terrainSubNodes) {
        int divisor = 1 << TerrainShapeSubNode.DEPTH - depth - 1;
        Index index = relativePosition.divide(divisor).toIndex();
        TerrainSubNode terrainSubNode = terrainSubNodes[index.getX()][index.getY()];
        if (terrainSubNode == null) {
            return null;
        }
        if (terrainSubNode.getTerrainSubNodes() == null) {
            return terrainSubNode;
        }
        TerrainSubNode terrainSubSubNode = getSubNode(relativePosition, depth + 1, terrainSubNode.getTerrainSubNodes());
        if (terrainSubSubNode != null) {
            return terrainSubSubNode;
        } else {
            return terrainSubNode;
        }
    }
}
