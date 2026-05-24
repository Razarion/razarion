package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Equivalence test for PassabilityGrid. For every cell of the AStarBaseTest map and a
 * range of unit radii, asserts that
 *
 *     grid.isPassable(x, y) == computeFitsReference(x, y, terrainType, scopeNodeIndices)
 *
 * where the reference computeFits inlines the scope-iteration semantics the grid is
 * meant to memoise. If this passes, the grid result is bit-identical to "all scope
 * cells in-bounds and of an allowed terrain type" — which is what A* used to compute
 * per cell.
 */
public class PassabilityGridTest extends AStarBaseTest {

    @Test
    public void equivalentToReferenceFits_land_radius_1_5() {
        assertEquivalent(TerrainType.LAND, 1.5);
    }

    @Test
    public void equivalentToReferenceFits_land_radius_2_0() {
        assertEquivalent(TerrainType.LAND, 2.0);
    }

    @Test
    public void equivalentToReferenceFits_land_radius_3_0() {
        assertEquivalent(TerrainType.LAND, 3.0);
    }

    @Test
    public void equivalentToReferenceFits_water_radius_1_5() {
        assertEquivalent(TerrainType.WATER, 1.5);
    }

    @Test
    public void equivalentToReferenceFits_water_radius_3_0() {
        assertEquivalent(TerrainType.WATER, 3.0);
    }

    private void assertEquivalent(TerrainType terrainType, double correctedRadius) {
        List<Index> scopeNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, correctedRadius), (int) TerrainUtil.NODE_SIZE);
        TerrainShapeManager shape = getTerrainShape();
        TerrainAnalyzer analyzer = getTerrainService().getTerrainAnalyzer();
        int xCount = shape.getTileXCount() * TerrainUtil.NODE_X_COUNT;
        int yCount = shape.getTileYCount() * TerrainUtil.NODE_Y_COUNT;

        PassabilityGrid.Grid grid = getPassabilityGrid().getOrBuild(terrainType, correctedRadius);
        if (grid == null) {
            throw new IllegalStateException("PassabilityGrid.getOrBuild returned null - terrain not ready?");
        }

        int mismatches = 0;
        StringBuilder firstFailures = new StringBuilder();
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                boolean gridPassable = grid.isPassable(x, y);
                boolean referenceFits = computeFitsReference(x, y, xCount, yCount, terrainType, scopeNodeIndices, analyzer);
                if (gridPassable != referenceFits) {
                    mismatches++;
                    if (mismatches <= 10) {
                        firstFailures.append("\n  (").append(x).append(",").append(y).append("): grid=")
                                .append(gridPassable).append(" reference=").append(referenceFits);
                    }
                }
            }
        }
        assertEquals("PassabilityGrid disagrees with reference fits for terrainType="
                + terrainType + " correctedRadius=" + correctedRadius
                + " (" + mismatches + " mismatches out of " + (xCount * yCount) + " cells)"
                + firstFailures, 0, mismatches);
    }

    private boolean computeFitsReference(int x, int y, int xCount, int yCount, TerrainType terrainType, List<Index> scopeNodeIndices, TerrainAnalyzer analyzer) {
        for (Index scopeNodeIndex : scopeNodeIndices) {
            int sx = x + scopeNodeIndex.getX();
            int sy = y + scopeNodeIndex.getY();
            if (sx < 0 || sy < 0 || sx >= xCount || sy >= yCount) {
                return false;
            }
            if (!TerrainType.isAllowed(terrainType, analyzer.getTerrainType(new Index(sx, sy)))) {
                return false;
            }
        }
        return true;
    }
}
