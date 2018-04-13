package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.utils.CollectionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 09.04.2017.
 */
public class AssertTerrainTile {
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "terrain";
    // public static final String SAVE_DIRECTORY = "C:\\dev\\projects\\razarion\\code\\razarion2\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain";
    private Collection<TerrainTile> expected;
    private DifferenceCollector differenceCollector;

    public AssertTerrainTile(Class theClass, String resourceName) {
        this(theClass, resourceName, null);
    }

    public AssertTerrainTile(Class theClass, String resourceName, DifferenceCollector differenceCollector) {
        this.differenceCollector = differenceCollector;
        InputStream inputStream = theClass.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new RuntimeException("Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + resourceName);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            expected = objectMapper.readValue(inputStream, new TypeReference<List<TestTerrainTile>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (expected.isEmpty()) {
            throw new RuntimeException("expected.isEmpty()");
        }
    }

    public void assertEquals(Collection<TerrainTile> actual) {
        if (expected.size() != actual.size()) {
            Assert.fail("Expected size does not match one single TerrainTile. Expected size: " + expected.size());
        }

        for (TerrainTile expectedTile : expected) {
            boolean found = false;
            for (TerrainTile actualTile : actual) {
                if (expectedTile.getIndexX() == actualTile.getIndexX() && expectedTile.getIndexY() == actualTile.getIndexY()) {
                    compare(expectedTile, actualTile);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assert.fail("Terrain file not found in actual. Index X: " + expectedTile.getIndexX() + " Index Y:" + expectedTile.getIndexY());
            }
        }
    }

    public void assertEquals(TerrainTile actual) {
        if (expected.size() != 1) {
            Assert.fail("Expected size does not match one single TerrainTile. Expected size: " + expected.size());
        }
        compare(CollectionUtils.getFirst(expected), actual);
    }

    private void compare(TerrainTile expected, TerrainTile actual) {
        Index tileIndex = new Index(expected.getIndexX(), actual.getIndexX());
        // Ground
        Assert.assertEquals("Index X", expected.getIndexX(), actual.getIndexX());
        Assert.assertEquals("Index Y", expected.getIndexY(), actual.getIndexY());
        if (differenceCollector != null) {
            differenceCollector.compareArray("Ground Vertices", expected.getGroundVertices(), actual.getGroundVertices(), 0.001);
        } else {
            Assert.assertArrayEquals("Ground Vertices", expected.getGroundVertices(), actual.getGroundVertices(), 0.001);
            Assert.assertArrayEquals("Ground Norms", expected.getGroundNorms(), actual.getGroundNorms(), 0.001);
            Assert.assertArrayEquals("Ground Tangents", expected.getGroundTangents(), actual.getGroundTangents(), 0.001);
            Assert.assertArrayEquals("Ground Splattings", expected.getGroundSplattings(), actual.getGroundSplattings(), 0.001);
            Assert.assertEquals("Ground Vertex Count", expected.getGroundVertexCount(), actual.getGroundVertexCount());
            Assert.assertEquals("Height", expected.getHeight(), actual.getHeight(), 0.001);
            Assert.assertEquals("LandWaterProportion", expected.getLandWaterProportion(), actual.getLandWaterProportion(), 0.001);
        }
        // Slope
        int expectedSlopeTileCount = 0;
        if (expected.getTerrainSlopeTiles() != null) {
            expectedSlopeTileCount = expected.getTerrainSlopeTiles().length;
        }
        int actualSlopeTileCount = 0;
        if (actual.getTerrainSlopeTiles() != null) {
            actualSlopeTileCount = expected.getTerrainSlopeTiles().length;
        }
        Assert.assertEquals("TerrainSlopeTiles length different", expectedSlopeTileCount, actualSlopeTileCount);
        if (expectedSlopeTileCount != 0) {
            for (int i = 0; i < expected.getTerrainSlopeTiles().length; i++) {
                compare(expected.getTerrainSlopeTiles()[i], actual.getTerrainSlopeTiles()[i]);
            }
        }
        // Water
        if (expected.getTerrainWaterTile() != null || expected.getTerrainWaterTile() != null) {
            if (expected.getTerrainWaterTile() != null && expected.getTerrainWaterTile() != null) {
                compare(expected.getTerrainWaterTile(), actual.getTerrainWaterTile());
            } else {
                Assert.fail("TerrainWaterTile is invalid. Expected: " + expected.getTerrainWaterTile() + " Actual: " + actual.getTerrainWaterTile());
            }
        }
        // Terrain nodes
        if (expected.getTerrainNodes() != null && actual.getTerrainNodes() != null) {
            for (int x = 0; x < TerrainUtil.TERRAIN_TILE_NODES_COUNT; x++) {
                for (int y = 0; y < TerrainUtil.TERRAIN_TILE_NODES_COUNT; y++) {
                    compare(expected.getTerrainNodes()[x][y], actual.getTerrainNodes()[x][y], TerrainUtil.toNodeAbsolute(TerrainUtil.tileToNode(new Index(expected.getIndexX(), expected.getIndexY())).add(x, y)));
                }
            }
        } else if (expected.getTerrainNodes() != null) {
            Assert.fail("TerrainWaterTile expected.getTerrainNodes() != null && actual.getTerrainNodes() == null");
        } else if (actual.getTerrainNodes() != null) {
            Assert.fail("TerrainWaterTile expected.getTerrainNodes() == null && actual.getTerrainNodes() != null");
        }
        // Terrain objects
        if (expected.getTerrainTileObjectLists() != null && actual.getTerrainTileObjectLists() != null) {
            compare(tileIndex, expected.getTerrainTileObjectLists(), actual.getTerrainTileObjectLists());
        } else if (expected.getTerrainTileObjectLists() != null) {
            Assert.fail("expected.getTerrainTileObjectLists() != null && actual.getTerrainTileObjectLists() == null");
        } else if (actual.getTerrainTileObjectLists() != null) {
            Assert.fail("expected.getTerrainTileObjectLists() == null && actual.getTerrainTileObjectLists() != null");
        }
    }

    private void compare(TerrainSlopeTile expected, TerrainSlopeTile actual) {
        Assert.assertEquals("Slope Skeleton Config Id", expected.getSlopeSkeletonConfigId(), actual.getSlopeSkeletonConfigId());
        Assert.assertArrayEquals("Slope Vertices", expected.getVertices(), actual.getVertices(), 0.001);
        Assert.assertArrayEquals("Slope Norms", expected.getNorms(), actual.getNorms(), 0.001);
        Assert.assertArrayEquals("Slope Tangents", expected.getTangents(), actual.getTangents(), 0.001);
        Assert.assertArrayEquals("Slope Factor", expected.getSlopeFactors(), actual.getSlopeFactors(), 0.001);
        Assert.assertArrayEquals("Slope Ground Splattings", expected.getGroundSplattings(), actual.getGroundSplattings(), 0.001);
        Assert.assertEquals("Slope Vertex Count", expected.getSlopeVertexCount(), actual.getSlopeVertexCount());
    }

    private void compare(TerrainWaterTile expected, TerrainWaterTile actual) {
        Assert.assertArrayEquals("Water Vertices", expected.getVertices(), actual.getVertices(), 0.001);
        Assert.assertEquals("Water Vertex Count", expected.getVertexCount(), actual.getVertexCount());
    }

    private void compare(TerrainNode expected, TerrainNode actual, DecimalPosition absoluteStart) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainNode expected != null && TerrainNode actual == null. At: " + absoluteStart);
        }
        if (expected == null) {
            Assert.fail("TerrainNode expected == null && TerrainNode actual != null. At: " + absoluteStart);
        }
        assertTerrainType("TerrainNode TerrainType. At: " + absoluteStart, expected.getTerrainType(), actual.getTerrainType());
        Assert.assertEquals("TerrainNode Height: " + absoluteStart, expected.getHeight(), actual.getHeight(), 0.001);

        compare(expected.getTerrainSubNodes(), actual.getTerrainSubNodes(), absoluteStart);
    }

    private void compare(TerrainSubNode[][] expected, TerrainSubNode[][] actual, DecimalPosition absoluteStart) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainSubNode[][] expected != null && TerrainSubNode[][] actual == null. At: " + absoluteStart);
        }
        if (expected == null) {
            Assert.fail("TerrainSubNode[][] expected == null && TerrainSubNode[][] actual != null. At: " + absoluteStart);
        }
        compare(expected[0][0], expected[0][0], absoluteStart);
        compare(expected[0][1], expected[0][1], absoluteStart);
        compare(expected[1][0], expected[1][0], absoluteStart);
        compare(expected[1][1], expected[1][1], absoluteStart);
    }

    private void compare(TerrainSubNode expected, TerrainSubNode actual, DecimalPosition absoluteStart) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainSubNode expected != null && TerrainSubNode actual == null. At: " + absoluteStart);
        }
        if (expected == null) {
            Assert.fail("TerrainSubNode expected == null && TerrainSubNode actual != null. At: " + absoluteStart);
        }

        assertTerrainType("TerrainNode TerrainType. At: " + absoluteStart, expected.getTerrainType(), actual.getTerrainType());
        Assert.assertEquals("TerrainNode Height. At: " + absoluteStart, expected.getHeight(), actual.getHeight(), 0.001);

        compare(expected.getTerrainSubNodes(), actual.getTerrainSubNodes(), absoluteStart);
    }

    private void compare(Index tileIndex, TerrainTileObjectList[] expectedObjectLists, TerrainTileObjectList[] actualObjectLists1) {
        Assert.assertEquals("TerrainTileObjectList length. tileIndex: " + tileIndex, expectedObjectLists.length, actualObjectLists1.length);
        for (int i = 0; i < expectedObjectLists.length; i++) {
            compare(tileIndex, expectedObjectLists[i], actualObjectLists1[i]);
        }
    }

    private void compare(Index tileIndex, TerrainTileObjectList expectedObjectList, TerrainTileObjectList actualObjectList) {
        Assert.assertEquals("getTerrainObjectConfigId. tileIndex: " + tileIndex, expectedObjectList.getTerrainObjectConfigId(), actualObjectList.getTerrainObjectConfigId());
        if (expectedObjectList.getModels() == null && actualObjectList.getModels() == null) {
            Assert.fail("Not expected");
        }
        if (expectedObjectList.getModels() != null && actualObjectList.getModels() == null) {
            Assert.fail("expectedObjectList.getModels() != null && actualObjectList.getModels() == null. tileIndex: " + tileIndex);
        }
        if (expectedObjectList.getModels() == null) {
            Assert.fail("expected == null. tileIndex: " + tileIndex);
        }
        Assert.assertEquals("TerrainTileObjectList length. tileIndex: " + tileIndex, expectedObjectList.getModels().length, actualObjectList.getModels().length);
        for (int i = 0; i < expectedObjectList.getModels().length; i++) {
            compare(tileIndex, expectedObjectList.getModels()[i], actualObjectList.getModels()[i]);
        }
    }

    private void compare(Index tileIndex, NativeMatrix expected, NativeMatrix actual) {
        if (expected == null && actual == null) {
            Assert.fail("Not expected. tileIndex: " + tileIndex);
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null. tileIndex: " + tileIndex);
        }
        if (expected == null) {
            Assert.fail("expected == null. tileIndex: " + tileIndex);
        }
        Assert.assertArrayEquals("tileIndex: " + tileIndex, expected.toColumnMajorArray(), actual.toColumnMajorArray(), 0.0001);
    }

    public static void saveTerrainTiles(Collection<TerrainTile> terrainTiles, String fileName) {
        try {
            new ObjectMapper().writeValue(new File(SAVE_DIRECTORY, fileName), terrainTiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveTerrainTile(TerrainTile terrainTile, String fileName) {
        saveTerrainTiles(Collections.singletonList(terrainTile), fileName);
    }

    public static void assertNormTangentTerrainTile(double[] norms, double[] tangents) {
        for (int i = 0; i < norms.length; i++) {
            Vertex norm = TestHelper.createVertex(norms, i / 3);
            Vertex tangent = TestHelper.createVertex(tangents, i / 3);
            Assert.assertTrue("dot too big at: " + i + " dot:" + norm.dot(tangent), Math.abs(norm.dot(tangent)) < 0.00001);
        }
    }

    private static void assertTerrainType(String message, int expected, int actual) {
        if ((expected == 0 || expected == -1) && (actual == 0 || actual == -1)) {
            return;
        }
        Assert.assertEquals(message, expected, actual);
    }
}
