package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeGroundSlopeConnection;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeObstacle;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 18.06.2017.
 */
public class TerrainShapeNode {
    public static final double DEFAULT_HEIGHT = 0;
    private static Logger logger = Logger.getLogger(TerrainShapeNode.class.getName());
    // Game engine
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl
    private TerrainType terrainType;
    private Collection<Obstacle> obstacles;
    private Double gameEngineHeight;
    private Boolean fullGameEngineDriveway;
    // Render engine
    private Map<Integer, List<List<Vertex>>> groundSlopeConnections;
    private Integer renderInnerSlopeId;
    private boolean renderHideGround;
    private Boolean fullRenderEngineDriveway;
    private Integer renderInnerWaterSlopeId;
    private Map<Integer, List<List<Vertex>>> waterSegments;
    // Game and render engine
    private double[] drivewayHeights; // bl, br, tr, tl
    private double innerGroundHeight;
    private Double fullWaterLevel;
    private Boolean drivewayBreakingLine;

    public TerrainShapeNode() {
    }

    public TerrainShapeNode(NativeTerrainShapeNode nativeTerrainShapeNode) {
        drivewayHeights = nativeTerrainShapeNode.fullDrivewayHeights;
        innerGroundHeight = nativeTerrainShapeNode.innerGroundHeight;
        fullWaterLevel = nativeTerrainShapeNode.fullWaterLevel;
        drivewayBreakingLine = nativeTerrainShapeNode.drivewayBreakingLine;
        fullGameEngineDriveway = nativeTerrainShapeNode.fullGameEngineDriveway;
        fullRenderEngineDriveway = nativeTerrainShapeNode.fullRenderEngineDriveway;
        terrainType = TerrainType.fromOrdinal(nativeTerrainShapeNode.terrainTypeOrdinal);
        if (nativeTerrainShapeNode.obstacles != null) {
            Collection<Obstacle> obstacles = new ArrayList<>();
            for (NativeObstacle nativeObstacle : nativeTerrainShapeNode.obstacles) {
                if (ObstacleSlope.isValidNative(nativeObstacle)) {
                    obstacles.add(new ObstacleSlope(nativeObstacle));
                } else if (ObstacleTerrainObject.isValidNative(nativeObstacle)) {
                    obstacles.add(new ObstacleTerrainObject(nativeObstacle));
                } else {
                    logger.warning("TerrainShapeNode setup from net. Illegal Obstacle received");
                }
            }
            this.obstacles = obstacles;
        }
        renderInnerSlopeId = nativeTerrainShapeNode.renderInnerSlopeId;
        renderHideGround = nativeTerrainShapeNode.renderHideGround;
        renderInnerWaterSlopeId = nativeTerrainShapeNode.renderInnerWaterSlopeId;
        if (nativeTerrainShapeNode.groundSlopeConnections != null) {
            groundSlopeConnections = new HashMap<>();
//
// GWT cna not handle below stream solution
//
//            Arrays.stream(nativeTerrainShapeNode.groundSlopeConnections).forEach(groundSlopeConnection -> {
//                List<List<Vertex>> polygons = Arrays.stream(groundSlopeConnection.polygons)
//                        .map(nativeVertices -> Arrays.stream(nativeVertices)
//                                .map(nativeVertex -> new Vertex(nativeVertex.x, nativeVertex.y, nativeVertex.z))
//                                .collect(Collectors.toList()))
//                        .collect(Collectors.toList());
//                groundSlopeConnections.put(groundSlopeConnection.groundConfigId, polygons);
//            });
//
            for (NativeGroundSlopeConnection groundSlopeConnection : nativeTerrainShapeNode.groundSlopeConnections) {
                List<List<Vertex>> polygons = new ArrayList<>();
                for (NativeVertex[] nativeVertexPolygon : groundSlopeConnection.polygons) {
                    List<Vertex> polygon = new ArrayList<>();
                    polygons.add(polygon);
                    for (NativeVertex nativeVertex : nativeVertexPolygon) {
                        polygon.add(new Vertex(nativeVertex.x, nativeVertex.y, nativeVertex.z));
                    }
                }
                if(groundSlopeConnection.defaultGround) {
                    groundSlopeConnections.put(null, polygons);
                } else {
                    groundSlopeConnections.put(groundSlopeConnection.groundConfigId, polygons);
                }
            }
        }
        if (nativeTerrainShapeNode.waterSegments != null) {
            waterSegments = nativeTerrainShapeNode.waterSegments;
//   TODO         waterSegments = new ArrayList<>();
//            for (NativeVertex[] nativeWaterSegment : nativeTerrainShapeNode.waterSegments) {
//                List<Vertex> waterSegment = new ArrayList<>();
//                for (NativeVertex nativeVertex : nativeWaterSegment) {
//                    waterSegment.add(new Vertex(nativeVertex.x, nativeVertex.y, nativeVertex.z));
//                }
//                waterSegments.add(waterSegment);
//            }
        }
        terrainShapeSubNodes = TerrainShapeSubNode.fromNativeTerrainShapeSubNode(0, nativeTerrainShapeNode.nativeTerrainShapeSubNodes);
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

    public void setDrivewayHeights(double[] drivewayHeights) {
        this.drivewayHeights = drivewayHeights;
    }

    public void setInnerGroundHeight(double innerGroundHeight) {
        this.innerGroundHeight = innerGroundHeight;
    }

    public void setGameEngineHeight(Double gameEngineHeight) {
        this.gameEngineHeight = gameEngineHeight;
    }

    public void addGroundSlopeConnections(List<Vertex> groundSlopeConnection, Integer slopeId) {
        if (groundSlopeConnection == null) {
            return;
        }
        if (groundSlopeConnections == null) {
            groundSlopeConnections = new HashMap<>();
        }
        groundSlopeConnections.computeIfAbsent(slopeId, integer -> new ArrayList<>()).add(groundSlopeConnection);
    }

    public void addWaterSegments(List<Vertex> waterSegment, int slopeId) {
        if (waterSegment == null) {
            return;
        }
        if (waterSegments == null) {
            waterSegments = new HashMap<>();
        }
        waterSegments.computeIfAbsent(slopeId, integer -> new ArrayList<>()).add(waterSegment);
    }

    public void setRenderInnerSlopeId(Integer renderInnerSlopeId) {
        this.renderInnerSlopeId = renderInnerSlopeId;
    }

    public Integer getRenderInnerSlopeId() {
        return renderInnerSlopeId;
    }

    public void setRenderHideGround(boolean renderHideGround) {
        this.renderHideGround = renderHideGround;
    }

    public boolean isRenderHideGround() {
        return renderHideGround;
    }

    public Integer getRenderInnerWaterSlopeId() {
        return renderInnerWaterSlopeId;
    }

    public void setRenderInnerWaterSlopeId(Integer renderInnerWaterSlopeId) {
        this.renderInnerWaterSlopeId = renderInnerWaterSlopeId;
    }

    public boolean isFullRenderEngineDriveway() {
        if (fullRenderEngineDriveway != null) {
            return fullRenderEngineDriveway;
        } else {
            return false;
        }
    }

    public void setFullRenderEngineDriveway(boolean fullRenderEngineDriveway) {
        this.fullRenderEngineDriveway = fullRenderEngineDriveway;
    }

    public boolean isFullGameEngineDriveway() {
        if (fullGameEngineDriveway != null) {
            return fullGameEngineDriveway;
        } else {
            return false;
        }
    }

    public void setFullGameEngineDriveway(Boolean fullGameEngineDriveway) {
        this.fullGameEngineDriveway = fullGameEngineDriveway;
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

    public TerrainShapeSubNode getSubNodeBL() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeNode.getSubNodeBL() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[0];
    }

    public TerrainShapeSubNode getSubNodeBR() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeNode.getSubNodeBR() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[1];
    }

    public TerrainShapeSubNode getSubNodeTR() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeNode.getSubNodeTR() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[2];
    }

    public TerrainShapeSubNode getSubNodeTL() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeNode.getSubNodeTL() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[3];
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

    public double getGameEngineHeight() {
        if (gameEngineHeight != null) {
            return gameEngineHeight;
        } else {
            return 0;
        }
    }

    public Double getGameEngineHeightOrNull() {
        return gameEngineHeight;
    }

    public double getInnerGroundHeight() {
        return innerGroundHeight;
    }

    public TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition nodeRelative) {
        return TerrainShapeSubNode.getTerrainShapeSubNode(0, nodeRelative, terrainShapeSubNodes);
    }

    public Map<Integer, List<List<Vertex>>> getGroundSlopeConnections() {
        return groundSlopeConnections;
    }

    public Map<Integer, List<List<Vertex>>> getWaterSegments() {
        return waterSegments;
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public double getDrivewayHeightBL() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBL() drivewayHeights == null");
        }
        return drivewayHeights[0];
    }

    public double getDrivewayHeightBR() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBR() drivewayHeights == null");
        }
        return drivewayHeights[1];
    }

    public double getDrivewayHeightTR() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTR() drivewayHeights == null");
        }
        return drivewayHeights[2];
    }

    public double getDrivewayHeightTL() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTL() drivewayHeights == null");
        }
        return drivewayHeights[3];
    }

    public double[] getDrivewayHeights() {
        return drivewayHeights;
    }

    public NativeTerrainShapeNode toNativeTerrainShapeNode() {
        NativeTerrainShapeNode nativeTerrainShapeNode = new NativeTerrainShapeNode();
        nativeTerrainShapeNode.fullDrivewayHeights = drivewayHeights;
        nativeTerrainShapeNode.innerGroundHeight = innerGroundHeight;
        nativeTerrainShapeNode.terrainTypeOrdinal = TerrainType.toOrdinal(terrainType);
        nativeTerrainShapeNode.fullGameEngineDriveway = fullGameEngineDriveway;
        nativeTerrainShapeNode.fullRenderEngineDriveway = fullRenderEngineDriveway;
        nativeTerrainShapeNode.renderInnerSlopeId = renderInnerSlopeId;
        nativeTerrainShapeNode.renderHideGround = renderHideGround;
        nativeTerrainShapeNode.renderInnerWaterSlopeId = renderInnerWaterSlopeId;
        if (groundSlopeConnections != null) {
            nativeTerrainShapeNode.groundSlopeConnections = groundSlopeConnections.entrySet().stream().map(entry -> {
                NativeGroundSlopeConnection nativeGroundSlopeConnection = new NativeGroundSlopeConnection();
                if (entry.getKey() != null) {
                    nativeGroundSlopeConnection.groundConfigId = entry.getKey();
                } else {
                    nativeGroundSlopeConnection.defaultGround = true;
                }
                nativeGroundSlopeConnection.polygons = entry.getValue().stream()
                        .map(polygons -> polygons.stream().map(NativeHelper::fromVertex).toArray(value -> new NativeVertex[polygons.size()]))
                        .toArray(value -> new NativeVertex[entry.getValue().size()][]);
                return nativeGroundSlopeConnection;
            }).toArray(value -> new NativeGroundSlopeConnection[groundSlopeConnections.size()]);
        }
        if (waterSegments != null) {
//      TODO      nativeTerrainShapeNode.waterSegments = new NativeVertex[waterSegments.size()][];
//            for (int i = 0; i < waterSegments.size(); i++) {
//                List<Vertex> waterSegment = waterSegments.get(i);
//                nativeTerrainShapeNode.waterSegments[i] = waterSegment.stream().map(NativeHelper::fromVertex).toArray(NativeVertex[]::new);
//            }
            nativeTerrainShapeNode.waterSegments = waterSegments;
        }
        nativeTerrainShapeNode.fullWaterLevel = fullWaterLevel;
        if (obstacles != null) {
            nativeTerrainShapeNode.obstacles = obstacles.stream().map(Obstacle::toNativeObstacle).toArray(NativeObstacle[]::new);
        }
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

    public void outerDirectionCallback(AStarContext aStarContext, Index outerDirection, DecimalPosition nodePosition, DirectionConsumer directionConsumer) {
        if (!hasSubNodes()) {
            if (aStarContext.isAllowed(terrainType)) {
                directionConsumer.onTerrainShapeNode(this);
            }
        } else if (outerDirection.getX() > 0) {
            // Access from west
            double length = TerrainUtil.calculateSubNodeLength(0);
            getSubNodeBL().outerDirectionCallback(aStarContext, outerDirection, nodePosition, directionConsumer);
            getSubNodeTL().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(0, length), directionConsumer);
        } else if (outerDirection.getX() < 0) {
            // Access from east
            double length = TerrainUtil.calculateSubNodeLength(0);
            getSubNodeBR().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(length, 0), directionConsumer);
            getSubNodeTR().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(length, length), directionConsumer);
        } else if (outerDirection.getY() > 0) {
            // Access from south
            double length = TerrainUtil.calculateSubNodeLength(0);
            getSubNodeBL().outerDirectionCallback(aStarContext, outerDirection, nodePosition, directionConsumer);
            getSubNodeBR().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(length, 0), directionConsumer);
        } else if (outerDirection.getY() < 0) {
            // Access from north
            double length = TerrainUtil.calculateSubNodeLength(0);
            getSubNodeTR().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(length, length), directionConsumer);
            getSubNodeTL().outerDirectionCallback(aStarContext, outerDirection, nodePosition.add(0, length), directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.outerDirectionCallback() outerDirection: " + outerDirection);
        }
    }

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
}
