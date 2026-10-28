package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;
import org.junit.Test;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_X_COUNT;
import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_Y_COUNT;

/**
 * TerrainAnalyzer keeps the terrain type per node in one array per tile instead of a HashMap keyed by
 * Index. The cached answer must be the analysed one, on every tile, read twice and after a clear.
 */
public class TerrainTypeCacheTest extends AStarBaseTest {

    @Test
    public void cachedTypeIsTheAnalysedType() {
        TerrainAnalyzer analyzer = getTerrainService().getTerrainAnalyzer();
        Index tile = new Index(1, 1);
        int[] analysed = analyzer.generateTerrainTypeOrdinals(tile);
        int baseX = tile.getX() * NODE_X_COUNT;
        int baseY = tile.getY() * NODE_Y_COUNT;
        int types = 0;
        for (int pass = 0; pass < 2; pass++) {
            for (int y = 0; y < NODE_Y_COUNT; y++) {
                for (int x = 0; x < NODE_X_COUNT; x++) {
                    TerrainType expected = TerrainType.values()[analysed[y * NODE_X_COUNT + x]];
                    Assert.assertEquals("node " + (baseX + x) + "/" + (baseY + y) + " pass " + pass,
                            expected, analyzer.getTerrainType(new Index(baseX + x, baseY + y)));
                    types |= 1 << expected.ordinal();
                }
            }
        }
        // The tile has more than one kind of ground, or the test says nothing
        Assert.assertNotEquals(Integer.bitCount(types), 1);

        analyzer.clearTerrainTypeCache();
        Assert.assertEquals(TerrainType.values()[analysed[0]], analyzer.getTerrainType(new Index(baseX, baseY)));
    }

    @Test
    public void offTheGrid() {
        TerrainAnalyzer analyzer = getTerrainService().getTerrainAnalyzer();
        TerrainType first = analyzer.getTerrainType(new Index(-1, -1));
        Assert.assertEquals(first, analyzer.getTerrainType(new Index(-1, -1)));
    }
}
