package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
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
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * on 10.07.2017.
 */
public class AStarTest extends AStarBaseTest {

    @Test
    public void land1() {
        SimplePath simplePath = setupPath(2.0, TerrainType.LAND, new DecimalPosition(180.000, 100.000), 15.0, 2.0, TerrainType.LAND, new DecimalPosition(199.000, 89.000));
        assertSimplePath(simplePath, new DecimalPosition(181.5, 100.5), new DecimalPosition(182.5, 100.5), new DecimalPosition(183.5, 100.5), new DecimalPosition(184.5, 100.5), new DecimalPosition(185.5, 100.5), new DecimalPosition(186.5, 100.5), new DecimalPosition(187.5, 100.5), new DecimalPosition(188.5, 100.5), new DecimalPosition(188.5, 99.5), new DecimalPosition(189.5, 99.5), new DecimalPosition(189.5, 98.5), new DecimalPosition(190.5, 98.5), new DecimalPosition(190.5, 97.5), new DecimalPosition(191.5, 97.5), new DecimalPosition(192.5, 97.5), new DecimalPosition(192.5, 96.5), new DecimalPosition(193.5, 96.5), new DecimalPosition(193.5, 95.5), new DecimalPosition(194.5, 95.5), new DecimalPosition(194.5, 94.5), new DecimalPosition(195.5, 94.5), new DecimalPosition(195.5, 93.5), new DecimalPosition(195.5, 92.5), new DecimalPosition(196.5, 92.5), new DecimalPosition(197.5, 92.5), new DecimalPosition(197.5, 91.5), new DecimalPosition(198.5, 91.5), new DecimalPosition(198.5, 90.5), new DecimalPosition(198.5, 89.5), new DecimalPosition(199.0, 89.0));
    }

    @Test
    public void landDestinationNearBlocked() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(180.000, 100.000), 15.0, 3.0, TerrainType.LAND, new DecimalPosition(180.667, 81.667));
        assertSimplePath(simplePath, new DecimalPosition(180.5, 99.5), new DecimalPosition(180.5, 98.5), new DecimalPosition(180.5, 97.5), new DecimalPosition(180.5, 96.5), new DecimalPosition(180.5, 95.5), new DecimalPosition(180.5, 94.5), new DecimalPosition(180.5, 93.5), new DecimalPosition(180.5, 92.5), new DecimalPosition(180.5, 91.5), new DecimalPosition(180.5, 90.5), new DecimalPosition(180.5, 89.5), new DecimalPosition(181.5, 89.5), new DecimalPosition(181.5, 88.5), new DecimalPosition(182.5, 88.5), new DecimalPosition(182.5, 87.5), new DecimalPosition(183.5, 87.5), new DecimalPosition(183.5, 86.5), new DecimalPosition(184.5, 86.5), new DecimalPosition(184.5, 85.5), new DecimalPosition(185.5, 85.5), new DecimalPosition(185.5, 84.5), new DecimalPosition(185.5, 83.5), new DecimalPosition(186.5, 83.5), new DecimalPosition(186.5, 82.5), new DecimalPosition(180.66666666666666, 81.66666666666667));
    }

    @Test
    public void water1() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(200.000, 180.000), 15.0, 3.0, TerrainType.WATER, new DecimalPosition(224.500, 179.000));
        assertSimplePath(simplePath, new DecimalPosition(201.5, 180.5), new DecimalPosition(202.5, 180.5), new DecimalPosition(203.5, 180.5), new DecimalPosition(204.5, 180.5), new DecimalPosition(205.5, 180.5), new DecimalPosition(206.5, 180.5), new DecimalPosition(207.5, 180.5), new DecimalPosition(208.5, 180.5), new DecimalPosition(209.5, 180.5), new DecimalPosition(210.5, 180.5), new DecimalPosition(211.5, 180.5), new DecimalPosition(212.5, 180.5), new DecimalPosition(213.5, 180.5), new DecimalPosition(214.5, 180.5), new DecimalPosition(215.5, 180.5), new DecimalPosition(216.5, 180.5), new DecimalPosition(217.5, 180.5), new DecimalPosition(218.5, 180.5), new DecimalPosition(219.5, 180.5), new DecimalPosition(220.5, 180.5), new DecimalPosition(221.5, 180.5), new DecimalPosition(222.5, 180.5), new DecimalPosition(223.5, 180.5), new DecimalPosition(223.5, 179.5), new DecimalPosition(224.5, 179.0));
    }

    @Test
    public void water2() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(200.000, 180.000), 15.0, 3.0, TerrainType.WATER, new DecimalPosition(277.500, 105.000));
        assertSimplePath(simplePath, new DecimalPosition(201.5, 180.5), new DecimalPosition(202.5, 180.5), new DecimalPosition(203.5, 180.5), new DecimalPosition(203.5, 179.5), new DecimalPosition(204.5, 179.5), new DecimalPosition(204.5, 178.5), new DecimalPosition(205.5, 178.5), new DecimalPosition(205.5, 177.5), new DecimalPosition(205.5, 176.5), new DecimalPosition(206.5, 176.5), new DecimalPosition(206.5, 175.5), new DecimalPosition(207.5, 175.5), new DecimalPosition(207.5, 174.5), new DecimalPosition(208.5, 174.5), new DecimalPosition(209.5, 174.5), new DecimalPosition(209.5, 173.5), new DecimalPosition(209.5, 172.5), new DecimalPosition(210.5, 172.5), new DecimalPosition(210.5, 171.5), new DecimalPosition(211.5, 171.5), new DecimalPosition(212.5, 171.5), new DecimalPosition(212.5, 170.5), new DecimalPosition(213.5, 170.5), new DecimalPosition(213.5, 169.5), new DecimalPosition(214.5, 169.5), new DecimalPosition(214.5, 168.5), new DecimalPosition(214.5, 167.5), new DecimalPosition(215.5, 167.5), new DecimalPosition(215.5, 166.5), new DecimalPosition(216.5, 166.5), new DecimalPosition(217.5, 166.5), new DecimalPosition(217.5, 165.5), new DecimalPosition(218.5, 165.5), new DecimalPosition(218.5, 164.5), new DecimalPosition(219.5, 164.5), new DecimalPosition(219.5, 163.5), new DecimalPosition(220.5, 163.5), new DecimalPosition(220.5, 162.5), new DecimalPosition(221.5, 162.5), new DecimalPosition(221.5, 161.5), new DecimalPosition(221.5, 160.5), new DecimalPosition(222.5, 160.5), new DecimalPosition(223.5, 160.5), new DecimalPosition(223.5, 159.5), new DecimalPosition(224.5, 159.5), new DecimalPosition(224.5, 158.5), new DecimalPosition(225.5, 158.5), new DecimalPosition(225.5, 157.5), new DecimalPosition(226.5, 157.5), new DecimalPosition(226.5, 156.5), new DecimalPosition(227.5, 156.5), new DecimalPosition(227.5, 155.5), new DecimalPosition(228.5, 155.5), new DecimalPosition(228.5, 154.5), new DecimalPosition(229.5, 154.5), new DecimalPosition(229.5, 153.5), new DecimalPosition(230.5, 153.5), new DecimalPosition(230.5, 152.5), new DecimalPosition(230.5, 151.5), new DecimalPosition(231.5, 151.5), new DecimalPosition(232.5, 151.5), new DecimalPosition(232.5, 150.5), new DecimalPosition(233.5, 150.5), new DecimalPosition(233.5, 149.5), new DecimalPosition(234.5, 149.5), new DecimalPosition(234.5, 148.5), new DecimalPosition(234.5, 147.5), new DecimalPosition(235.5, 147.5), new DecimalPosition(235.5, 146.5), new DecimalPosition(236.5, 146.5), new DecimalPosition(237.5, 146.5), new DecimalPosition(237.5, 145.5), new DecimalPosition(237.5, 144.5), new DecimalPosition(238.5, 144.5), new DecimalPosition(239.5, 144.5), new DecimalPosition(239.5, 143.5), new DecimalPosition(239.5, 142.5), new DecimalPosition(240.5, 142.5), new DecimalPosition(241.5, 142.5), new DecimalPosition(241.5, 141.5), new DecimalPosition(241.5, 140.5), new DecimalPosition(242.5, 140.5), new DecimalPosition(243.5, 140.5), new DecimalPosition(243.5, 139.5), new DecimalPosition(244.5, 139.5), new DecimalPosition(244.5, 138.5), new DecimalPosition(245.5, 138.5), new DecimalPosition(245.5, 137.5), new DecimalPosition(245.5, 136.5), new DecimalPosition(246.5, 136.5), new DecimalPosition(247.5, 136.5), new DecimalPosition(247.5, 135.5), new DecimalPosition(248.5, 135.5), new DecimalPosition(248.5, 134.5), new DecimalPosition(249.5, 134.5), new DecimalPosition(249.5, 133.5), new DecimalPosition(249.5, 132.5), new DecimalPosition(250.5, 132.5), new DecimalPosition(251.5, 132.5), new DecimalPosition(251.5, 131.5), new DecimalPosition(252.5, 131.5), new DecimalPosition(252.5, 130.5), new DecimalPosition(253.5, 130.5), new DecimalPosition(254.5, 130.5), new DecimalPosition(255.5, 130.5), new DecimalPosition(255.5, 129.5), new DecimalPosition(256.5, 129.5), new DecimalPosition(257.5, 129.5), new DecimalPosition(258.5, 129.5), new DecimalPosition(259.5, 129.5), new DecimalPosition(259.5, 128.5), new DecimalPosition(260.5, 128.5), new DecimalPosition(261.5, 128.5), new DecimalPosition(262.5, 128.5), new DecimalPosition(262.5, 127.5), new DecimalPosition(262.5, 126.5), new DecimalPosition(262.5, 125.5), new DecimalPosition(262.5, 124.5), new DecimalPosition(262.5, 123.5), new DecimalPosition(262.5, 122.5), new DecimalPosition(262.5, 121.5), new DecimalPosition(262.5, 120.5), new DecimalPosition(263.5, 120.5), new DecimalPosition(263.5, 119.5), new DecimalPosition(263.5, 118.5), new DecimalPosition(264.5, 118.5), new DecimalPosition(265.5, 118.5), new DecimalPosition(265.5, 117.5), new DecimalPosition(266.5, 117.5), new DecimalPosition(266.5, 116.5), new DecimalPosition(266.5, 115.5), new DecimalPosition(267.5, 115.5), new DecimalPosition(267.5, 114.5), new DecimalPosition(268.5, 114.5), new DecimalPosition(269.5, 114.5), new DecimalPosition(269.5, 113.5), new DecimalPosition(270.5, 113.5), new DecimalPosition(270.5, 112.5), new DecimalPosition(270.5, 111.5), new DecimalPosition(271.5, 111.5), new DecimalPosition(271.5, 110.5), new DecimalPosition(272.5, 110.5), new DecimalPosition(273.5, 110.5), new DecimalPosition(273.5, 109.5), new DecimalPosition(274.5, 109.5), new DecimalPosition(274.5, 108.5), new DecimalPosition(275.5, 108.5), new DecimalPosition(275.5, 107.5), new DecimalPosition(276.5, 107.5), new DecimalPosition(276.5, 106.5), new DecimalPosition(276.5, 105.5), new DecimalPosition(277.5, 105.0));
    }

    @Test
    @Ignore
    public void stuck() {
        SimplePath simplePath = setupPath(3, TerrainType.WATER, new DecimalPosition(178.5, 259.5), new DecimalPosition(180, 250));
        assertSimplePath(simplePath, new DecimalPosition(178.5, 258.5), new DecimalPosition(179.0, 257.0), new DecimalPosition(180.0, 250.0));
    }

    @Test
    @Ignore
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
    @Ignore
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
    @Ignore
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
    @Ignore
    public void landWater1() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(226.250, 238.500));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(252.0, 236.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(226.25, 238.5));
    }

    @Test
    @Ignore
    public void landWater2() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(213.250, 203.000));
        assertSimplePath(simplePath, new DecimalPosition(260.0, 236.0), new DecimalPosition(260.0, 228.0), new DecimalPosition(252.0, 228.0), new DecimalPosition(252.0, 220.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(213.25, 203.0));
    }

    @Test
    @Ignore
    public void landWater3() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(67.750, 203.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(67.75, 203.0));
    }

    @Test
    @Ignore
    public void landWater4() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(56, 301.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(50.0, 198.0), new DecimalPosition(50.0, 202.0), new DecimalPosition(50.0, 206.0), new DecimalPosition(50.0, 210.0), new DecimalPosition(44.0, 212.0), new DecimalPosition(44.0, 220.0), new DecimalPosition(44.0, 228.0), new DecimalPosition(44.0, 236.0), new DecimalPosition(44.0, 244.0), new DecimalPosition(44.0, 252.0), new DecimalPosition(44.0, 260.0), new DecimalPosition(42.0, 266.0), new DecimalPosition(36.0, 268.0), new DecimalPosition(36.0, 276.0), new DecimalPosition(36.0, 284.0), new DecimalPosition(36.0, 292.0), new DecimalPosition(42.0, 294.0), new DecimalPosition(42.0, 302.0), new DecimalPosition(56.0, 301.0));
    }

    @Test
    @Ignore
    public void landWater5() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(162.250, 265.000));
        assertSimplePath(simplePath, new DecimalPosition(252.0, 244.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(238.0, 270.0), new DecimalPosition(234.0, 270.0), new DecimalPosition(230.0, 270.0), new DecimalPosition(226.0, 270.0), new DecimalPosition(222.0, 270.0), new DecimalPosition(218.0, 270.0), new DecimalPosition(214.0, 270.0), new DecimalPosition(210.0, 270.0), new DecimalPosition(206.0, 270.0), new DecimalPosition(202.0, 270.0), new DecimalPosition(198.0, 270.0), new DecimalPosition(194.0, 270.0), new DecimalPosition(190.0, 270.0), new DecimalPosition(186.0, 270.0), new DecimalPosition(182.0, 270.0), new DecimalPosition(162.25, 265.0));
    }

    @Test
    @Ignore
    public void landWater6() {
        SimplePath simplePath = setupPath(3.0, TerrainType.LAND, new DecimalPosition(256.000, 240.000), 15.0, 4.0, TerrainType.WATER, new DecimalPosition(137.750, 356.500));
        assertSimplePath(simplePath, new DecimalPosition(260.0, 252.0), new DecimalPosition(260.0, 260.0), new DecimalPosition(252.0, 260.0), new DecimalPosition(252.0, 268.0), new DecimalPosition(252.0, 276.0), new DecimalPosition(252.0, 284.0), new DecimalPosition(252.0, 292.0), new DecimalPosition(252.0, 300.0), new DecimalPosition(252.0, 308.0), new DecimalPosition(252.0, 316.0), new DecimalPosition(252.0, 324.0), new DecimalPosition(252.0, 332.0), new DecimalPosition(252.0, 340.0), new DecimalPosition(252.0, 348.0), new DecimalPosition(252.0, 356.0), new DecimalPosition(252.0, 364.0), new DecimalPosition(252.0, 372.0), new DecimalPosition(244.0, 372.0), new DecimalPosition(236.0, 372.0), new DecimalPosition(228.0, 372.0), new DecimalPosition(220.0, 372.0), new DecimalPosition(212.0, 372.0), new DecimalPosition(204.0, 372.0), new DecimalPosition(196.0, 372.0), new DecimalPosition(188.0, 372.0), new DecimalPosition(180.0, 372.0), new DecimalPosition(172.0, 372.0), new DecimalPosition(164.0, 372.0), new DecimalPosition(156.0, 372.0), new DecimalPosition(148.0, 372.0), new DecimalPosition(142.0, 374.0), new DecimalPosition(138.0, 374.0), new DecimalPosition(137.75, 356.5));
    }

    @Test
    @Ignore
    public void waterLand1() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(151.625, 186.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 244.0), new DecimalPosition(132.0, 236.0), new DecimalPosition(132.0, 228.0), new DecimalPosition(132.0, 220.0), new DecimalPosition(140.0, 220.0), new DecimalPosition(140.0, 212.0), new DecimalPosition(140.0, 204.0), new DecimalPosition(148.0, 204.0), new DecimalPosition(151.625, 186.0));
    }

    @Test
    @Ignore
    public void waterLand2() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(241.375, 216.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 252.0), new DecimalPosition(148.0, 252.0), new DecimalPosition(156.0, 252.0), new DecimalPosition(164.0, 252.0), new DecimalPosition(172.0, 252.0), new DecimalPosition(180.0, 252.0), new DecimalPosition(188.0, 252.0), new DecimalPosition(196.0, 252.0), new DecimalPosition(196.0, 244.0), new DecimalPosition(204.0, 244.0), new DecimalPosition(212.0, 244.0), new DecimalPosition(212.0, 236.0), new DecimalPosition(212.0, 228.0), new DecimalPosition(220.0, 228.0), new DecimalPosition(220.0, 220.0), new DecimalPosition(228.0, 220.0), new DecimalPosition(241.375, 216.25));
    }

    @Test
    @Ignore
    public void waterLand3() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(181.625, 269.500));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 252.0), new DecimalPosition(140.0, 260.0), new DecimalPosition(148.0, 260.0), new DecimalPosition(148.0, 268.0), new DecimalPosition(156.0, 268.0), new DecimalPosition(162.0, 270.0), new DecimalPosition(164.0, 276.0), new DecimalPosition(164.0, 284.0), new DecimalPosition(170.0, 282.0), new DecimalPosition(181.625, 269.5));
    }

    @Test
    @Ignore
    public void waterLand4() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(250.875, 365.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 260.0), new DecimalPosition(140.0, 260.0), new DecimalPosition(140.0, 268.0), new DecimalPosition(140.0, 276.0), new DecimalPosition(142.0, 282.0), new DecimalPosition(148.0, 284.0), new DecimalPosition(156.0, 284.0), new DecimalPosition(164.0, 284.0), new DecimalPosition(170.0, 286.0), new DecimalPosition(172.0, 292.0), new DecimalPosition(178.0, 294.0), new DecimalPosition(180.0, 300.0), new DecimalPosition(186.0, 302.0), new DecimalPosition(188.0, 308.0), new DecimalPosition(188.0, 316.0), new DecimalPosition(196.0, 316.0), new DecimalPosition(196.0, 324.0), new DecimalPosition(196.0, 332.0), new DecimalPosition(196.0, 340.0), new DecimalPosition(204.0, 340.0), new DecimalPosition(212.0, 340.0), new DecimalPosition(218.0, 342.0), new DecimalPosition(220.0, 348.0), new DecimalPosition(226.0, 350.0), new DecimalPosition(234.0, 358.0), new DecimalPosition(250.875, 365.0));
    }

    @Test
    @Ignore
    public void waterLand5() {
        SimplePath simplePath = setupPath(3.0, TerrainType.WATER, new DecimalPosition(130.000, 250.000), 15.0, 4.0, TerrainType.LAND, new DecimalPosition(140.625, 374.250));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 260.0), new DecimalPosition(132.0, 268.0), new DecimalPosition(132.0, 276.0), new DecimalPosition(140.0, 276.0), new DecimalPosition(142.0, 282.0), new DecimalPosition(142.0, 286.0), new DecimalPosition(142.0, 290.0), new DecimalPosition(142.0, 294.0), new DecimalPosition(140.0, 300.0), new DecimalPosition(140.0, 308.0), new DecimalPosition(140.0, 316.0), new DecimalPosition(140.0, 324.0), new DecimalPosition(140.0, 332.0), new DecimalPosition(140.0, 340.0), new DecimalPosition(140.0, 348.0), new DecimalPosition(140.0, 356.0), new DecimalPosition(140.625, 374.25));
    }

    @Test
    @Ignore
    public void landWaterCoast1() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(133.625, 197.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(133.625, 197.25));
    }

    @Test
    @Ignore
    public void landWaterCoast2() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(58.125, 221.000));
        assertSimplePath(simplePath, new DecimalPosition(132.0, 172.0), new DecimalPosition(124.0, 172.0), new DecimalPosition(116.0, 172.0), new DecimalPosition(108.0, 172.0), new DecimalPosition(100.0, 172.0), new DecimalPosition(92.0, 172.0), new DecimalPosition(84.0, 172.0), new DecimalPosition(84.0, 180.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(44.0, 196.0), new DecimalPosition(44.0, 204.0), new DecimalPosition(50.0, 210.0), new DecimalPosition(58.125, 221.0));
    }

    @Test
    @Ignore
    public void landWaterCoast3() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(169.375, 263.750));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(238.0, 270.0), new DecimalPosition(234.0, 270.0), new DecimalPosition(230.0, 270.0), new DecimalPosition(226.0, 270.0), new DecimalPosition(222.0, 270.0), new DecimalPosition(218.0, 270.0), new DecimalPosition(214.0, 270.0), new DecimalPosition(210.0, 270.0), new DecimalPosition(206.0, 270.0), new DecimalPosition(202.0, 270.0), new DecimalPosition(198.0, 270.0), new DecimalPosition(196.0, 276.0), new DecimalPosition(182.0, 270.0), new DecimalPosition(169.375, 263.75));
    }

    @Test
    @Ignore
    public void landWaterCoast4() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(194.125, 362.250));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(142.0, 186.0), new DecimalPosition(146.0, 186.0), new DecimalPosition(150.0, 186.0), new DecimalPosition(154.0, 186.0), new DecimalPosition(158.0, 186.0), new DecimalPosition(162.0, 186.0), new DecimalPosition(166.0, 186.0), new DecimalPosition(170.0, 186.0), new DecimalPosition(174.0, 186.0), new DecimalPosition(178.0, 186.0), new DecimalPosition(182.0, 186.0), new DecimalPosition(186.0, 186.0), new DecimalPosition(190.0, 186.0), new DecimalPosition(194.0, 186.0), new DecimalPosition(198.0, 186.0), new DecimalPosition(202.0, 186.0), new DecimalPosition(206.0, 186.0), new DecimalPosition(210.0, 186.0), new DecimalPosition(214.0, 186.0), new DecimalPosition(218.0, 186.0), new DecimalPosition(222.0, 186.0), new DecimalPosition(226.0, 186.0), new DecimalPosition(230.0, 186.0), new DecimalPosition(234.0, 186.0), new DecimalPosition(238.0, 186.0), new DecimalPosition(244.0, 188.0), new DecimalPosition(244.0, 196.0), new DecimalPosition(244.0, 204.0), new DecimalPosition(244.0, 212.0), new DecimalPosition(244.0, 220.0), new DecimalPosition(244.0, 228.0), new DecimalPosition(244.0, 236.0), new DecimalPosition(244.0, 244.0), new DecimalPosition(244.0, 252.0), new DecimalPosition(244.0, 260.0), new DecimalPosition(244.0, 268.0), new DecimalPosition(244.0, 276.0), new DecimalPosition(244.0, 284.0), new DecimalPosition(244.0, 292.0), new DecimalPosition(244.0, 300.0), new DecimalPosition(244.0, 308.0), new DecimalPosition(244.0, 316.0), new DecimalPosition(244.0, 324.0), new DecimalPosition(244.0, 332.0), new DecimalPosition(244.0, 340.0), new DecimalPosition(252.0, 340.0), new DecimalPosition(252.0, 348.0), new DecimalPosition(252.0, 356.0), new DecimalPosition(252.0, 364.0), new DecimalPosition(252.0, 372.0), new DecimalPosition(244.0, 372.0), new DecimalPosition(236.0, 372.0), new DecimalPosition(228.0, 372.0), new DecimalPosition(220.0, 372.0), new DecimalPosition(212.0, 372.0), new DecimalPosition(204.0, 372.0), new DecimalPosition(196.0, 372.0), new DecimalPosition(194.125, 362.25));
    }

    @Test
    @Ignore
    public void landWaterCoast5() {
        SimplePath simplePath = setupPath(4.0, TerrainType.LAND, new DecimalPosition(140.000, 170.000), 15.0, 3.0, TerrainType.WATER_COAST, new DecimalPosition(118.375, 363.750));
        assertSimplePath(simplePath, new DecimalPosition(140.0, 180.0), new DecimalPosition(138.0, 186.0), new DecimalPosition(134.0, 186.0), new DecimalPosition(130.0, 186.0), new DecimalPosition(126.0, 186.0), new DecimalPosition(122.0, 186.0), new DecimalPosition(118.0, 186.0), new DecimalPosition(114.0, 186.0), new DecimalPosition(110.0, 186.0), new DecimalPosition(106.0, 186.0), new DecimalPosition(102.0, 186.0), new DecimalPosition(98.0, 186.0), new DecimalPosition(94.0, 186.0), new DecimalPosition(90.0, 186.0), new DecimalPosition(86.0, 186.0), new DecimalPosition(82.0, 186.0), new DecimalPosition(78.0, 186.0), new DecimalPosition(74.0, 186.0), new DecimalPosition(70.0, 186.0), new DecimalPosition(66.0, 186.0), new DecimalPosition(62.0, 186.0), new DecimalPosition(58.0, 186.0), new DecimalPosition(52.0, 188.0), new DecimalPosition(50.0, 194.0), new DecimalPosition(44.0, 196.0), new DecimalPosition(44.0, 204.0), new DecimalPosition(44.0, 212.0), new DecimalPosition(44.0, 220.0), new DecimalPosition(44.0, 228.0), new DecimalPosition(44.0, 236.0), new DecimalPosition(44.0, 244.0), new DecimalPosition(44.0, 252.0), new DecimalPosition(44.0, 260.0), new DecimalPosition(42.0, 266.0), new DecimalPosition(36.0, 268.0), new DecimalPosition(36.0, 276.0), new DecimalPosition(36.0, 284.0), new DecimalPosition(36.0, 292.0), new DecimalPosition(36.0, 300.0), new DecimalPosition(36.0, 308.0), new DecimalPosition(36.0, 316.0), new DecimalPosition(36.0, 324.0), new DecimalPosition(36.0, 332.0), new DecimalPosition(36.0, 340.0), new DecimalPosition(36.0, 348.0), new DecimalPosition(36.0, 356.0), new DecimalPosition(36.0, 364.0), new DecimalPosition(36.0, 372.0), new DecimalPosition(42.0, 374.0), new DecimalPosition(46.0, 374.0), new DecimalPosition(50.0, 374.0), new DecimalPosition(54.0, 374.0), new DecimalPosition(58.0, 374.0), new DecimalPosition(62.0, 374.0), new DecimalPosition(66.0, 374.0), new DecimalPosition(70.0, 374.0), new DecimalPosition(74.0, 374.0), new DecimalPosition(78.0, 374.0), new DecimalPosition(82.0, 374.0), new DecimalPosition(86.0, 374.0), new DecimalPosition(90.0, 374.0), new DecimalPosition(94.0, 374.0), new DecimalPosition(98.0, 374.0), new DecimalPosition(102.0, 374.0), new DecimalPosition(106.0, 374.0), new DecimalPosition(110.0, 374.0), new DecimalPosition(114.0, 374.0), new DecimalPosition(118.0, 374.0), new DecimalPosition(118.375, 363.75));
    }

    @Test
    @Ignore
    public void testCaseGenerator() {
        double actorRadius = 2;
        TerrainType actorTerrainType = TerrainType.LAND;
        DecimalPosition actorPosition = new DecimalPosition(180, 100); // Land
        // DecimalPosition actorPosition = new DecimalPosition(200, 180); // Water
        double range = 15;
        double targetRadius = 2;
        TerrainType targetTerrainType = TerrainType.LAND;

        showDisplay(new MouseMoveCallback().setCallback(position -> {
            try {
                SimplePath simplePath = setupPath(actorRadius, actorTerrainType, actorPosition, range, targetRadius, targetTerrainType, position);
                return new Object[]{
                        new PositionMarker()
                                .addCircleColor(new Circle2D(actorPosition, actorRadius), new Color(0.1, 0.1, 0.1, 0.5))
                                .addCircleColor(new Circle2D(position, targetRadius), new Color(0.1, 0.4, 0.9, 0.5)),
                        simplePath};
            } catch (Exception e) {
                System.out.println("--- EXCEPTION: " + e.getMessage());
            }
            return new Object[]{new PositionMarker()
                    .addCircleColor(new Circle2D(actorPosition, actorRadius), new Color(0.1, 0.1, 0.1, 0.5))
                    .addCircleColor(new Circle2D(position, targetRadius), new Color(0.9, 0.1, 0.9, 0.5))};
        }), new TestCaseGenerator().setTestCaseName("landWaterCoast").setTestGeneratorCallback((position, body) -> {
            SimplePath simplePath = setupPath(actorRadius, actorTerrainType, actorPosition, range, targetRadius, targetTerrainType, position);
            body.appendLine("SimplePath simplePath = setupPath(" + actorRadius + ", " + InstanceStringGenerator.generate(actorTerrainType) + ", " + InstanceStringGenerator.generate(actorPosition) + ", " + range + ", " + targetRadius + ", " + InstanceStringGenerator.generate(targetTerrainType) + ", " + InstanceStringGenerator.generate(position) + ");");
            body.appendLine("assertSimplePath(simplePath, "  /*simplePath.getTotalRange() + ", "*/ + TestHelper.decimalPositionsToString(simplePath.getWayPositions()) + ");");
        }));
    }

    protected void assertSimplePath(SimplePath actual, DecimalPosition... expectedPosition) {
        printSimplePath(actual);
        // showDisplay(actual, new SimplePath().wayPositions(Arrays.asList(expectedPosition)));
        TestHelper.assertDecimalPositions(Arrays.asList(expectedPosition), actual.getWayPositions());
    }

    protected void printSimplePath(SimplePath simplePath) {
        System.out.println("assertSimplePath(simplePath, " /*+ simplePath.getTotalRange() + ", "*/ + TestHelper.decimalPositionsToString(simplePath.getWayPositions()) + ");");
    }
}