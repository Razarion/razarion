package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 14.02.2018.
 */
public class TerrainDestinationFinderTest extends AStarBaseTest {
    @Test
    public void land1() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4, 4), new DecimalPosition(4, 4), 10 + 4 + 4, 4, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(4, 4), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void land2() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4, 4), new DecimalPosition(14, 14), 10 + 4 + 4, 4, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(12, 12), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast1() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(244.000, 252.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(244.000, 252.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast2() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(244.000, 260.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(244.000, 260.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast3() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(230.000, 270.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(230.000, 270.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast4() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(206.000, 270.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(206.000, 270.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast5() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(252.000, 364.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(252.000, 364.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast6() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(188.000, 372.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(188.000, 372.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast7() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(50.000, 374.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(50.000, 374.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast8() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(50.000, 202.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(50.000, 202.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast9() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(90.000, 186.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(90.000, 186.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void landWaterCoast10() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(4.000, 4.000), new DecimalPosition(234.000, 186.000), 18.0, 4.0, TerrainType.LAND, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(234.000, 186.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand1() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(228.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(228.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand2() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(228.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(228.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand3() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(172.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(172.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand4() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(196.000, 356.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(196.000, 356.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand5() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(234.000, 358.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(234.000, 358.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand6() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(92.000, 356.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(92.000, 356.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand7() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(52.000, 356.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(52.000, 356.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand8() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(54.000, 318.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(54.000, 318.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand9() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(68.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(68.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand10() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(68.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(68.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    @Test
    public void waterLand11() {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(new DecimalPosition(140.000, 230.000), new DecimalPosition(76.000, 204.000), 22.0, 3.0, TerrainType.WATER, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        Assert.assertEquals(new DecimalPosition(76.000, 204.000), terrainDestinationFinder.getReachableNode().getCenter());
    }

    // @Test
    public void testCaseGenerator() {
        DecimalPosition start = new DecimalPosition(140, 230);
        double startRadius = 3;
        TerrainType startTerrainType = TerrainType.WATER;
        double destinationRadius = 4;
        double distance = 15;
        showDisplay(new MouseMoveCallback().setCallback(position -> {
            DecimalPosition correctedDest = testBody(start, startRadius, startTerrainType, position, destinationRadius, distance);
            return new Object[]{new PositionMarker().addCircleColor(new Circle2D(start, startRadius), Color.PINK).addCircleColor(new Circle2D(correctedDest, startRadius), Color.RED).addCircleColor(new Circle2D(position, destinationRadius), Color.GREEN)};
        }), new TestCaseGenerator().setTestCaseName("waterLand").setTestGeneratorCallback((position, body) -> {
            DecimalPosition correctedDest = testBody(start, startRadius, startTerrainType, position, destinationRadius, distance);
            body.appendLine("TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(" + InstanceStringGenerator.generate(start) + ", " + InstanceStringGenerator.generate(correctedDest) + ", " + (startRadius + distance + destinationRadius) + ", " + startRadius + ", " + InstanceStringGenerator.generate(startTerrainType) + ", getTerrainService().getPathingAccess());");
            body.appendLine("terrainDestinationFinder.find();");
            body.appendLine("Assert.assertEquals(" + InstanceStringGenerator.generate(correctedDest) + ", terrainDestinationFinder.getReachableNode().getCenter());");
        }));
        // printSimplePath(simplePath);
    }

    private DecimalPosition testBody(DecimalPosition start, double startRadius, TerrainType startTerrainType, DecimalPosition destination, double radius, double destinationRadius) {
        TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(start, destination, destinationRadius + startRadius + destinationRadius, startRadius, startTerrainType, getTerrainService().getPathingAccess());
        terrainDestinationFinder.find();
        return terrainDestinationFinder.getReachableNode().getCenter();
    }
}