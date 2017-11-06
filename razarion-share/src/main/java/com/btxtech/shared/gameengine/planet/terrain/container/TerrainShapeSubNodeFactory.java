package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.InterpolationUtils;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 05.10.2017.
 */
public class TerrainShapeSubNodeFactory {
    private static Logger logger = Logger.getLogger(TerrainShape.class.getName());

    public void fillSlopeTerrainShapeSubNode(TerrainShapeNode terrainShapeNode, Rectangle2D terrainRect, Polygon2D terrainTypeRegion, TerrainType innerTerrainType, Double innerHeight, TerrainType outerTerrainType, Double outerHeight, DrivewayContext drivewayContext) {
        for (int y = 0; y < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; y += TerrainUtil.MIN_SUB_NODE_LENGTH) {
            for (int x = 0; x < TerrainUtil.TOTAL_MIN_SUB_NODE_COUNT; x += TerrainUtil.MIN_SUB_NODE_LENGTH) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(new Index(x, y)).add(terrainRect.getStart());
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
                terrainShapeSubNode.setDefaultHeightIfNull(terrainShapeNode.getGameEngineHeightOrNull());
                if (terrainShapeNode.isFullGameEngineDriveway() && !terrainShapeSubNode.isDriveway()) {
                    terrainShapeSubNode.setDrivewayHeights(setupDrivewayHeight4SmallestSubNode(new DecimalPosition(x, y), terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL()));
                }
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

    private double[] setupDrivewayHeight4SmallestSubNode(DecimalPosition smallestDubNodeStart, double drivewayHeightBL, double drivewayHeightBR, double drivewayHeightTR, double drivewayHeightTL) {
        DecimalPosition relativeStart = TerrainUtil.toNodeAbsolute(smallestDubNodeStart);
        double relativeLength = TerrainUtil.MIN_SUB_NODE_LENGTH / (double) TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        Rectangle2D relativeRect = new Rectangle2D(relativeStart.getX(), relativeStart.getY(), relativeLength, relativeLength);
        return new double[]{
                InterpolationUtils.rectangleInterpolate(relativeRect.cornerBottomLeft(), drivewayHeightBL, drivewayHeightBR, drivewayHeightTR, drivewayHeightTL),
                InterpolationUtils.rectangleInterpolate(relativeRect.cornerBottomRight(), drivewayHeightBL, drivewayHeightBR, drivewayHeightTR, drivewayHeightTL),
                InterpolationUtils.rectangleInterpolate(relativeRect.cornerTopRight(), drivewayHeightBL, drivewayHeightBR, drivewayHeightTR, drivewayHeightTL),
                InterpolationUtils.rectangleInterpolate(relativeRect.cornerTopLeft(), drivewayHeightBL, drivewayHeightBR, drivewayHeightTR, drivewayHeightTL),
        };
    }

    public void concentrate(Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes) {
        dirtyTerrainShapeNodes.forEach((index, terrainShapeNode) -> {
            try {
                concentrate(terrainShapeNode);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "TerrainShapeSubNodeFactory.concentrate() failed for node at absolute position: " + TerrainUtil.toNodeAbsolute(index), e);
            }
        });
    }

    private void concentrate(TerrainShapeNode terrainShapeNode) {
        TerrainShapeSubNode[] terrainShapeSubNodes = terrainShapeNode.getTerrainShapeSubNodes();
        if (terrainShapeSubNodes == null) {
            return;
        }
        TerrainType lastTerrainType = null;
        double lastHeight = Double.MIN_VALUE;
        boolean lastDriveway = false;
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
                lastHeight = concentrateResult.getHeight();
                lastDriveway = concentrateResult.isDriveway();
            } else if (lastTerrainType != concentrateResult.getTerrainType() || lastHeight != concentrateResult.getHeight() || lastDriveway != concentrateResult.isDriveway()) {
                mixed = true;
            }
        }
        if (mixed) {
            terrainShapeNode.setTerrainType(null);
            terrainShapeNode.setGameEngineHeight(null);
            if (!terrainShapeNode.isFullRenderEngineDriveway()) {
                terrainShapeNode.setDrivewayHeights(null);
            }
        } else {
            terrainShapeNode.setTerrainType(lastTerrainType);
            terrainShapeNode.setGameEngineHeight(lastHeight);
            if (lastDriveway) {
                terrainShapeNode.setFullGameEngineDriveway(true);
                if (!terrainShapeNode.isFullRenderEngineDriveway()) {
                    terrainShapeNode.setDrivewayHeights(new double[]{terrainShapeNode.getSubNodeBL().getDrivewayHeightBL(),
                            terrainShapeNode.getSubNodeBR().getDrivewayHeightBR(),
                            terrainShapeNode.getSubNodeTR().getDrivewayHeightTR(),
                            terrainShapeNode.getSubNodeTL().getDrivewayHeightTL()});
                }
            } else {
                terrainShapeNode.setFullGameEngineDriveway(false);
                if (!terrainShapeNode.isFullRenderEngineDriveway()) {
                    terrainShapeNode.setDrivewayHeights(null);
                }
            }
            terrainShapeNode.setTerrainShapeSubNodes(null);
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
            return new ConcentrateResult().setTerrainType(terrainShapeSubNode.getTerrainType()).setHeight(terrainShapeSubNode.getHeight()).setDrivewayHeights(terrainShapeSubNode.getDrivewayHeights());
        }
        TerrainType lastTerrainType = null;
        double lastHeight = Double.MIN_VALUE;
        boolean lastDriveway = false;
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
                lastHeight = concentrateResult.getHeight();
                lastDriveway = concentrateResult.isDriveway();
            } else if (lastTerrainType != concentrateResult.getTerrainType() || lastHeight != concentrateResult.getHeight() || lastDriveway != concentrateResult.isDriveway()) {
                mixed = true;
            }
        }
        if (mixed) {
            terrainShapeSubNode.setTerrainType(null);
            terrainShapeSubNode.setHeight(null);
            terrainShapeSubNode.setDrivewayHeights(null);
            return new ConcentrateResult().setMixed();
        } else {
            terrainShapeSubNode.setTerrainType(lastTerrainType);
            terrainShapeSubNode.setHeight(lastHeight);
            if (lastDriveway) {
                terrainShapeSubNode.setDrivewayHeights(new double[]{terrainShapeSubNode.getChildSubNodeBL().getDrivewayHeightBL(),
                        terrainShapeSubNode.getChildSubNodeBR().getDrivewayHeightBR(),
                        terrainShapeSubNode.getChildSubNodeTR().getDrivewayHeightTR(),
                        terrainShapeSubNode.getChildSubNodeTL().getDrivewayHeightTL()});
            } else {
                terrainShapeSubNode.setDrivewayHeights(null);
            }
            terrainShapeSubNode.setTerrainShapeSubNodes(null);
            return new ConcentrateResult().setTerrainType(lastTerrainType).setHeight(lastHeight).setDrivewayHeights(terrainShapeSubNode.getDrivewayHeights());
        }
    }

    private static class ConcentrateResult {
        private boolean mixed;
        private TerrainType terrainType;
        private double height;
        private double[] drivewayHeights; // bl, br, tr, tl

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

        public double getHeight() {
            return height;
        }

        public ConcentrateResult setHeight(Double height) {
            this.height = height;
            return this;
        }

        public double[] getDrivewayHeights() {
            return drivewayHeights;
        }

        public boolean isDriveway() {
            return drivewayHeights != null;
        }

        public ConcentrateResult setDrivewayHeights(double[] drivewayHeights) {
            this.drivewayHeights = drivewayHeights;
            return this;
        }
    }
}
