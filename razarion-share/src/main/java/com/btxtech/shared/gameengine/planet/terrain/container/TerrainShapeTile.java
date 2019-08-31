package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeTile;

import java.util.ArrayList;
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
    private Double renderFullWaterLevel;
    private Integer renderFullWaterSlopeId;
    private Double uniformGroundHeight;
    private List<FractionalSlope> fractionalSlopes;
    private NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists;

    public boolean isRenderLand() {
        return renderFullWaterLevel == null;
    }

    public Double getRenderFullWaterLevel() {
        return renderFullWaterLevel;
    }

    public TerrainType getTerrainType() {
        return renderFullWaterLevel == null ? TerrainType.LAND : TerrainType.WATER;
    }

    public void setRenderFullWaterLevel(Double renderFullWaterLevel) {
        this.renderFullWaterLevel = renderFullWaterLevel;
    }

    public Integer getRenderFullWaterSlopeId() {
        return renderFullWaterSlopeId;
    }

    public void setRenderFullWaterSlopeId(Integer renderFullWaterSlopeId) {
        this.renderFullWaterSlopeId = renderFullWaterSlopeId;
    }

    public boolean hasNodes() {
        return terrainShapeNodes != null;
    }

    public void setUniformGroundHeight(Double uniformGroundHeight) {
        this.uniformGroundHeight = uniformGroundHeight;
    }

    public double getUniformGroundHeight() {
        if (uniformGroundHeight != null) {
            return uniformGroundHeight;
        } else {
            return 0;
        }
    }

    public TerrainShapeNode createTerrainShapeNode(Index nodeRelativeIndex) {
        if (terrainShapeNodes == null) {
            terrainShapeNodes = new TerrainShapeNode[TerrainUtil.TERRAIN_TILE_NODES_COUNT][TerrainUtil.TERRAIN_TILE_NODES_COUNT];
        }
        checkNodeIndex(nodeRelativeIndex);
        TerrainShapeNode terrainShapeNode = new TerrainShapeNode();
        terrainShapeNodes[nodeRelativeIndex.getX()][nodeRelativeIndex.getY()] = terrainShapeNode;
        return terrainShapeNode;
    }

    public TerrainShapeNode getTerrainShapeNode(Index nodeRelativeIndex) {
        if (!hasNodes()) {
            return null;
        }
        checkNodeIndex(nodeRelativeIndex);
        return terrainShapeNodes[nodeRelativeIndex.getX()][nodeRelativeIndex.getY()];
    }

    private void checkNodeIndex(Index nodeRelativeIndex) {
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

    public void addFractionalSlope(FractionalSlope fractionalSlope) {
        if (fractionalSlopes == null) {
            fractionalSlopes = new ArrayList<>();
        }
        fractionalSlopes.add(fractionalSlope);
    }

    public List<FractionalSlope> getFractionalSlopes() {
        return fractionalSlopes;
    }

    public void setTerrainShapeNodes(TerrainShapeNode[][] terrainShapeNodes) {
        this.terrainShapeNodes = terrainShapeNodes;
    }


    public NativeTerrainShapeTile toNativeTerrainShapeTile() {
        NativeTerrainShapeTile nativeTerrainShapeTile = new NativeTerrainShapeTile();
        nativeTerrainShapeTile.fullWaterLevel = renderFullWaterLevel;
        if (fractionalSlopes != null) {
            nativeTerrainShapeTile.fractionalSlopes = fractionalSlopes.stream().map(FractionalSlope::toNativeFractionalSlope).toArray(NativeFractionalSlope[]::new);
        }
        nativeTerrainShapeTile.uniformGroundHeight = uniformGroundHeight;
        if (terrainShapeNodes != null) {
            nativeTerrainShapeTile.nativeTerrainShapeNodes = new NativeTerrainShapeNode[TerrainUtil.TERRAIN_TILE_NODES_COUNT][TerrainUtil.TERRAIN_TILE_NODES_COUNT];
            for (int x = 0; x < TerrainUtil.TERRAIN_TILE_NODES_COUNT; x++) {
                for (int y = 0; y < TerrainUtil.TERRAIN_TILE_NODES_COUNT; y++) {
                    TerrainShapeNode terrainShapeNode = terrainShapeNodes[x][y];
                    if (terrainShapeNode != null) {
                        nativeTerrainShapeTile.nativeTerrainShapeNodes[x][y] = terrainShapeNode.toNativeTerrainShapeNode();
                    }
                }
            }
        }
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
