package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeVertex;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Beat
 * on 02.10.2017.
 */
public interface AssertTerrainShape {
    static void assertTerrainShape(Class theClass, String resourceName, TerrainShape actualTerrainShape) {
        InputStream inputStream = theClass.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + resourceName);
        }
        NativeTerrainShape expected;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            expected = objectMapper.readValue(inputStream, NativeTerrainShape.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        compareTerrainShape(expected, actualTerrainShape.toNativeTerrainShape());
    }

    static void compareTerrainShape(NativeTerrainShape expected, NativeTerrainShape actual) {
        Assert.assertEquals("tileXCount", expected.tileXCount, actual.tileXCount);
        Assert.assertEquals("tileYCount", expected.tileYCount, actual.tileYCount);
        Assert.assertEquals("tileXOffset", expected.tileXOffset, actual.tileXOffset);
        Assert.assertEquals("tileYOffset", expected.tileYOffset, actual.tileYOffset);

        if (expected.nativeTerrainShapeTiles == null && actual.nativeTerrainShapeTiles == null) {
            return;
        }
        if (expected.nativeTerrainShapeTiles != null && actual.nativeTerrainShapeTiles == null) {
            Assert.fail("expected.nativeTerrainShapeTiles != null && actual.nativeTerrainShapeTiles == null");
        }
        if (expected.nativeTerrainShapeTiles == null) {
            Assert.fail("expected.nativeTerrainShapeTiles == null");
        }

        Assert.assertEquals("nativeTerrainShapeTiles.length", expected.nativeTerrainShapeTiles.length, actual.nativeTerrainShapeTiles.length);

        for (int x = 0; x < expected.nativeTerrainShapeTiles.length; x++) {
            Assert.assertEquals(expected.nativeTerrainShapeTiles[x].length, actual.nativeTerrainShapeTiles[x].length);
            for (int y = 0; y < expected.nativeTerrainShapeTiles[x].length; y++) {
                compareTerrainTile(expected.nativeTerrainShapeTiles[x][y], actual.nativeTerrainShapeTiles[x][y]);
            }
        }
    }

    static void compareTerrainTile(NativeTerrainShapeTile expectedTile, NativeTerrainShapeTile actualTile) {
        if (expectedTile == null && actualTile == null) {
            return;
        }
        if (expectedTile != null && actualTile == null) {
            Assert.fail("expectedTile != null && actualTile == null");
        }
        if (expectedTile == null) {
            Assert.fail("expectedTile == null");
        }

        TestHelper.assertDouble("fullWaterLevel", expectedTile.fullWaterLevel, actualTile.fullWaterLevel);
        TestHelper.assertDouble("uniformGroundHeight", expectedTile.uniformGroundHeight, actualTile.uniformGroundHeight);

        if (expectedTile.fractionalSlopes != null && actualTile.fractionalSlopes == null) {
            Assert.fail("expectedTile.fractionalSlopes != null && actualTile.fractionalSlopes == null");
        }
        if (expectedTile.fractionalSlopes == null && actualTile.fractionalSlopes != null) {
            Assert.fail("expectedTile.fractionalSlopes == null && actualTile.fractionalSlopes != null");
        }
        if (expectedTile.fractionalSlopes != null) {
            Assert.assertEquals("fractionalSlopes.length", expectedTile.fractionalSlopes.length, actualTile.fractionalSlopes.length);
            for (int i = 0; i < expectedTile.fractionalSlopes.length; i++) {
                compareFractionalSlope(expectedTile.fractionalSlopes[i], actualTile.fractionalSlopes[i]);
            }
        }

        if (expectedTile.nativeTerrainShapeNodes != null && actualTile.nativeTerrainShapeNodes == null) {
            Assert.fail("expectedTile.nativeTerrainShapeNodes != null && actualTile.nativeTerrainShapeNodes == null");
        }
        if (expectedTile.nativeTerrainShapeNodes == null && actualTile.nativeTerrainShapeNodes != null) {
            Assert.fail("expectedTile.nativeTerrainShapeNodes == null && actualTile.nativeTerrainShapeNodes != null");
        }
        if (expectedTile.nativeTerrainShapeNodes != null) {
            Assert.assertEquals("nativeTerrainShapeNodes.length", expectedTile.nativeTerrainShapeNodes.length, actualTile.nativeTerrainShapeNodes.length);
            for (int x = 0; x < expectedTile.nativeTerrainShapeNodes.length; x++) {
                Assert.assertEquals(expectedTile.nativeTerrainShapeNodes[x].length, actualTile.nativeTerrainShapeNodes[x].length);
                for (int y = 0; y < expectedTile.nativeTerrainShapeNodes[x].length; y++) {
                    compareTerrainShapeNode(expectedTile.nativeTerrainShapeNodes[x][y], actualTile.nativeTerrainShapeNodes[x][y]);
                }
            }
        }

        if (expectedTile.nativeTerrainShapeObjectLists != null && actualTile.nativeTerrainShapeObjectLists == null) {
            Assert.fail("expectedTile.nativeTerrainShapeObjectLists != null && actualTile.nativeTerrainShapeObjectLists == null");
        }
        if (expectedTile.nativeTerrainShapeObjectLists == null && actualTile.nativeTerrainShapeObjectLists != null) {
            Assert.fail("expectedTile.nativeTerrainShapeObjectLists == null && actualTile.nativeTerrainShapeObjectLists != null");
        }
        if (expectedTile.nativeTerrainShapeObjectLists != null) {
            Assert.assertEquals("nativeTerrainShapeObjectLists.length", expectedTile.nativeTerrainShapeObjectLists.length, actualTile.nativeTerrainShapeObjectLists.length);
            for (int i = 0; i < expectedTile.nativeTerrainShapeObjectLists.length; i++) {
                compareTerrainShapeObjectList(expectedTile.nativeTerrainShapeObjectLists[i], actualTile.nativeTerrainShapeObjectLists[i]);
            }
        }

    }

    static void compareFractionalSlope(NativeFractionalSlope expectedFractionalSlope, NativeFractionalSlope actualFractionalSlope1) {
        Assert.assertEquals("slopeSkeletonConfigId", expectedFractionalSlope.slopeSkeletonConfigId, actualFractionalSlope1.slopeSkeletonConfigId);
        Assert.assertEquals("groundHeight", expectedFractionalSlope.groundHeight, actualFractionalSlope1.groundHeight, 0.001);
        Assert.assertEquals("inverted", expectedFractionalSlope.inverted, actualFractionalSlope1.inverted);

        if (expectedFractionalSlope.fractionalSlopeSegments != null && actualFractionalSlope1.fractionalSlopeSegments == null) {
            Assert.fail("expectedFractionalSlope.fractionalSlopeSegments != null && actualFractionalSlope1.fractionalSlopeSegments == null");
        }
        if (expectedFractionalSlope.fractionalSlopeSegments == null && actualFractionalSlope1.fractionalSlopeSegments != null) {
            Assert.fail("expectedFractionalSlope.fractionalSlopeSegments == null && actualFractionalSlope1.fractionalSlopeSegments != null");
        }
        if (expectedFractionalSlope.fractionalSlopeSegments != null) {
            Assert.assertEquals("fractionalSlopeSegments.length", expectedFractionalSlope.fractionalSlopeSegments.length, actualFractionalSlope1.fractionalSlopeSegments.length);

            for (int i = 0; i < expectedFractionalSlope.fractionalSlopeSegments.length; i++) {
                compareFractionalSlopeSegment(expectedFractionalSlope.fractionalSlopeSegments[i], actualFractionalSlope1.fractionalSlopeSegments[i]);
            }
        }

    }

    static void compareFractionalSlopeSegment(NativeFractionalSlopeSegment expectedSegment, NativeFractionalSlopeSegment actualSlopeSegment1) {
        Assert.assertEquals("xI", expectedSegment.xI, actualSlopeSegment1.xI, 0.001);
        Assert.assertEquals("yI", expectedSegment.yI, actualSlopeSegment1.yI, 0.001);
        Assert.assertEquals("xO", expectedSegment.xO, actualSlopeSegment1.xO, 0.001);
        Assert.assertEquals("yO", expectedSegment.yO, actualSlopeSegment1.yO, 0.001);
        Assert.assertEquals("index", expectedSegment.index, actualSlopeSegment1.index);
        TestHelper.assertDouble("drivewayHeightFactor", expectedSegment.drivewayHeightFactor, actualSlopeSegment1.drivewayHeightFactor);
    }

    static void compareTerrainShapeNode(NativeTerrainShapeNode expectedNode, NativeTerrainShapeNode actualNode1) {
        if (expectedNode == null && actualNode1 == null) {
            return;
        }
        if (expectedNode != null && actualNode1 == null) {
            Assert.fail("expectedNode != null && actualNode1 == null");
        }
        if (expectedNode == null) {
            Assert.fail("expectedNode == null");
        }

        TestHelper.assertDoubleArray("fullDrivewayHeights", expectedNode.fullDrivewayHeights, actualNode1.fullDrivewayHeights);
        // TODO TestHelper.assertDouble("gameEngineHeight", expectedNode.gameEngineHeight, actualNode1.gameEngineHeight);
        // TODO TestHelper.assertDouble("renderEngineHeight", expectedNode.renderEngineHeight, actualNode1.renderEngineHeight);
        TestHelper.assertDouble("fullWaterLevel", expectedNode.fullWaterLevel, actualNode1.fullWaterLevel);
        Assert.assertEquals("terrainTypeOrdinal", expectedNode.terrainTypeOrdinal, actualNode1.terrainTypeOrdinal);
        // TODO Assert.assertEquals("doNotRenderGround", expectedNode.doNotRenderGround, actualNode1.doNotRenderGround);
        Assert.assertEquals("drivewayBreakingLine", expectedNode.drivewayBreakingLine, actualNode1.drivewayBreakingLine);
        Assert.assertEquals("fullGameEngineDriveway", expectedNode.fullGameEngineDriveway, actualNode1.fullGameEngineDriveway);
        Assert.assertEquals("fullRenderEngineDriveway", expectedNode.fullRenderEngineDriveway, actualNode1.fullRenderEngineDriveway);
        // TODO assertNativeVertex("groundSlopeConnections", expectedNode.groundSlopeConnections, actualNode1.groundSlopeConnections);
        // TODO assertNativeVertex("waterSegments", expectedNode.waterSegments, actualNode1.waterSegments);
        assertNativeObstacles("obstacles", expectedNode.obstacles, actualNode1.obstacles);
        assertNativeTerrainShapeSubNode("nativeTerrainShapeSubNodes", expectedNode.nativeTerrainShapeSubNodes, actualNode1.nativeTerrainShapeSubNodes);
    }

    static void assertNativeVertex(String message, NativeVertex[][] expected, NativeVertex[][] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null: " + message);
        }
        if (expected == null) {
            Assert.fail("expected == null: " + message);
        }

        Assert.assertEquals("expected.length: " + message, expected.length, actual.length);
        for (int x = 0; x < expected.length; x++) {
            Assert.assertEquals(expected[x].length, actual[x].length);
            for (int y = 0; y < expected[x].length; y++) {
                Assert.assertEquals(expected[x][y].x, actual[x][y].x, 0.001);
                Assert.assertEquals(expected[x][y].y, actual[x][y].y, 0.001);
                Assert.assertEquals(expected[x][y].z, actual[x][y].z, 0.001);
            }
        }
    }

    static void assertNativeObstacles(String message, NativeObstacle[] expected, NativeObstacle[] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null: " + message);
        }
        if (expected == null) {
            Assert.fail("expected == null: " + message);
        }

        Assert.assertEquals("length: " + message, expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            TestHelper.assertDouble("x1: " + message, expected[i].x1, actual[i].x1);
            TestHelper.assertDouble("y1: " + message, expected[i].y1, actual[i].y1);
            TestHelper.assertDouble("x2: " + message, expected[i].x2, actual[i].x2);
            TestHelper.assertDouble("y2: " + message, expected[i].y2, actual[i].y2);
            TestHelper.assertDouble("xC: " + message, expected[i].xC, actual[i].xC);
            TestHelper.assertDouble("yC: " + message, expected[i].yC, actual[i].yC);
            TestHelper.assertDouble("r: " + message, expected[i].r, actual[i].r);
        }
    }

    static void saveTerrainShape(TerrainShape terrainShape, String fileName) {
        try {
            new ObjectMapper().writeValue(new File(AssertTerrainTile.SAVE_DIRECTORY, fileName), terrainShape.toNativeTerrainShape());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void assertNativeTerrainShapeSubNode(String message, NativeTerrainShapeSubNode[] expected, NativeTerrainShapeSubNode[] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null: " + message);
        }
        if (expected == null) {
            Assert.fail("expected == null: " + message);
        }

        Assert.assertEquals("length: " + message, expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("terrainTypeOrdinal: " + message, expected[i].terrainTypeOrdinal, actual[i].terrainTypeOrdinal);
            TestHelper.assertDouble("height: " + message, expected[i].height, actual[i].height);
            TestHelper.assertDoubleArray("drivewayHeights: " + message, expected[i].drivewayHeights, actual[i].drivewayHeights);
            assertNativeTerrainShapeSubNode("nativeTerrainShapeSubNodes: " + message, expected[i].nativeTerrainShapeSubNodes, actual[i].nativeTerrainShapeSubNodes);
        }
    }


    static void compareTerrainShapeObjectList(NativeTerrainShapeObjectList expectedTerrainShapeObjectList, NativeTerrainShapeObjectList actualTerrainShapeObjectList) {
        Assert.assertNotNull(expectedTerrainShapeObjectList);
        Assert.assertNotNull(actualTerrainShapeObjectList);
        Assert.assertEquals(expectedTerrainShapeObjectList.terrainObjectId, actualTerrainShapeObjectList.terrainObjectId);
        Assert.assertEquals(expectedTerrainShapeObjectList.positions.length, actualTerrainShapeObjectList.positions.length);
        for (int i = 0; i < expectedTerrainShapeObjectList.positions.length; i++) {
            Assert.assertEquals(expectedTerrainShapeObjectList.positions[i].x, actualTerrainShapeObjectList.positions[i].x, 0.0001);
            Assert.assertEquals(expectedTerrainShapeObjectList.positions[i].y, actualTerrainShapeObjectList.positions[i].y, 0.0001);
            Assert.assertEquals(expectedTerrainShapeObjectList.positions[i].scale, actualTerrainShapeObjectList.positions[i].scale, 0.0001);
            Assert.assertEquals(expectedTerrainShapeObjectList.positions[i].rotationZ, actualTerrainShapeObjectList.positions[i].rotationZ, 0.0001);
        }
    }

}
