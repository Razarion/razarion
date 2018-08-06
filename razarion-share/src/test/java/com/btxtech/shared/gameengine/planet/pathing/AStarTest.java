package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.InstanceStringGenerator;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.gui.userobject.TestCaseGenerator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * on 10.07.2017.
 */
public class AStarTest extends AStarBaseTest {

    @Test
    public void land() {
        SimplePath simplePath = setupPath(3, TerrainType.LAND, new DecimalPosition(62, 11), new DecimalPosition(64, 48));
        assertSimplePath(simplePath, new DecimalPosition(60.0, 20.0), new DecimalPosition(58.0, 26.0), new DecimalPosition(54.0, 26.0), new DecimalPosition(50.0, 26.0), new DecimalPosition(44.0, 28.0), new DecimalPosition(36.0, 28.0), new DecimalPosition(36.0, 36.0), new DecimalPosition(36.0, 44.0), new DecimalPosition(36.0, 52.0), new DecimalPosition(36.0, 60.0), new DecimalPosition(36.0, 68.0), new DecimalPosition(36.0, 76.0), new DecimalPosition(36.0, 84.0), new DecimalPosition(36.0, 92.0), new DecimalPosition(36.0, 100.0), new DecimalPosition(36.0, 108.0), new DecimalPosition(34.0, 114.0), new DecimalPosition(34.0, 118.0), new DecimalPosition(34.0, 122.0), new DecimalPosition(34.0, 126.0), new DecimalPosition(34.0, 130.0), new DecimalPosition(34.0, 134.0), new DecimalPosition(36.0, 140.0), new DecimalPosition(44.0, 140.0), new DecimalPosition(52.0, 140.0), new DecimalPosition(60.0, 140.0), new DecimalPosition(68.0, 140.0), new DecimalPosition(76.0, 140.0), new DecimalPosition(84.0, 140.0), new DecimalPosition(92.0, 140.0), new DecimalPosition(100.0, 140.0), new DecimalPosition(108.0, 140.0), new DecimalPosition(116.0, 140.0), new DecimalPosition(122.0, 142.0), new DecimalPosition(124.0, 148.0), new DecimalPosition(132.0, 148.0), new DecimalPosition(140.0, 148.0), new DecimalPosition(142.0, 142.0), new DecimalPosition(148.0, 140.0), new DecimalPosition(148.0, 132.0), new DecimalPosition(156.0, 132.0), new DecimalPosition(164.0, 132.0), new DecimalPosition(172.0, 132.0), new DecimalPosition(180.0, 132.0), new DecimalPosition(188.0, 132.0), new DecimalPosition(196.0, 132.0), new DecimalPosition(204.0, 132.0), new DecimalPosition(204.0, 124.0), new DecimalPosition(206.0, 118.0), new DecimalPosition(206.0, 114.0), new DecimalPosition(204.0, 108.0), new DecimalPosition(204.0, 100.0), new DecimalPosition(196.0, 100.0), new DecimalPosition(188.0, 100.0), new DecimalPosition(188.0, 92.0), new DecimalPosition(188.0, 84.0), new DecimalPosition(182.0, 82.0), new DecimalPosition(178.0, 82.0), new DecimalPosition(178.0, 78.0), new DecimalPosition(172.0, 76.0), new DecimalPosition(164.0, 76.0), new DecimalPosition(156.0, 76.0), new DecimalPosition(148.0, 76.0), new DecimalPosition(140.0, 76.0), new DecimalPosition(132.0, 76.0), new DecimalPosition(124.0, 76.0), new DecimalPosition(116.0, 76.0), new DecimalPosition(111.5, 72.5), new DecimalPosition(110.5, 72.5), new DecimalPosition(110.5, 71.5), new DecimalPosition(109.0, 71.0), new DecimalPosition(106.0, 70.0), new DecimalPosition(100.0, 68.0), new DecimalPosition(100.0, 60.0), new DecimalPosition(98.0, 54.0), new DecimalPosition(92.0, 52.0), new DecimalPosition(84.0, 52.0), new DecimalPosition(76.0, 52.0), new DecimalPosition(64.0, 48.0));
    }

    @Test
    public void landDestinationNearBlocked() {
        SimplePath simplePath = setupPath(3, TerrainType.LAND, new DecimalPosition(115, 124), new DecimalPosition(77, 119.9));
        assertSimplePath(simplePath, new DecimalPosition(108.0, 124.0), new DecimalPosition(100.0, 124.0), new DecimalPosition(92.0, 124.0), new DecimalPosition(84.0, 124.0), new DecimalPosition(77.0, 119.9));
    }

    @Test
    public void water() {
        SimplePath simplePath = setupPath(4, TerrainType.WATER, new DecimalPosition(207, 240), new DecimalPosition(192, 344));
        assertSimplePath(simplePath, new DecimalPosition(204.0, 252.0), new DecimalPosition(196.0, 252.0), new DecimalPosition(188.0, 252.0), new DecimalPosition(180.0, 252.0), new DecimalPosition(172.0, 252.0), new DecimalPosition(164.0, 252.0), new DecimalPosition(164.0, 260.0), new DecimalPosition(162.0, 266.0), new DecimalPosition(162.0, 270.0), new DecimalPosition(164.0, 276.0), new DecimalPosition(164.0, 284.0), new DecimalPosition(170.0, 286.0), new DecimalPosition(172.0, 292.0), new DecimalPosition(178.0, 294.0), new DecimalPosition(180.0, 300.0), new DecimalPosition(180.0, 308.0), new DecimalPosition(180.0, 316.0), new DecimalPosition(180.0, 324.0), new DecimalPosition(180.0, 332.0), new DecimalPosition(188.0, 332.0), new DecimalPosition(188.0, 340.0), new DecimalPosition(188.0, 348.0), new DecimalPosition(192.0, 344.0));
    }

    @Test
    public void stuck() {
        SimplePath simplePath = setupPath(3, TerrainType.WATER, new DecimalPosition(178.5, 259.5), new DecimalPosition(180, 250));
        assertSimplePath(simplePath, new DecimalPosition(178.5, 258.5), new DecimalPosition(179.0, 257.0), new DecimalPosition(180.0, 250.0));
    }

    @Test
    public void slopeError() {
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.LAND, new DecimalPosition(170, 151));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(70, 117));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void landWaterError() {
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.LAND, new DecimalPosition(76, 92));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(100.375, 226.0));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void waterLandError() {
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(4, TerrainType.WATER, new DecimalPosition(153, 296));
        try {
            getPathingService().setupPathToDestination(syncBaseItem, new DecimalPosition(274, 233));
            Assert.fail("Fail expected. Destination is not free");
        } catch (PathFindingNotFreeException e) {
            // Expected
            Assert.assertTrue(e.getMessage().startsWith("Destination tile is not free:"));
        }
    }

    @Test
    public void landWater1() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(226.250, 238.500));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(252.0, 236.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(226.25, 238.5));
    }

    @Test
    public void landWater2() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(213.250, 203.000));
        assertSimplePath(simplePath, new DecimalPosition(260.0, 236.0), new DecimalPosition(260.0, 228.0), new DecimalPosition(252.0, 228.0), new DecimalPosition(252.0, 220.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(213.25, 203.0));
    }

    @Test
    public void landWater3() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(67.750, 203.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(67.75, 203.0));
    }

    @Test
    public void landWater4() {
        showDisplay();
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(56, 301.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(50.0, 198.0), new DecimalPosition(50.0, 202.0), new DecimalPosition(50.0, 206.0), new DecimalPosition(50.0, 210.0), new DecimalPosition(44.0, 212.0), new DecimalPosition(44.0, 220.0), new DecimalPosition(44.0, 228.0), new DecimalPosition(44.0, 236.0), new DecimalPosition(44.0, 244.0), new DecimalPosition(44.0, 252.0), new DecimalPosition(44.0, 260.0), new DecimalPosition(42.0, 266.0), new DecimalPosition(36.0, 268.0), new DecimalPosition(36.0, 276.0), new DecimalPosition(36.0, 284.0), new DecimalPosition(36.0, 292.0), new DecimalPosition(42.0, 294.0), new DecimalPosition(42.0, 298.0), new DecimalPosition(56.75, 301.0));
    }

    @Test
    public void landWater5() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(162.250, 265.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(238.0, 270.0), new DecimalPosition(234.0, 270.0), new DecimalPosition(230.0, 270.0), new DecimalPosition(226.0, 270.0), new DecimalPosition(222.0, 270.0), new DecimalPosition(218.0, 270.0), new DecimalPosition(214.0, 270.0), new DecimalPosition(210.0, 270.0), new DecimalPosition(206.0, 270.0), new DecimalPosition(202.0, 270.0), new DecimalPosition(198.0, 270.0), new DecimalPosition(194.0, 270.0), new DecimalPosition(190.0, 270.0), new DecimalPosition(186.0, 270.0), new DecimalPosition(182.0, 270.0), new DecimalPosition(162.25, 265.0));
    }

    @Test
    public void landWater6() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(137.750, 356.500));
        assertSimplePath(simplePath, new DecimalPosition(260.0, 252.0), new DecimalPosition(260.0, 260.0), new DecimalPosition(252.0, 260.0), new DecimalPosition(252.0, 268.0), new DecimalPosition(252.0, 276.0), new DecimalPosition(252.0, 284.0), new DecimalPosition(252.0, 292.0), new DecimalPosition(252.0, 300.0), new DecimalPosition(252.0, 308.0), new DecimalPosition(252.0, 316.0), new DecimalPosition(252.0, 324.0), new DecimalPosition(252.0, 332.0), new DecimalPosition(252.0, 340.0), new DecimalPosition(252.0, 348.0), new DecimalPosition(252.0, 356.0), new DecimalPosition(252.0, 364.0), new DecimalPosition(252.0, 372.0), new DecimalPosition(244.0, 372.0), new DecimalPosition(236.0, 372.0), new DecimalPosition(228.0, 372.0), new DecimalPosition(220.0, 372.0), new DecimalPosition(212.0, 372.0), new DecimalPosition(204.0, 372.0), new DecimalPosition(196.0, 372.0), new DecimalPosition(188.0, 372.0), new DecimalPosition(180.0, 372.0), new DecimalPosition(172.0, 372.0), new DecimalPosition(164.0, 372.0), new DecimalPosition(156.0, 372.0), new DecimalPosition(148.0, 372.0), new DecimalPosition(142.0, 374.0), new DecimalPosition(138.0, 374.0), new DecimalPosition(137.75, 356.5));
    }

    @Test
    public void waterLand1() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(151.625, 186.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 244.0), new DecimalPosition(132.0, 236.0), new DecimalPosition(132.0, 228.0), new DecimalPosition(132.0, 220.0), new DecimalPosition(140.0, 220.0), new DecimalPosition(140.0, 212.0), new DecimalPosition(140.0, 204.0), new DecimalPosition(148.0, 204.0), new DecimalPosition(151.625, 186.0));
    }

    @Test
    public void waterLand2() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(241.375, 216.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 252.0), new DecimalPosition(148.0, 252.0), new DecimalPosition(156.0, 252.0), new DecimalPosition(164.0, 252.0), new DecimalPosition(172.0, 252.0), new DecimalPosition(180.0, 252.0), new DecimalPosition(188.0, 252.0), new DecimalPosition(196.0, 252.0), new DecimalPosition(196.0, 244.0), new DecimalPosition(204.0, 244.0), new DecimalPosition(212.0, 244.0), new DecimalPosition(212.0, 236.0), new DecimalPosition(212.0, 228.0), new DecimalPosition(220.0, 228.0), new DecimalPosition(220.0, 220.0), new DecimalPosition(228.0, 220.0), new DecimalPosition(241.375, 216.25));
    }

    @Test
    public void waterLand3() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(181.625, 269.500));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 252.0), new DecimalPosition(140.0, 260.0), new DecimalPosition(148.0, 260.0), new DecimalPosition(148.0, 268.0), new DecimalPosition(156.0, 268.0), new DecimalPosition(162.0, 270.0), new DecimalPosition(164.0, 276.0), new DecimalPosition(164.0, 284.0), new DecimalPosition(170.0, 282.0), new DecimalPosition(181.625, 269.5));
    }

    @Test
    public void waterLand4() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(250.875, 365.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 260.0), new DecimalPosition(140.0, 260.0), new DecimalPosition(140.0, 268.0), new DecimalPosition(140.0, 276.0), new DecimalPosition(142.0, 282.0), new DecimalPosition(148.0, 284.0), new DecimalPosition(156.0, 284.0), new DecimalPosition(164.0, 284.0), new DecimalPosition(170.0, 286.0), new DecimalPosition(172.0, 292.0), new DecimalPosition(178.0, 294.0), new DecimalPosition(180.0, 300.0), new DecimalPosition(186.0, 302.0), new DecimalPosition(188.0, 308.0), new DecimalPosition(188.0, 316.0), new DecimalPosition(196.0, 316.0), new DecimalPosition(196.0, 324.0), new DecimalPosition(196.0, 332.0), new DecimalPosition(196.0, 340.0), new DecimalPosition(204.0, 340.0), new DecimalPosition(212.0, 340.0), new DecimalPosition(218.0, 342.0), new DecimalPosition(220.0, 348.0), new DecimalPosition(226.0, 350.0), new DecimalPosition(234.0, 358.0), new DecimalPosition(250.875, 365.0));
    }

    @Test
    public void waterLand5() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(140.625, 374.250));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 260.0), new DecimalPosition(132.0, 268.0), new DecimalPosition(132.0, 276.0), new DecimalPosition(140.0, 276.0), new DecimalPosition(142.0, 282.0), new DecimalPosition(142.0, 286.0), new DecimalPosition(142.0, 290.0), new DecimalPosition(142.0, 294.0), new DecimalPosition(140.0, 300.0), new DecimalPosition(140.0, 308.0), new DecimalPosition(140.0, 316.0), new DecimalPosition(140.0, 324.0), new DecimalPosition(140.0, 332.0), new DecimalPosition(140.0, 340.0), new DecimalPosition(140.0, 348.0), new DecimalPosition(140.0, 356.0), new DecimalPosition(140.625, 374.25));
    }

    @Test
    public void landWaterCoast1() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(133.625, 197.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(133.625, 197.25));
    }

    @Test
    public void landWaterCoast2() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(58.125, 221.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 172.0), new DecimalPosition(124.0, 172.0), new DecimalPosition(116.0, 172.0), new DecimalPosition(108.0, 172.0), new DecimalPosition(100.0, 172.0), new DecimalPosition(92.0, 172.0), new DecimalPosition(92.0, 180.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(44.0, 196.0), new DecimalPosition(44.0, 204.0), new DecimalPosition(44.0, 212.0), new DecimalPosition(44.0, 220.0), new DecimalPosition(58.125, 221.0));
    }

    @Test
    public void landWaterCoast3() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(169.375, 263.750));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(238.0, 270.0), new DecimalPosition(234.0, 270.0), new DecimalPosition(230.0, 270.0), new DecimalPosition(226.0, 270.0), new DecimalPosition(222.0, 270.0), new DecimalPosition(218.0, 270.0), new DecimalPosition(214.0, 270.0), new DecimalPosition(210.0, 270.0), new DecimalPosition(206.0, 270.0), new DecimalPosition(202.0, 270.0), new DecimalPosition(198.0, 270.0), new DecimalPosition(196.0, 276.0), new DecimalPosition(188.0, 276.0), new DecimalPosition(169.375, 263.75));
    }

    @Test
    public void landWaterCoast4() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(194.125, 362.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(244.0, 276.0), new DecimalPosition(244.0, 284.0), new DecimalPosition(244.0, 292.0), new DecimalPosition(244.0, 300.0), new DecimalPosition(244.0, 308.0), new DecimalPosition(244.0, 316.0), new DecimalPosition(244.0, 324.0), new DecimalPosition(244.0, 332.0), new DecimalPosition(244.0, 340.0), new DecimalPosition(252.0, 340.0), new DecimalPosition(252.0, 348.0), new DecimalPosition(252.0, 356.0), new DecimalPosition(252.0, 364.0), new DecimalPosition(252.0, 372.0), new DecimalPosition(244.0, 372.0), new DecimalPosition(236.0, 372.0), new DecimalPosition(228.0, 372.0), new DecimalPosition(220.0, 372.0), new DecimalPosition(212.0, 372.0), new DecimalPosition(204.0, 372.0), new DecimalPosition(196.0, 372.0), new DecimalPosition(194.125, 362.25));
    }

    @Test
    public void landWaterCoast5() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(118.375, 363.750));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(44.0, 196.0), new DecimalPosition(44.0, 204.0), new DecimalPosition(44.0, 212.0), new DecimalPosition(44.0, 220.0), new DecimalPosition(44.0, 228.0), new DecimalPosition(44.0, 236.0), new DecimalPosition(44.0, 244.0), new DecimalPosition(44.0, 252.0), new DecimalPosition(44.0, 260.0), new DecimalPosition(42.0, 266.0), new DecimalPosition(36.0, 268.0), new DecimalPosition(36.0, 276.0), new DecimalPosition(36.0, 284.0), new DecimalPosition(36.0, 292.0), new DecimalPosition(36.0, 300.0), new DecimalPosition(36.0, 308.0), new DecimalPosition(36.0, 316.0), new DecimalPosition(36.0, 324.0), new DecimalPosition(36.0, 332.0), new DecimalPosition(36.0, 340.0), new DecimalPosition(36.0, 348.0), new DecimalPosition(36.0, 356.0), new DecimalPosition(36.0, 364.0), new DecimalPosition(36.0, 372.0), new DecimalPosition(42.0, 374.0), new DecimalPosition(46.0, 374.0), new DecimalPosition(50.0, 374.0), new DecimalPosition(54.0, 374.0), new DecimalPosition(58.0, 374.0), new DecimalPosition(62.0, 374.0), new DecimalPosition(66.0, 374.0), new DecimalPosition(70.0, 374.0), new DecimalPosition(74.0, 374.0), new DecimalPosition(78.0, 374.0), new DecimalPosition(82.0, 374.0), new DecimalPosition(86.0, 374.0), new DecimalPosition(90.0, 374.0), new DecimalPosition(94.0, 374.0), new DecimalPosition(98.0, 374.0), new DecimalPosition(102.0, 374.0), new DecimalPosition(106.0, 374.0), new DecimalPosition(110.0, 374.0), new DecimalPosition(114.0, 374.0), new DecimalPosition(118.0, 374.0), new DecimalPosition(118.375, 363.75));
    }

    // @Test
    public void testCaseGenerator() {
        double actorRadius = 4;
        TerrainType actorTerrainType = TerrainType.LAND;
        DecimalPosition actorPosition = new DecimalPosition(140, 170);
        double range = 15;
        double targetRadius = 3;
        TerrainType targetTerrainType = TerrainType.WATER_COAST;

        showDisplay(new MouseMoveCallback().setCallback(position -> {
            try {
                SimplePath simplePath = setupPath(actorRadius, actorTerrainType, actorPosition, range, targetRadius, targetTerrainType, position);
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(actorPosition, actorRadius), Color.DARKGRAY).addCircleColor(new Circle2D(position, targetRadius), Color.BLUEVIOLET), simplePath};
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return new Object[]{new PositionMarker().addCircleColor(new Circle2D(actorPosition, actorRadius), Color.DARKGRAY).addCircleColor(new Circle2D(position, targetRadius), Color.BLUEVIOLET)};
        }), new TestCaseGenerator().setTestCaseName("landWaterCoast").setTestGeneratorCallback((position, body) -> {
            SimplePath simplePath = setupPath(actorRadius, actorTerrainType, actorPosition, range, targetRadius, targetTerrainType, position);
            body.appendLine("SimplePath simplePath = setupPath(" + actorRadius + ", " + InstanceStringGenerator.generate(actorTerrainType) + ", " + InstanceStringGenerator.generate(actorPosition) + ", " + range + ", " + targetRadius + ", " + InstanceStringGenerator.generate(targetTerrainType) + ", " + InstanceStringGenerator.generate(position) + ");");
            body.appendLine("assertSimplePath(simplePath, "  /*simplePath.getTotalRange() + ", "*/ + TestHelper.decimalPositionsToString(simplePath.getWayPositions()) + ");");
        }));
    }

    protected void assertSimplePath(SimplePath actual, DecimalPosition... expectedPosition) {
        printSimplePath(actual);
        // showDisplay(actual, new PositionMarker().addPolygon2D(new Polygon2D(Arrays.asList(expectedPosition))));
        TestHelper.assertDecimalPositions(Arrays.asList(expectedPosition), actual.getWayPositions());
    }

    protected void printSimplePath(SimplePath simplePath) {
        System.out.println("assertSimplePath(simplePath, " /*+ simplePath.getTotalRange() + ", "*/ + TestHelper.decimalPositionsToString(simplePath.getWayPositions()) + ");");
    }
}