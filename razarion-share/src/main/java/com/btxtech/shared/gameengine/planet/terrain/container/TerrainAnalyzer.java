package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGround;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeBotGroundSlopeBox;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.*;

public class TerrainAnalyzer {
    // private final Logger log = Logger.getLogger(TerrainAnalyzer.class.getName());
    private static final byte[] NO_BLOCKED_NODES = new byte[0];
    private final HeightMapAccess heightMapAccess;
    private final TerrainShapeManager terrainShapeManager;
    private final Map<Index, TerrainType> terrainTypeCache = new HashMap<>();
    /**
     * Per tile bitmask (one bit per node, row-major) of the nodes blocked by terrain objects.
     * Built lazily per tile by stamping every blocking circle once, instead of scanning all
     * objects of the tile for every single node - see {@link #buildBlockedNodeMask(Index)}.
     */
    private final Map<Index, byte[]> blockedNodeMasks = new HashMap<>();
    private int lastMaskTileX = Integer.MIN_VALUE;
    private int lastMaskTileY = Integer.MIN_VALUE;
    private byte[] lastMask;

    public TerrainAnalyzer(HeightMapAccess heightMapAccess, TerrainShapeManager terrainShape) {
        this.heightMapAccess = heightMapAccess;
        this.terrainShapeManager = terrainShape;
    }

    private static Double findHeightInSlopeBoxes(TerrainShapeTile terrainShapeTile, DecimalPosition position) {
        for (NativeBotGround nativeBotGround : terrainShapeTile.getNativeBotGrounds()) {
            if (nativeBotGround.botGroundSlopeBoxes != null) {
                for (NativeBotGroundSlopeBox botGroundSlopeBox : nativeBotGround.botGroundSlopeBoxes) {
                    DecimalPosition boxMiddle = new DecimalPosition(botGroundSlopeBox.xPos, botGroundSlopeBox.yPos);
                    Rectangle2D box = Rectangle2D.generateRectangleFromMiddlePoint(boxMiddle, BOT_BOX_LENGTH, BOT_BOX_LENGTH);
                    if (box.containsExclusive(position)) {

                        double yRot = botGroundSlopeBox.yRot % (2 * Math.PI);
                        double factor;
                        if (yRot < Math.toRadians(90)) {
                            factor = (position.getX() - box.startX()) / BOT_BOX_LENGTH;
                        } else if (yRot < Math.toRadians(180)) {
                            factor = 1 - (position.getY() - box.startY()) / BOT_BOX_LENGTH;
                        } else if (yRot < Math.toRadians(270)) {
                            factor = 1 - (position.getX() - box.startX()) / BOT_BOX_LENGTH;
                        } else {
                            factor = (position.getY() - box.startY()) / BOT_BOX_LENGTH;
                        }

                        double heightDelta = Math.tan(botGroundSlopeBox.zRot) * BOT_BOX_LENGTH;
                        return botGroundSlopeBox.height + heightDelta * factor - heightDelta / 2;
                    }
                }
            }
        }
        return null;
    }

    public Vertex toPosition3d(DecimalPosition position2d) {
        double z = getHeightNodeAt(TerrainUtil.terrainPositionToNodeIndex(position2d));
        return new Vertex(position2d, z);
    }

    public TerrainType getTerrainType(Index terrainNodeIndex) {
        TerrainType cached = terrainTypeCache.get(terrainNodeIndex);
        if (cached != null) {
            return cached;
        }

        TerrainType terrainType = analyze(terrainNodeIndex);
        terrainTypeCache.put(terrainNodeIndex, terrainType);

        return terrainType;
    }

    private TerrainType analyze(Index terrainNodeIndex) {
        if (terrainShapeManager != null) {
            if (isBlockedByTerrainObject(terrainNodeIndex)) {
                return TerrainType.BLOCKED;
            }
        }

        double blHeight = getHeightNodeAt(terrainNodeIndex);
        double brHeight = getHeightNodeAt(terrainNodeIndex.add(1, 0));
        double trHeight = getHeightNodeAt(terrainNodeIndex.add(1, 1));
        double tlHeight = getHeightNodeAt(terrainNodeIndex.add(0, 1));

        double avgHeight = (blHeight + brHeight + trHeight + tlHeight) / 4.0;
        if (avgHeight < WATER_LEVEL) {
            return TerrainType.WATER;
        }

        double maxHeight = CollectionUtils.getMax(blHeight, brHeight, trHeight, tlHeight);
        double minHeight = CollectionUtils.getMin(blHeight, brHeight, trHeight, tlHeight);
        if (Math.abs(maxHeight - minHeight) < WALL_HEIGHT_DIFF) {
            return TerrainType.LAND;
        } else {
            return TerrainType.BLOCKED;
        }
    }

    private boolean isBlockedByTerrainObject(Index terrainNodeIndex) {
        // Same truncating division as TerrainUtil.nodeIndexToTileIndex(), but without allocating
        // an Index - this runs once per analyzed node.
        int tileX = (int) (terrainNodeIndex.getX() / (double) NODE_X_COUNT);
        int tileY = (int) (terrainNodeIndex.getY() / (double) NODE_Y_COUNT);
        int localX = terrainNodeIndex.getX() - tileX * NODE_X_COUNT;
        int localY = terrainNodeIndex.getY() - tileY * NODE_Y_COUNT;
        if (localX < 0 || localY < 0 || localX >= NODE_X_COUNT || localY >= NODE_Y_COUNT) {
            // Only reachable for off-map (negative) node indices, where the truncating division
            // above maps to tile 0. Nothing sensible to report there.
            return false;
        }
        byte[] mask = getBlockedNodeMask(tileX, tileY);
        if (mask.length == 0) {
            return false;
        }
        int bit = localY * NODE_X_COUNT + localX;
        return (mask[bit >> 3] & (1 << (bit & 7))) != 0;
    }

    private byte[] getBlockedNodeMask(int tileX, int tileY) {
        if (tileX == lastMaskTileX && tileY == lastMaskTileY) {
            return lastMask;
        }
        Index terrainTileIndex = new Index(tileX, tileY);
        byte[] mask = blockedNodeMasks.get(terrainTileIndex);
        if (mask == null) {
            mask = buildBlockedNodeMask(terrainTileIndex);
            blockedNodeMasks.put(terrainTileIndex, mask);
        }
        lastMaskTileX = tileX;
        lastMaskTileY = tileY;
        lastMask = mask;
        return mask;
    }

    /**
     * Stamps every blocking terrain object of the tile into a node bitmask. Cost is
     * O(objects * covered nodes) instead of the O(nodes * objects) a per-node scan pays, which is
     * what makes a whole-tile analysis affordable (see
     * {@link #generateTerrainTypeOrdinals(Index)}).
     *
     * Only objects registered on THIS tile are considered - identical to the previous per-node
     * scan. TerrainShapeManagerSetup files an object under the tile its centre falls into, so a
     * blocking circle reaching across a tile border does not block on the other side. That quirk
     * is shared with the pathing and deliberately left untouched here.
     */
    private byte[] buildBlockedNodeMask(Index terrainTileIndex) {
        TerrainShapeTile terrainShapeTile = terrainShapeManager.getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null || terrainShapeTile.getNativeTerrainShapeObjectLists() == null) {
            return NO_BLOCKED_NODES;
        }
        Index nodeBase = tileIndexToNodeIndex(terrainTileIndex);
        byte[] mask = null;
        for (NativeTerrainShapeObjectList nativeTerrainShapeObjectList : terrainShapeTile.getNativeTerrainShapeObjectLists()) {
            double radius = terrainShapeManager.getTerrainTypeService().getTerrainObjectConfig(nativeTerrainShapeObjectList.terrainObjectConfigId).getRadius();
            if (radius <= 0.0) {
                continue;
            }
            for (NativeTerrainShapeObjectPosition position : nativeTerrainShapeObjectList.terrainShapeObjectPositions) {
                // A node blocks if its CENTRE (nodeIndex + NODE_SIZE / 2) is closer than radius.
                double localCentreX = position.x - nodeBase.getX() - NODE_SIZE / 2.0;
                double localCentreY = position.y - nodeBase.getY() - NODE_SIZE / 2.0;
                int minX = Math.max(0, (int) Math.floor(localCentreX - radius));
                int maxX = Math.min(NODE_X_COUNT - 1, (int) Math.ceil(localCentreX + radius));
                int minY = Math.max(0, (int) Math.floor(localCentreY - radius));
                int maxY = Math.min(NODE_Y_COUNT - 1, (int) Math.ceil(localCentreY + radius));
                for (int localY = minY; localY <= maxY; localY++) {
                    double deltaY = localY - localCentreY;
                    for (int localX = minX; localX <= maxX; localX++) {
                        double deltaX = localX - localCentreX;
                        if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) - radius < 0) {
                            if (mask == null) {
                                mask = new byte[(NODE_X_COUNT * NODE_Y_COUNT + 7) / 8];
                            }
                            int bit = localY * NODE_X_COUNT + localX;
                            mask[bit >> 3] |= (byte) (1 << (bit & 7));
                        }
                    }
                }
            }
        }
        return mask == null ? NO_BLOCKED_NODES : mask;
    }

    /**
     * Computes the {@link TerrainType} ordinals of a whole tile (row-major
     * NODE_Y_COUNT * NODE_X_COUNT).
     *
     * Calls {@link #analyze(Index)} rather than {@link #getTerrainType(Index)} on purpose: the
     * result is the same, but the 25'600 nodes of a tile are not pushed into
     * {@link #terrainTypeCache}. That cache serves the sparse node sets the pathing walks;
     * filling it tile-wise would cost far more memory than the returned ordinal array.
     */
    public int[] generateTerrainTypeOrdinals(Index terrainTileIndex) {
        Index nodeBase = tileIndexToNodeIndex(terrainTileIndex);
        int[] ordinals = new int[NODE_X_COUNT * NODE_Y_COUNT];
        for (int localY = 0; localY < NODE_Y_COUNT; localY++) {
            for (int localX = 0; localX < NODE_X_COUNT; localX++) {
                ordinals[localY * NODE_X_COUNT + localX] = analyze(nodeBase.add(localX, localY)).ordinal();
            }
        }
        return ordinals;
    }

    public double getHeightNodeAt(Index terrainNodeIndex) {
        Double botSlopeHeight = null;
        Double botHeight = null;
        if (terrainShapeManager != null) {
            TerrainShapeTile terrainShapeTile = terrainShapeManager.getTerrainShapeTile(TerrainUtil.nodeIndexToTileIndex(terrainNodeIndex));
            if (terrainShapeTile != null && terrainShapeTile.getNativeBotGrounds() != null) {
                DecimalPosition position = TerrainUtil.nodeIndexToTerrainPosition(terrainNodeIndex);
                botSlopeHeight = findHeightInSlopeBoxes(terrainShapeTile, position);

                botHeight = Arrays.stream(terrainShapeTile.getNativeBotGrounds())
                        .filter(nativeBotGround ->
                                Arrays.stream(nativeBotGround.positions)
                                        .anyMatch(boxPosition -> Rectangle2D.generateRectangleFromMiddlePoint(NativeUtil.toDecimalPosition(boxPosition), BOT_BOX_LENGTH, BOT_BOX_LENGTH).containsExclusive(position))
                        )
                        .map(nativeBotGround -> nativeBotGround.height)
                        .findFirst()
                        .orElse(null);
            }
        }

        int uint16 = getUInt16GroundHeightAt(terrainNodeIndex);
        double height = uint16ToHeight(uint16);

        if (botSlopeHeight != null && botHeight != null) {
            return Math.max(height, Math.max(botSlopeHeight, botHeight));
        } else if (botSlopeHeight != null) {
            return Math.max(height, botSlopeHeight);
        } else if (botHeight != null) {
            return Math.max(height, botHeight);
        } else {
            return height;
        }


    }

    /**
     * Returns true if the node covering the given position is raised onto a flat bot-ground
     * (plateau) box. Mirrors the bot-ground branch of {@link #getHeightNodeAt(Index)} so callers can
     * predict whether something placed here will sit at the raised plateau height instead of the low
     * terrain height. Slope (ramp) boxes are intentionally excluded — only the flat top counts.
     */
    public boolean isBotGround(DecimalPosition position) {
        if (terrainShapeManager == null) {
            return false;
        }
        Index terrainNodeIndex = terrainPositionToNodeIndex(position);
        TerrainShapeTile terrainShapeTile = terrainShapeManager.getTerrainShapeTile(nodeIndexToTileIndex(terrainNodeIndex));
        if (terrainShapeTile == null || terrainShapeTile.getNativeBotGrounds() == null) {
            return false;
        }
        DecimalPosition nodePosition = nodeIndexToTerrainPosition(terrainNodeIndex);
        return Arrays.stream(terrainShapeTile.getNativeBotGrounds())
                .anyMatch(nativeBotGround -> Arrays.stream(nativeBotGround.positions)
                        .anyMatch(boxPosition -> Rectangle2D.generateRectangleFromMiddlePoint(NativeUtil.toDecimalPosition(boxPosition), BOT_BOX_LENGTH, BOT_BOX_LENGTH).containsExclusive(nodePosition)));
    }

    private int getUInt16GroundHeightAt(Index terrainNodeIndex) {
        Index terrainTileIndex = nodeIndexToTileIndex(terrainNodeIndex);

        int startTileNodeIndex = 0;
        int yAddition = 1;
        if (terrainShapeManager != null) {
            yAddition = 0;
            startTileNodeIndex = TILE_NODE_SIZE * (terrainTileIndex.getY() * terrainShapeManager.getTileXCount() + terrainTileIndex.getX());
        }

        Index offset = terrainNodeIndex.sub(terrainTileIndex.scale(NODE_X_COUNT, NODE_Y_COUNT));
        int offsetTileNodeIndex = offset.getY() * (NODE_X_COUNT + yAddition) + offset.getX();

        return heightMapAccess.getUInt16HeightAt(startTileNodeIndex + offsetTileNodeIndex);
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, Index nodeIndex) {
        return TerrainType.isAllowed(terrainType, getTerrainType(nodeIndex));
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition terrainPosition, double radius) {
        if (terrainType == null) {
            throw new NullPointerException("PathingAccess.isTerrainTypeAllowed() terrainType==null");
        }
        Index nodeIndex = terrainPositionToNodeIndex(terrainPosition);
        if (terrainType.isAreaCheck()) {
            List<Index> rasterOffsets = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.NODE_SIZE);
            for (Index rasterOffset : rasterOffsets) {
                if (!isTerrainTypeAllowed(terrainType, nodeIndex.add(rasterOffset))) {
                    return false;
                }
            }
            return true;
        } else {
            return isTerrainTypeAllowed(terrainType, nodeIndex);
        }
    }

    public Set<Obstacle> getObstacles(DecimalPosition position, double radius) {
        return Collections.emptySet(); // TODO
    }

    public boolean isInSight(DecimalPosition start, double radius, DecimalPosition target, TerrainType terrainType) {
        if (start.equals(target)) {
            return true;
        }
        double angel = start.getAngle(target);
        double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(start, target);
        Line line1 = new Line(start.getPointWithDistance(angel1, radius), target.getPointWithDistance(angel1, radius));
        Line line2 = new Line(start.getPointWithDistance(angel2, radius), target.getPointWithDistance(angel2, radius));

        return !terrainShapeManager.isSightBlocked(line, terrainType) && !terrainShapeManager.isSightBlocked(line1, terrainType) && !terrainShapeManager.isSightBlocked(line2, terrainType);
    }

    public PathingNodeWrapper getPathingNodeWrapper(Index nodeIndex) {
        return new PathingNodeWrapper(nodeIndex, getTerrainType(nodeIndex), this);
    }

    public PathingNodeWrapper getPathingNodeWrapper(DecimalPosition terrainPosition) {
        Index nodeIndex = TerrainUtil.terrainPositionToNodeIndex(terrainPosition);
        return getPathingNodeWrapper(nodeIndex);
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        if (nodeIndex.getX() < 0 || nodeIndex.getY() < 0) {
            return false;
        }
        int xBoundary = terrainShapeManager.getTileXCount() * NODE_X_COUNT;
        int yBoundary = terrainShapeManager.getTileYCount() * NODE_Y_COUNT;
        return nodeIndex.getX() < xBoundary && nodeIndex.getY() < yBoundary;
    }

    public TerrainShapeManager getTerrainShapeManager() {
        return terrainShapeManager;
    }
}
