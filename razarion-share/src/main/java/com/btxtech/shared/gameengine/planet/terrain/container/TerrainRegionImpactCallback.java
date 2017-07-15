package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * on 20.06.2017.
 */
public interface TerrainRegionImpactCallback {
    interface Control {
        void doStop();

        boolean isStop();
    }

    /**
     * Called if no tile for terrain position
     *
     * @param tileIndex the index of the tile. Eg: 0,0 is the tile on bottom left
     * @return result
     */
    default void landNoTile(Index tileIndex) {
    }

    /**
     * Called if tile found on terrain position. The tile does not have any nodes. It is also full water or full land inside a plateau
     *
     * @param terrainShapeTile TerrainShapeTile
     * @param tileIndex        the index of the tile. Eg: 0,0 is the tile on bottom left
     * @return result
     */
    default void inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
    }

    /**
     * Called if node found on terrain position. The node does not have any sub nodes. It is also full water or full land inside a plateau
     *
     * @param terrainShapeNode  TerrainShapeNode
     * @param nodeRelativeIndex the relative node index in the tile. 0,0 is bottom left . TerrainUtil.TERRAIN_TILE_NODES_COUNT is top or left
     * @param tileIndex         the index of the tile. Eg: 0,0 is the tile on bottom left
     * @return result
     */
    default void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, Index tileIndex) {
    }

    default void inSubNode(TerrainShapeSubNode terrainShapeSubNode) {

    }
}
