package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import com.btxtech.shared.gameengine.planet.pathing.AStarBaseTest;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by Beat
 * on 14.02.2018.
 */
public class PathingAccessTest extends AStarBaseTest {

    @Test
    public void land1() {
        Assert.assertTrue(testCase(new DecimalPosition(30.667, 16.000), 5.0, TerrainType.LAND));
    }

    @Test
    public void land2() {
        Assert.assertFalse(testCase(new DecimalPosition(78.667, 89.000), 5.0, TerrainType.LAND));
    }

    @Test
    public void land3() {
        Assert.assertTrue(testCase(new DecimalPosition(144.667, 99.000), 5.0, TerrainType.LAND));
    }

    @Test
    public void land4() {
        Assert.assertFalse(testCase(new DecimalPosition(202.667, 149.333), 5.0, TerrainType.LAND));
    }

    @Test
    public void water1() {
        Assert.assertTrue(testCase(new DecimalPosition(190.667, 156.667), 5.0, TerrainType.WATER));
    }

    @Test
    public void water2() {
        Assert.assertFalse(testCase(new DecimalPosition(163.667, 134.667), 5.0, TerrainType.WATER));
    }

    @Test
    public void water3() {
        Assert.assertFalse(testCase(new DecimalPosition(190.333, 109.667), 5.0, TerrainType.WATER));
    }

    @Test
    public void water4() {
        Assert.assertFalse(testCase(new DecimalPosition(185.333, 125.000), 5.0, TerrainType.WATER));
    }

    @Test
    public void water5() {
        Assert.assertTrue(testCase(new DecimalPosition(194.667, 129.000), 5.0, TerrainType.WATER));
    }

    @Test
    public void getObstacles() {
        Collection<Obstacle> obstacles = getTerrainService().getPathingAccess().getObstacles(new DecimalPosition(180, 60), 8);
        List<Obstacle> obstacleList = new ArrayList<>(obstacles);
        assertThat(obstacleList, hasSize(3));
        assertThat(obstacleList, containsInAnyOrder(
                allOf(
                        hasProperty("point1", equalTo(new DecimalPosition(179.68693548636554, 58.0))),
                        hasProperty("point2", equalTo(new DecimalPosition(174.780725808768, 58.0))),
                        hasProperty("previousDirection", equalTo(new DecimalPosition(0.0, 1.0))),
                        hasProperty("point1Convex", equalTo(true)),
                        hasProperty("point1Direction", equalTo(new DecimalPosition(-1.0, 0.0))),
                        hasProperty("point2Convex", equalTo(true)),
                        hasProperty("point2Direction", equalTo(new DecimalPosition(-1.0, 0.0)))),
                allOf(
                        hasProperty("point1", equalTo(new DecimalPosition(179.68693548636554, 51.0))),
                        hasProperty("point2", equalTo(new DecimalPosition(179.68693548636554, 58.0))),
                        hasProperty("previousDirection", equalTo(new DecimalPosition(1.0, 0.0))),
                        hasProperty("point1Convex", equalTo(true)),
                        hasProperty("point1Direction", equalTo(new DecimalPosition(0.0, 1.0))),
                        hasProperty("point2Convex", equalTo(true)),
                        hasProperty("point2Direction", equalTo(new DecimalPosition(-1.0, 0.0)))),
                allOf(
                        hasProperty("point1", equalTo(new DecimalPosition(174.780725808768, 58.0))),
                        hasProperty("point2", equalTo(new DecimalPosition(169.87451613117048, 58.0))),
                        hasProperty("previousDirection", equalTo(new DecimalPosition(-1.0, 0.0))),
                        hasProperty("point1Convex", equalTo(true)),
                        hasProperty("point1Direction", equalTo(new DecimalPosition(-1.0, 0.0))),
                        hasProperty("point2Convex", equalTo(true)),
                        hasProperty("point2Direction", equalTo(new DecimalPosition(-1.0, 0.0))))
        ));
//        PositionMarker positionMarker = new PositionMarker().addCircleColor(new Circle2D(new DecimalPosition(180, 60), 8), new Color(1,0,0, 0.3));
//        System.out.println("Size: " + obstacles.size());
//        obstacles.forEach(obstacle -> {
//            ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
//            positionMarker.addLine(new Line(obstacleSlope.getPoint1(), obstacleSlope.getPoint2()), new Color(0, 0, 1, 0.3));
//        });
//        showDisplay(positionMarker);
    }

    // @Test
    public void generateTestCase() {
        double radius = 5;
        TerrainType terrainType = TerrainType.LAND;
        showDisplay(new MouseMoveCallback().setCallback(position -> {
            if (testCase(position, radius, terrainType)) {
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.GREEN)};
            } else {
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.RED)};
            }
        }), new TestCaseGenerator().setTestCaseName("water").setTestGeneratorCallback((position, body) -> {
            if (testCase(position, radius, terrainType)) {
                body.appendLine("Assert.assertTrue(testCase(" + InstanceStringGenerator.generate(position) + ", " + radius + ", " + InstanceStringGenerator.generate(terrainType) + "));");
            } else {
                body.appendLine("Assert.assertFalse(testCase(" + InstanceStringGenerator.generate(position) + ", " + radius + ", " + InstanceStringGenerator.generate(terrainType) + "));");
            }
        }));
    }

    private boolean testCase(DecimalPosition position, double radius, TerrainType terrainType) {
        return getTerrainService().getPathingAccess().isTerrainTypeAllowed(terrainType, position, radius);
    }

}