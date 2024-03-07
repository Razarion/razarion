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
        Assert.assertTrue(testCase(new DecimalPosition(132.100, 103.000), 4.0, TerrainType.LAND));
    }

    @Test
    public void land2() {
        Assert.assertTrue(testCase(new DecimalPosition(149.117, 72.350), 4.0, TerrainType.LAND));
    }

    @Test
    public void land3() {
        Assert.assertFalse(testCase(new DecimalPosition(157.783, 47.683), 4.0, TerrainType.LAND));
    }

    @Test
    public void land4() {
        Assert.assertFalse(testCase(new DecimalPosition(232.117, 86.017), 4.0, TerrainType.LAND));
    }

    @Test
    public void land5() {
        Assert.assertFalse(testCase(new DecimalPosition(222.450, 94.350), 4.0, TerrainType.LAND));
    }

    @Test
    public void land6() {
        Assert.assertTrue(testCase(new DecimalPosition(225.783, 55.017), 4.0, TerrainType.LAND));
    }

    @Test
    public void land7() {
        Assert.assertFalse(testCase(new DecimalPosition(209.450, 189.683), 4.0, TerrainType.LAND));
    }

    @Test
    public void land8() {
        Assert.assertTrue(testCase(new DecimalPosition(197.117, 186.350), 4.0, TerrainType.LAND));
    }

    @Test
    public void land9() {
        Assert.assertTrue(testCase(new DecimalPosition(227.450, 268.683), 4.0, TerrainType.LAND));
    }

    @Test
    public void land10() {
        Assert.assertTrue(testCase(new DecimalPosition(191.450, 281.683), 4.0, TerrainType.LAND));
    }

    @Test
    public void water1() {
        Assert.assertFalse(testCase(new DecimalPosition(121.375, 33.750), 3.0, TerrainType.WATER));
    }

    @Test
    public void water2() {
        Assert.assertFalse(testCase(new DecimalPosition(110.625, 71.000), 3.0, TerrainType.WATER));
    }

    @Test
    public void water3() {
        Assert.assertFalse(testCase(new DecimalPosition(104.375, 137.250), 3.0, TerrainType.WATER));
    }

    @Test
    public void water4() {
        Assert.assertFalse(testCase(new DecimalPosition(91.375, 191.750), 3.0, TerrainType.WATER));
    }

    @Test
    public void water5() {
        Assert.assertFalse(testCase(new DecimalPosition(114.375, 197.250), 3.0, TerrainType.WATER));
    }

    @Test
    public void water6() {
        Assert.assertTrue(testCase(new DecimalPosition(78.875, 223.250), 3.0, TerrainType.WATER));
    }

    @Test
    public void water7() {
        Assert.assertFalse(testCase(new DecimalPosition(166.625, 273.000), 3.0, TerrainType.WATER));
    }

    @Test
    public void water8() {
        Assert.assertFalse(testCase(new DecimalPosition(131.625, 294.000), 3.0, TerrainType.WATER));
    }

    @Test
    public void water9() {
        Assert.assertTrue(testCase(new DecimalPosition(172.875, 309.750), 3.0, TerrainType.WATER));
    }

    @Test
    public void waterCoast1() {
        Assert.assertFalse(testCase(new DecimalPosition(18.083, 51.167), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast2() {
        Assert.assertFalse(testCase(new DecimalPosition(83.750, 78.167), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast3() {
        Assert.assertFalse(testCase(new DecimalPosition(176.417, 224.167), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast4() {
        Assert.assertFalse(testCase(new DecimalPosition(126.750, 287.167), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast5() {
        Assert.assertTrue(testCase(new DecimalPosition(155.988, 195.881), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast6() {
        Assert.assertFalse(testCase(new DecimalPosition(167.274, 192.452), 5.0, TerrainType.WATER_COAST));
    }

    @Test
    public void waterCoast7() {
        Assert.assertFalse(testCase(new DecimalPosition(181.988, 198.452), 5.0, TerrainType.WATER_COAST));
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
        TerrainType terrainType = TerrainType.WATER_COAST;
        showDisplay(new MouseMoveCallback().setCallback(position -> {
            if (testCase(position, radius, terrainType)) {
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.GREEN)};
            } else {
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.RED)};
            }
        }), new TestCaseGenerator().setTestCaseName("waterCoast").setTestGeneratorCallback((position, body) -> {
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