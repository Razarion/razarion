package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;

import java.util.logging.Level;
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
    }

    private NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;

    public TerrainType getTerrainType() {
        return TerrainType.LAND;
    }

    public double getLandWaterProportion() {
        throw new UnsupportedOperationException(".... TODO .... TerrainShapeTile.getLandWaterProportion()");
    }

    public void iterateOverTerrainNodes(TerrainShapeNodeConsumer terrainShapeNodeConsumer) {
        IterationControl iterationControl = new IterationControl();
        for (int xNode = 0; xNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; xNode++) {
            for (int yNode = 0; yNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; yNode++) {
                Index nodeRelativeIndex = new Index(xNode, yNode);
                try {
                    TerrainShapeNode terrainShapeNode = null;
                    terrainShapeNodeConsumer.onTerrainShapeNode(nodeRelativeIndex, terrainShapeNode, iterationControl);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "TerrainShapeTile.iterateOverTerrainNodes: " + t.getMessage() + " nodeRelativeIndex: " + nodeRelativeIndex, t);
                }
            }
        }
    }

    public NativeTerrainShapeTile toNativeTerrainShapeTile() {
        NativeTerrainShapeTile nativeTerrainShapeTile = new NativeTerrainShapeTile();
        nativeTerrainShapeTile.nativeTerrainShapeObjectLists = nativeTerrainShapeObjectLists;
        return nativeTerrainShapeTile;
    }

    public NativeTerrainShapeObjectList[] getNativeTerrainShapeObjectLists() {
        return nativeTerrainShapeObjectLists;
    }

    public void setNativeTerrainShapeObjectLists(NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists) {
        this.nativeTerrainShapeObjectLists = nativeTerrainShapeObjectLists;
    }
}
