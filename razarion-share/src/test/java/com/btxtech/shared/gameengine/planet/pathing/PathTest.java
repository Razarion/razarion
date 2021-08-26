package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 03.08.2018.
 */
public class PathTest extends AStarBaseTest {

    @Test
    public void testSimple() {
        Path path = setup(new DecimalPosition(75, 165), new DecimalPosition(145, 165), 2);
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(84, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(92, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(100, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(108, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(116, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(124, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(132, 164));
        assertPath(new DecimalPosition(145, 165), path, 2, new DecimalPosition(140, 164));
    }

    @Test
    public void testCornerSkew() {
        setup(new DecimalPosition(220, 392), new DecimalPosition(218, 410), 2);
        Assert.fail("... TODO ...");
    }

    private Path setup(DecimalPosition start, DecimalPosition target, double radius) {
        SimplePath simplePath = setupPath(radius, TerrainType.LAND, start, target);
        List<DecimalPosition> way = new ArrayList<>();
        way.add(start);
        way.addAll(simplePath.getWayPositions());
        Path path = getWeldBean(Path.class);
        path.init(simplePath);
//        showDisplay(simplePath, new MouseMoveCallback() {
//            @Override
//            public Object[] onMouseMove(DecimalPosition position) {
//                path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, TerrainType.LAND, position, null, null, 0));
//                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.PINK).addCircleColor(new Circle2D(path.getCurrentWayPoint(), radius), Color.AQUA)};
//            }
//        });
        return path;
//        for (int i = 0; i + 1 < way.size(); i++) {
//            DecimalPosition current = way.get(i);
//            path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, TerrainType.LAND, current, null, null, 0));
//            System.out.println("current: " + current + " -> " + path.getCurrentWayPoint());
//
//            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, way);
//            // Assert.assertEquals(next, path.getCurrentWayPoint());
//        }
    }

    private void assertPath(DecimalPosition expected, Path path, double radius, DecimalPosition inputPosition) {
        path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, TerrainType.LAND, inputPosition, null));
        TestHelper.assertDecimalPosition("Unexpected getCurrentWayPoint()", expected, path.getCurrentWayPoint());
    }


}
