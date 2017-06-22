package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 18.06.2017.
 */
public class TerrainShapeTile {
    static private Logger logger = Logger.getLogger(TerrainShapeTile.class.getName());

    public interface TerrainShapeNodeConsumer {
        /**
         * @param nodeRelativeIndex the relative node index in the tile. 0,0 is bottom left . TerrainUtil.TERRAIN_TILE_NODES_COUNT is top or left
         * @param terrainShapeNode  TerrainShapeNode
         * @param iterationControl  can stop the iteration
         */
        void onTerrainShapeNode(Index nodeRelativeIndex, TerrainShapeNode terrainShapeNode, IterationControl iterationControl);
    }

    public static class IterationControl {
        private boolean stop;

        public boolean isStop() {
            return stop;
        }

        public void doStop() {
            stop = true;
        }
    }

    private TerrainShapeNode[][] terrainShapeNodes;
    private Boolean water;
    private Double uniformGroundHeight;
    private List<FractionalSlope> fractionalSlopes;

    public boolean isLand() {
        return water == null || !water;
    }

    public boolean hasNodes() {
        return terrainShapeNodes != null;
    }

    public double getUniformGroundHeight() {
        if (uniformGroundHeight != null) {
            return uniformGroundHeight;
        } else {
            return 0;
        }
    }

    public TerrainShapeNode getTerrainShapeNode(Index nodeRelativeIndex) {
        if (!hasNodes()) {
            return null;
        }
        if (nodeRelativeIndex.getX() < 0) {
            throw new IllegalArgumentException("nodeRelativeIndex X < 0: " + nodeRelativeIndex);
        }
        if (nodeRelativeIndex.getY() < 0) {
            throw new IllegalArgumentException("nodeRelativeIndex Y < 0: " + nodeRelativeIndex);
        }
        if (nodeRelativeIndex.getX() >= TerrainUtil.TERRAIN_TILE_NODES_COUNT) {
            throw new IllegalArgumentException("nodeRelativeIndex X >= " + TerrainUtil.TERRAIN_TILE_NODES_COUNT + ": " + nodeRelativeIndex);
        }
        if (nodeRelativeIndex.getY() >= TerrainUtil.TERRAIN_TILE_NODES_COUNT) {
            throw new IllegalArgumentException("nodeRelativeIndex Y >= " + TerrainUtil.TERRAIN_TILE_NODES_COUNT + ": " + nodeRelativeIndex);
        }
        return terrainShapeNodes[nodeRelativeIndex.getX()][nodeRelativeIndex.getY()];
    }

    public double getLandWaterProportion() {
        throw new UnsupportedOperationException("");
    }

    public void iterateOverTerrainNodes(TerrainShapeNodeConsumer terrainShapeNodeConsumer) {
        IterationControl iterationControl = new IterationControl();
        for (int xNode = 0; xNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; xNode++) {
            for (int yNode = 0; yNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; yNode++) {
                Index nodeRelativeIndex = new Index(xNode, yNode);
                try {
                    TerrainShapeNode terrainShapeNode = null;
                    if (hasNodes()) {
                        terrainShapeNode = getTerrainShapeNode(nodeRelativeIndex);
                    }
                    terrainShapeNodeConsumer.onTerrainShapeNode(nodeRelativeIndex, terrainShapeNode, iterationControl);

                    if (iterationControl.isStop()) {
                        return;
                    }

                } catch (Throwable t) {
                    logger.severe("TerrainShapeTile.iterateOverTerrainNodes: " + t.getMessage() + " nodeRelativeIndex: " + nodeRelativeIndex);
                    throw t;
                }
            }
        }
    }

    public List<FractionalSlope> getFractionalSlopes() {
        return fractionalSlopes;
    }
}
