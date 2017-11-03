package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.Collection;

/**
 * Created by Beat
 * on 05.10.2017.
 */
public class TerrainShapeSubNodeFactory {

    public void fillSlopeTerrainShapeSubNode(TerrainShapeNode terrainShapeNode, Rectangle2D terrainRect, Polygon2D terrainTypeRegion, TerrainType innerTerrainType, Double innerHeight, TerrainType outerTerrainType, Double outerHeight, DrivewayContext drivewayContext) {
//        if (terrainRect.startX() == 40 && terrainRect.startY() == 184) {
//            System.out.println("+++++++++++++++++ terrainRect: " + terrainRect);
//        }
//        if (terrainRect.startX() == 112 && terrainRect.startY() == 112) {
//            System.out.println("+++++++++++++++++ terrainRect: " + terrainRect);
//        }
        for (int y = 0; y < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; y += TerrainUtil.MIN_SUB_NODE_LENGTH) {
            for (int x = 0; x < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; x += TerrainUtil.MIN_SUB_NODE_LENGTH) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(new Index(x, y)).add(terrainRect.getStart());
                if (scanPosition.getX() == 47.5 && scanPosition.getY() == 184.5) {
                    System.out.println("+++++++++++++++++ scanPosition: " + scanPosition);
                }
                if (drivewayContext != null) {
                    if (drivewayContext.isInside(scanPosition)) {
                        TerrainShapeSubNode terrainShapeSubNode = getOrCreateDeepestTerrainShapeSubNode(terrainShapeNode, y, x);
                        terrainShapeSubNode.setTerrainType(drivewayContext.getInnerTerrainType());
                        DecimalPosition absoluteSubNodeStart = TerrainUtil.smallestSubNodeAbsolute(new Index(x, y)).add(terrainRect.getStart());
                        Rectangle2D subTerrainRect = new Rectangle2D(absoluteSubNodeStart.getX(), absoluteSubNodeStart.getY(), TerrainUtil.MIN_SUB_NODE_LENGTH, TerrainUtil.MIN_SUB_NODE_LENGTH);
                        if (drivewayContext.getType() == DrivewayContext.Type.SLOPE_DRIVEWAY) {
                            terrainShapeSubNode.setDrivewayHeights(drivewayContext.getDrivewayHeights(subTerrainRect));
                        }
                        terrainShapeSubNode.setHeight(drivewayContext.getHeight());
                        continue;
                    }
                }
                TerrainType terrainType;
                Double height;
                if (terrainTypeRegion.isInside(scanPosition)) {
                    terrainType = innerTerrainType;
                    height = innerHeight;
                } else {
                    terrainType = outerTerrainType;
                    height = outerHeight;
                }
                TerrainShapeSubNode terrainShapeSubNode = getOrCreateDeepestTerrainShapeSubNode(terrainShapeNode, y, x);
                if (terrainType != null) {
                    terrainShapeSubNode.setTerrainType(terrainType);
                }
                if (height != null) {
                    terrainShapeSubNode.setHeight(height);
                }
            }
        }
    }

    public void fillTerrainObjectTerrainShapeSubNode(TerrainShapeNode terrainShapeNode, Rectangle2D terrainRect, Circle2D circle) {
        for (int y = 0; y < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; y += TerrainUtil.MIN_SUB_NODE_LENGTH) {
            for (int x = 0; x < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; x += TerrainUtil.MIN_SUB_NODE_LENGTH) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(new Index(x, y)).add(terrainRect.getStart());
                TerrainShapeSubNode terrainShapeSubNode = getOrCreateDeepestTerrainShapeSubNode(terrainShapeNode, y, x);
                if (circle.inside(scanPosition)) {
                    terrainShapeSubNode.setTerrainType(TerrainType.BLOCKED);
                } else if (terrainShapeSubNode.getTerrainType() == null) {
                    if (terrainShapeNode.getTerrainType() != null) {
                        terrainShapeSubNode.setTerrainType(terrainShapeNode.getTerrainType());
                    } else {
                        terrainShapeSubNode.setTerrainType(TerrainType.LAND);
                    }
                }
            }
        }
    }

    public void concentrate(Collection<TerrainShapeNode> terrainShapeNodes) {
        // TODO terrainShapeNodes.forEach(this::concentrate);
    }

    private void concentrate(TerrainShapeNode terrainShapeNode) {
        TerrainShapeSubNode[] terrainShapeSubNodes = terrainShapeNode.getTerrainShapeSubNodes();
        if (terrainShapeSubNodes == null) {
            return;
        }
        TerrainType lastTerrainType = null;
        boolean mixed = false;
        for (TerrainShapeSubNode terrainShapeSubNode : terrainShapeSubNodes) {
            ConcentrateResult concentrateResult = concentrate(terrainShapeSubNode);
            if (concentrateResult.isMixed()) {
                mixed = true;
            }
            if (mixed) {
                continue;
            }
            if (lastTerrainType == null) {
                lastTerrainType = concentrateResult.getTerrainType();
            } else if (lastTerrainType != concentrateResult.getTerrainType()) {
                mixed = true;
            }
        }
        if (mixed) {
            terrainShapeNode.setTerrainType(null);
        } else {
            terrainShapeNode.setTerrainShapeSubNodes(null);
            terrainShapeNode.setTerrainType(lastTerrainType);
        }
    }

    private TerrainShapeSubNode getOrCreateDeepestTerrainShapeSubNode(TerrainShapeNode terrainShapeNode, int y, int x) {
        int depth0Index = calculateArrayIndex(x, y, 0);
        int depth1Index = calculateArrayIndex(x, y, 1);
        int depth2Index = calculateArrayIndex(x, y, 2);
        TerrainShapeSubNode[] terrainShapeSubNodes0 = terrainShapeNode.getTerrainShapeSubNodes();
        if (terrainShapeSubNodes0 == null) {
            terrainShapeSubNodes0 = new TerrainShapeSubNode[4];
            terrainShapeSubNodes0[0] = new TerrainShapeSubNode(0);
            terrainShapeSubNodes0[1] = new TerrainShapeSubNode(0);
            terrainShapeSubNodes0[2] = new TerrainShapeSubNode(0);
            terrainShapeSubNodes0[3] = new TerrainShapeSubNode(0);
            terrainShapeNode.setTerrainShapeSubNodes(terrainShapeSubNodes0);
        }
        TerrainShapeSubNode terrainShapeSubNode0 = terrainShapeSubNodes0[depth0Index];
        TerrainShapeSubNode terrainShapeSubNode1 = getOrCreateTerrainShapeSubNode(terrainShapeSubNode0, 1, depth1Index);
        return getOrCreateTerrainShapeSubNode(terrainShapeSubNode1, 2, depth2Index);
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
            children[0] = new TerrainShapeSubNode(depth);
            children[1] = new TerrainShapeSubNode(depth);
            children[2] = new TerrainShapeSubNode(depth);
            children[3] = new TerrainShapeSubNode(depth);
            parent.setTerrainShapeSubNodes(children);
        }
        return children[arrayIndex];
    }

    private ConcentrateResult concentrate(TerrainShapeSubNode terrainShapeSubNode) {
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
            return new ConcentrateResult().setTerrainType(terrainShapeSubNode.getTerrainType());
        }
        TerrainType lastTerrainType = null;
        boolean mixed = false;
        for (TerrainShapeSubNode child : terrainShapeSubNode.getTerrainShapeSubNodes()) {
            ConcentrateResult concentrateResult = concentrate(child);
            if (concentrateResult.isMixed()) {
                mixed = true;
            }
            if (mixed) {
                continue;
            }
            if (lastTerrainType == null) {
                lastTerrainType = concentrateResult.getTerrainType();
            } else if (lastTerrainType != concentrateResult.getTerrainType()) {
                mixed = true;
            }
        }
        if (mixed) {
            terrainShapeSubNode.setTerrainType(null);
            return new ConcentrateResult().setMixed();
        } else {
            terrainShapeSubNode.setTerrainShapeSubNodes(null);
            terrainShapeSubNode.setTerrainType(lastTerrainType);
            return new ConcentrateResult().setTerrainType(lastTerrainType);
        }
    }

    private static class ConcentrateResult {
        private boolean mixed;
        private TerrainType terrainType;

        public boolean isMixed() {
            return mixed;
        }

        public ConcentrateResult setMixed() {
            mixed = true;
            return this;
        }

        public TerrainType getTerrainType() {
            return terrainType;
        }

        public ConcentrateResult setTerrainType(TerrainType terrainType) {
            this.terrainType = terrainType;
            return this;
        }
    }
}
