package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Beat
 * on 28.09.2017.
 */
public class DestinationFinderTest extends AStarBaseTest {

    // @Test
    public void testCaseGenerator() {
        double radius = 3;
        TerrainType terrainType = TerrainType.WATER;

        showDisplay(new MouseMoveCallback().setCallback(position -> {
            try {
                DecimalPosition correctedPosition = testBody(position, radius, terrainType);
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(correctedPosition, radius), Color.PINK).addCircleColor(new Circle2D(position, 1), Color.PLUM)};
            } catch (Exception e) {
                System.out.println(e.getMessage());
                // e.printStackTrace();
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, 1), Color.PLUM)};
            }
        }), new TestCaseGenerator().setTestCaseName("water").setTestGeneratorCallback((position, body) -> {
            DecimalPosition correctedPosition = testBody(position, radius, terrainType);
            body.appendLine("DecimalPosition correctedPosition = testBody(" + InstanceStringGenerator.generate(position) + ", " + radius + ", " + InstanceStringGenerator.generate(terrainType) + ");");
            body.appendLine("Assert.assertEquals(" + InstanceStringGenerator.generate(correctedPosition) + ", correctedPosition);");
        }));
    }

    private DecimalPosition testBody(DecimalPosition destination, double radius, TerrainType terrainType) {
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper destinationNode = getTerrainService().getPathingAccess().getPathingNodeWrapper(destination);
        DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, terrainType, subNodeIndexScope, getTerrainService().getPathingAccess());
        return destinationFinder.find().getCenter();
    }

    @Test
    public void land1() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(240.357, 198.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(244.000, 196.000), correctedPosition);
    }

    @Test
    public void land2() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(239.786, 204.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(244.000, 204.000), correctedPosition);
    }

    @Test
    public void land3() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(230.786, 190.286), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(230.000, 186.000), correctedPosition);
    }

    @Test
    public void land4() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(209.500, 191.571), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(210.000, 186.000), correctedPosition);
    }

    @Test
    public void land5() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(164.643, 98.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(166.000, 102.000), correctedPosition);
    }

    @Test
    public void land6() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(160.071, 51.571), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(164.000, 44.000), correctedPosition);
    }

    @Test
    public void land7() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(191.071, 125.571), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(188.000, 132.000), correctedPosition);
    }

    @Test
    public void land8() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(196.214, 123.571), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(204.000, 124.000), correctedPosition);
    }

    @Test
    public void land9() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(199.500, 117.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(204.0, 108.0), correctedPosition);
    }

    @Test
    public void land10() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(199.071, 110.286), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(204.000, 108.000), correctedPosition);
    }

    @Test
    public void land11() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(193.500, 107.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(196.000, 100.000), correctedPosition);
    }

    @Test
    public void land12() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(189.357, 106.714), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(188.000, 100.000), correctedPosition);
    }

    @Test
    public void land13() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(185.500, 108.857), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(182.000, 102.000), correctedPosition);
    }

    @Test
    public void land14() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(182.643, 111.857), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(178.000, 110.000), correctedPosition);
    }

    @Test
    public void land15() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(180.929, 116.857), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(178.000, 118.000), correctedPosition);
    }

    @Test
    public void land16() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(40.500, 83.429), 3.0, TerrainType.LAND);
        Assert.assertEquals(new DecimalPosition(36.000, 84.000), correctedPosition);
    }

    @Test
    public void water1() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(164.000, 236.583), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(164.000, 236.000), correctedPosition);
    }

    @Test
    public void water2() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(178.333, 258.250), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(180.000, 252.000), correctedPosition);
    }

    @Test
    public void water3() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(232.267, 250.917), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(228.000, 252.000), correctedPosition);
    }

    @Test
    public void water4() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(57.467, 244.717), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(62.000, 246.000), correctedPosition);
    }

    @Test
    public void water5() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(128.467, 279.517), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(132.000, 276.000), correctedPosition);
    }

    @Test
    public void water6() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(136.667, 289.117), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(138.000, 282.000), correctedPosition);
    }

    @Test
    public void water7() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(134.667, 296.117), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(134.000, 302.000), correctedPosition);
    }

    @Test
    public void water8() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(125.067, 299.517), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(124.000, 308.000), correctedPosition);
    }

    @Test
    public void water9() {
        DecimalPosition correctedPosition = testBody(new DecimalPosition(153.267, 361.717), 3.0, TerrainType.WATER);
        Assert.assertEquals(new DecimalPosition(156.000, 356.000), correctedPosition);
    }
}