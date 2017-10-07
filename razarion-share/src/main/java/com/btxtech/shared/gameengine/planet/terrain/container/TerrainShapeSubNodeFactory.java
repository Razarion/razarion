package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

/**
 * Created by Beat
 * on 05.10.2017.
 */
public class TerrainShapeSubNodeFactory {

    public void fillTerrainShapeSubNode(TerrainShapeNode terrainShapeNode, Rectangle2D terrainRect, Polygon2D terrainTypeRegion, TerrainType innerTerrainType, TerrainType outerTerrainType) {
        for (int y = 0; y < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; y += TerrainUtil.MIN_SUB_NODE_LENGTH) {
            for (int x = 0; x < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; x += TerrainUtil.MIN_SUB_NODE_LENGTH) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(new Index(x, y)).add(terrainRect.getStart());
                TerrainType terrainType = terrainTypeRegion.isInside(scanPosition) ? innerTerrainType : outerTerrainType;
                if(terrainType == null) {
                    continue;
                }
                int depth0Index = calculateArrayIndex(x, y, 0);
                int depth1Index = calculateArrayIndex(x, y, 1);
                int depth2Index = calculateArrayIndex(x, y, 2);
                TerrainShapeSubNode[] terrainShapeSubNodes0 = terrainShapeNode.getTerrainShapeSubNodes();
                if (terrainShapeSubNodes0 == null) {
                    terrainShapeSubNodes0 = new TerrainShapeSubNode[4];
                    terrainShapeSubNodes0[0] = new TerrainShapeSubNode(null, 0);
                    terrainShapeSubNodes0[1] = new TerrainShapeSubNode(null, 0);
                    terrainShapeSubNodes0[2] = new TerrainShapeSubNode(null, 0);
                    terrainShapeSubNodes0[3] = new TerrainShapeSubNode(null, 0);
                    terrainShapeNode.setTerrainShapeSubNodes(terrainShapeSubNodes0);
                }
                TerrainShapeSubNode terrainShapeSubNode0 = terrainShapeSubNodes0[depth0Index];
                TerrainShapeSubNode terrainShapeSubNode1 = getOrCreateTerrainShapeSubNode(terrainShapeSubNode0, 1, depth1Index);
                TerrainShapeSubNode terrainShapeSubNode2 = getOrCreateTerrainShapeSubNode(terrainShapeSubNode1, 2, depth2Index);
                terrainShapeSubNode2.setTerrainType(terrainType);
            }
        }
    }

    private int calculateArrayIndex(int x, int y, int depth) {
        int nodeLength = (int) TerrainUtil.calculateSubNodeLength(depth);
        int xPart = (x / nodeLength) % 2;
        int yPart = (y / nodeLength) % 2;
        int arrayIndex = xPart + yPart * 2;
        // swap tr, tl
        if (arrayIndex == 2) {
            arrayIndex = 3;
        } else if (arrayIndex == 3) {
            arrayIndex = 2;
        }
        return arrayIndex;
    }

    private TerrainShapeSubNode getOrCreateTerrainShapeSubNode(TerrainShapeSubNode parent, int depth, int arrayIndex) {
        TerrainShapeSubNode[] children = parent.getTerrainShapeSubNodes();
        if (children == null) {
            children = new TerrainShapeSubNode[4];
            children[0] = new TerrainShapeSubNode(parent, depth);
            children[1] = new TerrainShapeSubNode(parent, depth);
            children[2] = new TerrainShapeSubNode(parent, depth);
            children[3] = new TerrainShapeSubNode(parent, depth);
            parent.setTerrainShapeSubNodes(children);
        }
        return children[arrayIndex];
    }
}
