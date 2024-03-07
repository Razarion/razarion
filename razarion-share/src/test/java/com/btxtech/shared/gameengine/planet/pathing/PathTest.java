package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
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
        Path path = setup(new DecimalPosition(220, 392), new DecimalPosition(218, 410), 2);
        assertPath(new DecimalPosition(242.000, 378.000), path, 2.0, new DecimalPosition(191.900, 406.275));
        assertPath(new DecimalPosition(246.000, 378.000), path, 2.0, new DecimalPosition(194.700, 381.275));
        assertPath(new DecimalPosition(252.000, 388.000), path, 2.0, new DecimalPosition(236.900, 371.075));
        assertPath(new DecimalPosition(228.000, 412.000), path, 2.0, new DecimalPosition(268.500, 367.875));
        assertPath(new DecimalPosition(218.000, 410.000), path, 2.0, new DecimalPosition(269.400, 420.075));
        assertPath(new DecimalPosition(218.000, 410.000), path, 2.0, new DecimalPosition(204.000, 425.975));
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
//                path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, -9998137, TerrainType.LAND, position, null, null));
//                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.PINK).addCircleColor(new Circle2D(path.getCurrentWayPoint(), radius), Color.AQUA)};
//            }
//        }, new TestCaseGenerator().setTestCaseName("landWaterCoast").setTestGeneratorCallback((position, body) -> {
//            path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, -9998137, TerrainType.LAND, position, null, null));
//            body.appendLine("assertPath(" + generate(path.getCurrentWayPoint()) + ", path, " + radius + ", " + generate(position) + ");");
//        }));
        return path;
    }

    private void assertPath(DecimalPosition expected, Path path, double radius, DecimalPosition inputPosition) {
        path.setupCurrentWayPoint(GameTestHelper.createSyncPhysicalMovable(radius, TerrainType.LAND, inputPosition, null));
        TestHelper.assertDecimalPosition("Unexpected getCurrentWayPoint()", expected, path.getCurrentWayPoint());
    }


}
