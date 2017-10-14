package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 14.10.2017.
 */
public class SlopeGroundConnectorFactory {
    public static void prepareContextGroundSlopeConnection(List<DecimalPosition> polygon, boolean isOuter, SlopeContext slopeContext) {
        DecimalPosition last = polygon.get(0);
        Index lastNodeIndex = null;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, polygon);
            if (last.equals(next)) {
                continue;
            }

            DecimalPosition absolute = polygon.get(i);
            Index nodeIndex = TerrainUtil.toNode(absolute);
            addSlopeGroundConnector(polygon, i, nodeIndex, absolute, isOuter, slopeContext);
            if (lastNodeIndex != null) {
                // Check if some node are left out
                if (nodeIndex.getX() != lastNodeIndex.getX() && nodeIndex.getY() != lastNodeIndex.getY()) {
                    DecimalPosition predecessor = polygon.get(i - 1);
                    DecimalPosition successor = polygon.get(i);
                    List<Index> leftOut = GeometricUtil.rasterizeLine(new Line(predecessor, successor), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                    leftOut.remove(0);
                    leftOut.remove(leftOut.size() - 1);
                    for (Index leftOutNodeIndex : leftOut) {
                        addLeftOutSlopeGroundConnector(leftOutNodeIndex, predecessor, successor, isOuter, slopeContext);
                    }
                }
            }
            lastNodeIndex = nodeIndex;
            last = next;
        }
    }

    private static void addSlopeGroundConnector(List<DecimalPosition> slopeLine, int slopePositionIndex, Index nodeIndex, DecimalPosition absolutePosition, boolean isOuter, SlopeContext slopeContext) {
        if (!slopeContext.exitsInSlopeGroundPiercing(nodeIndex, isOuter, absolutePosition)) {
            Rectangle2D nodeRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
            int currentIndex = findStart(nodeRect, slopePositionIndex, slopeLine);
            DecimalPosition current = slopeLine.get(currentIndex);
            List<DecimalPosition> piercingLine = new ArrayList<>();
            piercingLine.add(current);
            do {
                currentIndex = CollectionUtils.getCorrectedIndex(currentIndex + 1, slopeLine);
                current = slopeLine.get(currentIndex);
                piercingLine.add(current);
            } while (nodeRect.contains(current));
            slopeContext.addSlopeGroundPiercing(nodeIndex, isOuter, piercingLine);
        }
    }

    private static void addLeftOutSlopeGroundConnector(Index nodeIndex, DecimalPosition predecessor, DecimalPosition successor, boolean isOuter, SlopeContext slopeContext) {
        List<DecimalPosition> piercingLine = new ArrayList<>();
        piercingLine.add(predecessor);
        piercingLine.add(successor);
        slopeContext.addSlopeGroundPiercing(nodeIndex, isOuter, piercingLine);
    }

    private static int findStart(Rectangle2D rect, int index, List<DecimalPosition> outerLine) {
        int protection = outerLine.size() + 1;
        do {
            index = CollectionUtils.getCorrectedIndex(index - 1, outerLine);
            protection--;
            if (protection < 0) {
                throw new IllegalStateException("Prevent infinite loop");
            }
        } while (rect.contains(outerLine.get(index)));
        return index;
    }
}
