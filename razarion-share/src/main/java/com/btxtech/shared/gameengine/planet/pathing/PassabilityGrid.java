package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Per-locomotor passable grid, keyed by (TerrainType, correctedRadius). The (terrainType,
 * radius) pair fully determines the footprint scope, so no separate scopeNodeIndices is
 * needed outside this class — the grid derives scope from radius internally.
 *
 * Each Grid is sized {@code byte[xCount * yCount]} with three states per cell:
 * UNKNOWN (lazily computed on first access), PASSABLE, NOT_PASSABLE.
 *
 * Allocation is O(map cells) and cheap (zero-init). Actual fits-checks happen on demand
 * when A* asks about a cell, so an A* run that visits 5'000 cells pays for 5'000
 * fits-checks instead of pre-computing all 26'000'000.
 *
 * Auto-invalidates when the TerrainShapeManager identity changes.
 */
@Singleton
public class PassabilityGrid {
    public static final double RADIUS_BUCKET_SIZE = 0.5;
    private final TerrainService terrainService;
    private final Map<Key, Grid> gridsByKey = new HashMap<>();
    private TerrainShapeManager lastTerrainShape;

    /**
     * Rounds correctedRadius up to the nearest {@link #RADIUS_BUCKET_SIZE} multiple so units
     * with similar footprints share one grid. Rounding up is conservative: scope grows
     * slightly, the unit might miss its tightest passages, but never thinks it fits where it
     * doesn't. Also stable against floating-point drift in correctedRadius computation.
     */
    public static double bucketRadius(double correctedRadius) {
        return Math.ceil(correctedRadius / RADIUS_BUCKET_SIZE) * RADIUS_BUCKET_SIZE;
    }

    @Inject
    public PassabilityGrid(TerrainService terrainService) {
        this.terrainService = terrainService;
    }

    public void clear() {
        gridsByKey.clear();
        lastTerrainShape = null;
    }

    /**
     * Returns the grid for this footprint, allocating it (but not computing cells) on
     * first request. Returns null if the terrain shape is not yet available.
     */
    public Grid getOrBuild(TerrainType terrainType, double correctedRadius) {
        TerrainShapeManager shape = terrainService.getTerrainShape();
        if (shape == null) {
            return null;
        }
        if (shape != lastTerrainShape) {
            gridsByKey.clear();
            lastTerrainShape = shape;
        }
        Key key = new Key(terrainType, correctedRadius);
        Grid grid = gridsByKey.get(key);
        if (grid == null) {
            int xCount = shape.getTileXCount() * TerrainUtil.NODE_X_COUNT;
            int yCount = shape.getTileYCount() * TerrainUtil.NODE_Y_COUNT;
            List<Index> scopeNodeIndices = GeometricUtil.rasterizeCircle(
                    new Circle2D(DecimalPosition.NULL, correctedRadius), (int) TerrainUtil.NODE_SIZE);
            grid = new Grid(terrainType, scopeNodeIndices, terrainService.getTerrainAnalyzer(), xCount, yCount);
            gridsByKey.put(key, grid);
        }
        return grid;
    }

    public static class Grid {
        private static final byte UNKNOWN = 0;
        private static final byte PASSABLE = 1;
        private static final byte NOT_PASSABLE = 2;

        public final int xCount;
        public final int yCount;
        private final byte[] state;
        private final TerrainType terrainType;
        private final List<Index> scopeNodeIndices;
        private final TerrainAnalyzer analyzer;

        Grid(TerrainType terrainType, List<Index> scopeNodeIndices, TerrainAnalyzer analyzer, int xCount, int yCount) {
            this.terrainType = terrainType;
            this.scopeNodeIndices = scopeNodeIndices;
            this.analyzer = analyzer;
            this.xCount = xCount;
            this.yCount = yCount;
            this.state = new byte[xCount * yCount];
        }

        public boolean isPassable(int x, int y) {
            if (x < 0 || y < 0 || x >= xCount || y >= yCount) {
                return false;
            }
            int idx = y * xCount + x;
            byte v = state[idx];
            if (v == UNKNOWN) {
                v = computeFits(x, y) ? PASSABLE : NOT_PASSABLE;
                state[idx] = v;
            }
            return v == PASSABLE;
        }

        public boolean isPassable(Index nodeIndex) {
            return isPassable(nodeIndex.getX(), nodeIndex.getY());
        }

        private boolean computeFits(int x, int y) {
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

    private static class Key {
        private final TerrainType terrainType;
        private final long radiusBits;

        Key(TerrainType terrainType, double correctedRadius) {
            this.terrainType = terrainType;
            this.radiusBits = Double.doubleToLongBits(correctedRadius);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return radiusBits == key.radiusBits && terrainType == key.terrainType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(terrainType, radiusBits);
        }

        @Override
        public String toString() {
            return "Key{terrainType=" + terrainType + ", correctedRadius=" + Double.longBitsToDouble(radiusBits) + "}";
        }
    }
}
