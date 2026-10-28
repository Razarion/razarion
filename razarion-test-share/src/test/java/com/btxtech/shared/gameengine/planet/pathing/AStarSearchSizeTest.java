package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import org.junit.Assert;
import org.junit.Test;

/**
 * How many nodes A* closes for a path, not only which path it finds. A search runs on the game-engine
 * thread; on PROD (24.10.2026) reachable targets 80-150 m away took 200-1270 ms ([SlowPath]) and stalled
 * the tick for every player.
 * <p>
 * With the straight line as estimate on a grid of four directions, A* closed almost the whole rectangle
 * between start and destination: 7231 nodes for a path of 170, 13164 for 229, 21152 for 542.
 */
public class AStarSearchSizeTest extends AStarBaseTest {

    @Test
    public void openGroundClosesLittleMoreThanThePath() {
        assertClosedAtMost(new DecimalPosition(180, 100), new DecimalPosition(199, 89), 1.5);
        assertClosedAtMost(new DecimalPosition(30, 60), new DecimalPosition(160, 100), 1.5);
        assertClosedAtMost(new DecimalPosition(250, 20), new DecimalPosition(160, 160), 1.5);
    }

    @Test
    public void aroundObstacles() {
        // Across the whole map, around the terrain objects: 21152 nodes before
        assertClosedAtMost(new DecimalPosition(40, 300), new DecimalPosition(300, 40), 10);
    }

    private void assertClosedAtMost(DecimalPosition start, DecimalPosition destination, double timesThePath) {
        SimplePath path = setupPath(2.0, TerrainType.LAND, start, destination);
        int closed = DebugHelperStatic.getLastAStar().getCloseListSize();
        int way = path.getWayPositions().size();
        Assert.assertTrue(start + " -> " + destination + ": closed " + closed + " nodes for a path of " + way,
                closed <= way * timesThePath);
    }
}
