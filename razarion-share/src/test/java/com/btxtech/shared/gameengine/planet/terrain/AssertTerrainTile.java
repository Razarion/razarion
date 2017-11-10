package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.gui.teraintile.TerrainTileTestDisplay;
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
    private static final boolean SHOW_GUI = false;
    private static final String SAVE_DIRECTORY = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain";
    private Collection<TerrainTile> expected;

    public AssertTerrainTile(Class theClass, String resourceName) {
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
        if (SHOW_GUI) {
            TerrainTileTestDisplay.show(expected, actual);
        }

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
        // Ground
        Assert.assertEquals("Index X", expected.getIndexX(), actual.getIndexX());
        Assert.assertEquals("Index Y", expected.getIndexY(), actual.getIndexY());
        Assert.assertArrayEquals("Ground Vertices", expected.getGroundVertices(), actual.getGroundVertices(), 0.001);
        Assert.assertArrayEquals("Ground Norms", expected.getGroundNorms(), actual.getGroundNorms(), 0.001);
        Assert.assertArrayEquals("Ground Tangents", expected.getGroundTangents(), actual.getGroundTangents(), 0.001);
        Assert.assertArrayEquals("Ground Splattings", expected.getGroundSplattings(), actual.getGroundSplattings(), 0.001);
        Assert.assertEquals("Ground Vertex Count", expected.getGroundVertexCount(), actual.getGroundVertexCount());
        Assert.assertEquals("Height", expected.getHeight(), actual.getHeight(), 0.001);
        Assert.assertEquals("LandWaterProportion", expected.getLandWaterProportion(), actual.getLandWaterProportion(), 0.001);
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
                    compare(expected.getTerrainNodes()[x][y], actual.getTerrainNodes()[x][y]);
                }
            }
        } else if (expected.getTerrainNodes() != null) {
            Assert.fail("TerrainWaterTile expected.getTerrainNodes() != null && actual.getTerrainNodes() == null");
        } else if (actual.getTerrainNodes() != null) {
            Assert.fail("TerrainWaterTile expected.getTerrainNodes() == null && actual.getTerrainNodes() != null");
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

    private void compare(TerrainNode expected, TerrainNode actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainNode expected != null && TerrainNode actual == null");
        }
        if (expected == null) {
            Assert.fail("TerrainNode expected == null && TerrainNode actual != null");
        }
        Assert.assertEquals("TerrainNode TerrainType", expected.getTerrainType(), actual.getTerrainType());
        Assert.assertEquals("TerrainNode Height", expected.getHeight(), actual.getHeight(), 0.001);

        compare(expected.getTerrainSubNodes(), actual.getTerrainSubNodes());
    }

    private void compare(TerrainSubNode[][] expected, TerrainSubNode[][] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainSubNode[][] expected != null && TerrainSubNode[][] actual == null");
        }
        if (expected == null) {
            Assert.fail("TerrainSubNode[][] expected == null && TerrainSubNode[][] actual != null");
        }
        compare(expected[0][0], expected[0][0]);
    }

    private void compare(TerrainSubNode expected, TerrainSubNode actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("TerrainSubNode expected != null && TerrainSubNode actual == null");
        }
        if (expected == null) {
            Assert.fail("TerrainSubNode expected == null && TerrainSubNode actual != null");
        }

        Assert.assertEquals("TerrainNode TerrainType", expected.getTerrainType(), actual.getTerrainType());
        Assert.assertEquals("TerrainNode Height", expected.getHeight(), actual.getHeight(), 0.001);

        compare(expected.getTerrainSubNodes(), actual.getTerrainSubNodes());
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
}
