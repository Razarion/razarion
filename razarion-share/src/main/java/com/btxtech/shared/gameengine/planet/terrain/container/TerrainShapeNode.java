package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 18.06.2017.
 */
public class TerrainShapeNode {
    public interface TerrainShapeSubNodeConsumer {
        /**
         * Iterates through all sub nodes
         *
         * @param terrainShapeSubNode sub node
         * @param relativeOffset      absolute offset from the node bottom left
         * @param depth               depth 0 is the top most
         */
        void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition relativeOffset, int depth);
    }

    public interface DirectionConsumer {
        void onTerrainShapeNode(TerrainShapeNode terrainShapeNode);

        void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition subNodePosition);
    }

    private static Logger logger = Logger.getLogger(TerrainShapeNode.class.getName());
    private double[] fullDrivewayHeights; // bl, br, tr, tl
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl
    private Double uniformGroundHeight;
    private List<List<Vertex>> groundSlopeConnections;
    private List<List<Vertex>> waterSegments;
    private Double fullWaterLevel;
    private Collection<Obstacle> obstacles;
    private Boolean hiddenUnderSlope;
    private Boolean drivewayBreakingLine;
    private TerrainType terrainType;

    public TerrainShapeNode() {
    }

    public TerrainShapeNode(NativeTerrainShapeNode nativeTerrainShapeNode) {
        fullDrivewayHeights = nativeTerrainShapeNode.fullDrivewayHeights;
        uniformGroundHeight = nativeTerrainShapeNode.uniformGroundHeight;
        fullWaterLevel = nativeTerrainShapeNode.fullWaterLevel;
        hiddenUnderSlope = nativeTerrainShapeNode.hiddenUnderSlope;
        drivewayBreakingLine = nativeTerrainShapeNode.drivewayBreakingLine;
        if (nativeTerrainShapeNode.terrainTypeOrdinal != null) {
            terrainType = TerrainType.values()[nativeTerrainShapeNode.terrainTypeOrdinal];
        }
        if (nativeTerrainShapeNode.obstacles != null) {
            Collection<Obstacle> obstacles = new ArrayList<>();
            for (NativeObstacle nativeObstacle : nativeTerrainShapeNode.obstacles) {
                if (nativeObstacle.x1 != null && nativeObstacle.y1 != null && nativeObstacle.x2 != null && nativeObstacle.y2 != null) {
                    obstacles.add(new ObstacleSlope(new Line(new DecimalPosition(nativeObstacle.x1, nativeObstacle.y1), new DecimalPosition(nativeObstacle.x2, nativeObstacle.y2))));
                } else if (nativeObstacle.xC != null && nativeObstacle.yC != null && nativeObstacle.r != null) {
                    obstacles.add(new ObstacleTerrainObject(new Circle2D(new DecimalPosition(nativeObstacle.xC, nativeObstacle.yC), nativeObstacle.r)));
                } else {
                    logger.warning("TerrainShapeNode setup from net. Illegal Obstacle received");
                }
            }
            this.obstacles = obstacles;
        }
        if (nativeTerrainShapeNode.groundSlopeConnections != null) {
            groundSlopeConnections = new ArrayList<>();
            for (NativeVertex[] nativeGroundSlopeConnection : nativeTerrainShapeNode.groundSlopeConnections) {
                List<Vertex> groundSlopeConnection = new ArrayList<>();
                for (NativeVertex nativeVertex : nativeGroundSlopeConnection) {
                    groundSlopeConnection.add(new Vertex(nativeVertex.x, nativeVertex.y, nativeVertex.z));
                }
                groundSlopeConnections.add(groundSlopeConnection);
            }
        }
        if (nativeTerrainShapeNode.waterSegments != null) {
            waterSegments = new ArrayList<>();
            for (NativeVertex[] nativeWaterSegment : nativeTerrainShapeNode.waterSegments) {
                List<Vertex> waterSegment = new ArrayList<>();
                for (NativeVertex nativeVertex : nativeWaterSegment) {
                    waterSegment.add(new Vertex(nativeVertex.x, nativeVertex.y, nativeVertex.z));
                }
                waterSegments.add(waterSegment);
            }
        }
        terrainShapeSubNodes = TerrainShapeSubNode.fromNativeTerrainShapeSubNode(null, 0, nativeTerrainShapeNode.nativeTerrainShapeSubNodes);
    }

    public void addObstacle(Obstacle obstacle) {
        if (obstacles == null) {
            obstacles = new ArrayList<>();
        }
        obstacles.add(obstacle);
    }

    public void setFullWaterLevel(Double fullWaterLevel) {
        this.fullWaterLevel = fullWaterLevel;
    }

    public void setFullDrivewayHeights(double[] fullDrivewayHeights) {
        this.fullDrivewayHeights = fullDrivewayHeights;
    }

    public void setUniformGroundHeight(Double uniformGroundHeight) {
        this.uniformGroundHeight = uniformGroundHeight;
    }

    public void setHiddenUnderSlope() {
        hiddenUnderSlope = true;
    }

    public void addGroundSlopeConnections(List<Vertex> groundSlopeConnection) {
        if (groundSlopeConnection == null) {
            return;
        }
        if (groundSlopeConnections == null) {
            groundSlopeConnections = new ArrayList<>();
        }
        groundSlopeConnections.add(groundSlopeConnection);
    }

    public void addWaterSegments(List<Vertex> waterSegment) {
        if (waterSegment == null) {
            return;
        }
        if (waterSegments == null) {
            waterSegments = new ArrayList<>();
        }
        waterSegments.add(waterSegment);
    }

    public boolean isFullLand() {
        return groundSlopeConnections == null && waterSegments == null && !isFullDriveway() && !isFullWater() && !isHiddenUnderSlope();
    }

    @Deprecated // use getTerrainType()
    public boolean isFullDriveway() {
        return fullDrivewayHeights != null;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public boolean isFullWater() {
        return fullWaterLevel != null;
    }

    public Double getFullWaterLevel() {
        return fullWaterLevel;
    }

    public boolean hasSubNodes() {
        return terrainShapeSubNodes != null;
    }

    public TerrainShapeSubNode[] getTerrainShapeSubNodes() {
        return terrainShapeSubNodes;
    }

    public void setTerrainShapeSubNodes(TerrainShapeSubNode[] terrainShapeSubNodes) {
        this.terrainShapeSubNodes = terrainShapeSubNodes;
    }

    public void mergeTerrainShapeSubNodes(TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (this.terrainShapeSubNodes == null) {
            this.terrainShapeSubNodes = terrainShapeSubNodes;
            return;
        }
        for (int i = 0; i < this.terrainShapeSubNodes.length; i++) {
            this.terrainShapeSubNodes[i].merge(terrainShapeSubNodes[i]);
        }
    }

    public void iterateOverTerrainSubNodes(TerrainShapeSubNodeConsumer terrainShapeSubNodeConsumer) {
        double subNodeLength = TerrainUtil.calculateSubNodeLength(0);
        for (int i = 0; i < terrainShapeSubNodes.length; i++) {
            DecimalPosition relativeOffset = TerrainShapeSubNode.numberToSubNodeIndex(i).multiply(subNodeLength);
            TerrainShapeSubNode terrainShapeSubNode = terrainShapeSubNodes[i];
            if (terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
                terrainShapeSubNode.iterateOverTerrainSubNodes(terrainShapeSubNodeConsumer, relativeOffset, 0);
            } else {
                terrainShapeSubNodeConsumer.onTerrainShapeSubNode(terrainShapeSubNode, relativeOffset, 0);
            }
        }
    }

    public boolean isHiddenUnderSlope() {
        return hiddenUnderSlope != null && hiddenUnderSlope;
    }

    public double getUniformGroundHeight() {
        if (uniformGroundHeight != null) {
            return uniformGroundHeight;
        } else {
            return 0;
        }
    }

    public TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition nodeRelative) {
        return TerrainShapeSubNode.getTerrainShapeSubNode(0, nodeRelative, terrainShapeSubNodes);
    }

    public List<List<Vertex>> getGroundSlopeConnections() {
        return groundSlopeConnections;
    }

    public List<List<Vertex>> getWaterSegments() {
        return waterSegments;
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public double getDrivewayHeightBL() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBL() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[0];
    }

    public double getDrivewayHeightBR() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBR() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[1];
    }

    public double getDrivewayHeightTR() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTR() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[2];
    }

    public double getDrivewayHeightTL() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTL() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[3];
    }

    public NativeTerrainShapeNode toNativeTerrainShapeNode() {
        NativeTerrainShapeNode nativeTerrainShapeNode = new NativeTerrainShapeNode();
        nativeTerrainShapeNode.fullDrivewayHeights = fullDrivewayHeights;
        nativeTerrainShapeNode.uniformGroundHeight = uniformGroundHeight;
        nativeTerrainShapeNode.terrainTypeOrdinal = terrainType != null ? terrainType.ordinal() : null;
        if (groundSlopeConnections != null) {
            nativeTerrainShapeNode.groundSlopeConnections = new NativeVertex[groundSlopeConnections.size()][];
            for (int i = 0; i < groundSlopeConnections.size(); i++) {
                List<Vertex> groundSlopeConnection = groundSlopeConnections.get(i);
                nativeTerrainShapeNode.groundSlopeConnections[i] = groundSlopeConnection.stream().map(NativeHelper::fromVertex).toArray(NativeVertex[]::new);
            }
        }
        if (waterSegments != null) {
            nativeTerrainShapeNode.waterSegments = new NativeVertex[waterSegments.size()][];
            for (int i = 0; i < waterSegments.size(); i++) {
                List<Vertex> waterSegment = waterSegments.get(i);
                nativeTerrainShapeNode.waterSegments[i] = waterSegment.stream().map(NativeHelper::fromVertex).toArray(NativeVertex[]::new);
            }
        }
        nativeTerrainShapeNode.fullWaterLevel = fullWaterLevel;
        if (obstacles != null) {
            nativeTerrainShapeNode.obstacles = obstacles.stream().map(Obstacle::toNativeObstacle).toArray(NativeObstacle[]::new);
        }
        nativeTerrainShapeNode.hiddenUnderSlope = hiddenUnderSlope;
        nativeTerrainShapeNode.nativeTerrainShapeSubNodes = TerrainShapeSubNode.toNativeTerrainShapeSubNode(terrainShapeSubNodes);
        nativeTerrainShapeNode.drivewayBreakingLine = drivewayBreakingLine;
        return nativeTerrainShapeNode;
    }

    public void setDrivewayBreakingLine(Boolean drivewayBreakingLine) {
        this.drivewayBreakingLine = drivewayBreakingLine;
    }

    public boolean istDrivewayBreakingLine() {
        return drivewayBreakingLine != null && drivewayBreakingLine;
    }

    public void outerDirectionCallback(Index outerDirection, DecimalPosition nodePosition, DirectionConsumer directionConsumer) {
        if (!hasSubNodes()) {
            directionConsumer.onTerrainShapeNode(this);
        } else if (outerDirection.getX() > 0) {
            // Access from west
            double length = TerrainUtil.calculateSubNodeLength(0);
            terrainShapeSubNodes[0].outerDirectionCallback(outerDirection, nodePosition, directionConsumer);
            terrainShapeSubNodes[3].outerDirectionCallback(outerDirection, nodePosition.add(0, length), directionConsumer);
        } else if (outerDirection.getX() < 0) {
            // Access from east
            double length = TerrainUtil.calculateSubNodeLength(0);
            terrainShapeSubNodes[1].outerDirectionCallback(outerDirection, nodePosition.add(length, 0), directionConsumer);
            terrainShapeSubNodes[2].outerDirectionCallback(outerDirection, nodePosition.add(length, length), directionConsumer);
        } else if (outerDirection.getY() > 0) {
            // Access from south
            double length = TerrainUtil.calculateSubNodeLength(0);
            terrainShapeSubNodes[0].outerDirectionCallback(outerDirection, nodePosition, directionConsumer);
            terrainShapeSubNodes[1].outerDirectionCallback(outerDirection, nodePosition.add(length, 0), directionConsumer);
        } else if (outerDirection.getY() < 0) {
            // Access from north
            double length = TerrainUtil.calculateSubNodeLength(0);
            terrainShapeSubNodes[2].outerDirectionCallback(outerDirection, nodePosition.add(length, length), directionConsumer);
            terrainShapeSubNodes[3].outerDirectionCallback(outerDirection, nodePosition.add(0, length), directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.outerDirectionCallback() outerDirection: " + outerDirection);
        }
    }
}
