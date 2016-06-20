package com.btxtech.server.collada;

import com.btxtech.TestHelper;
import com.btxtech.server.itemtype.ItemTypeEntity;
import com.btxtech.server.terrain.object.TerrainObjectEntity;
import com.btxtech.server.terrain.object.TerrainObjectMaterialEntity;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Color;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testReadItemType1() throws Exception {
        ItemTypeEntity itemTypeEntity = new ItemTypeEntity();
        TestHelper.setPrivateField(itemTypeEntity, "id", 12L);
        TestHelper.setPrivateField(itemTypeEntity, "colladaString", IOUtils.toString(getClass().getResourceAsStream("/collada/TestItemType1.dae")));
        ItemType itemType = ColladaConverter.convertToItemType(itemTypeEntity);
        Assert.assertEquals(12, itemType.getId());
        Assert.assertEquals(99, itemType.getVertexContainer().getVerticesCount());
        Assert.assertArrayEquals(new double[]{9.41, 5.54, 11.80, 10.50, 6.21, 7.00, 4.00, 7.00, 7.00, -11.00, 7.14, 6.00, -9.50, 6.24, 7.50, 4.00, 7.00, 7.00, -1.00, 4.39, 12.00, 3.65, 5.50, 12.00, 2.67, 6.32, 8.50, 4.00, 5.90, 12.00, 4.00, 7.00, 7.00, 2.67, 6.32, 8.50, 2.67, 6.32, 8.50, 2.67, 6.32, 8.50, 3.65, 5.50, 12.00, 4.00, 5.90, 12.00, 4.47, 5.87, 12.30, 9.41, 5.54, 11.80, 9.41, 5.54, 11.80, 4.00, 7.00, 7.00, 4.00, 5.90, 12.00, 4.00, 7.00, 7.00, 10.50, 6.21, 7.00, 11.00, 7.10, 6.00, -2.79, 6.13, 8.50, 2.67, 6.32, 8.50, 4.00, 7.00, 7.00, 4.00, 7.00, 7.00, -9.50, 6.24, 7.50, -2.79, 6.13, 8.50, 4.00, 7.00, 7.00, 11.00, 7.10, 6.00, -11.00, 7.14, 6.00, -2.79, 6.13, 8.50, -1.00, 4.39, 12.00, 2.67, 6.32, 8.50, 3.65, 5.50, 12.00, 4.00, 5.90, 12.00, 2.67, 6.32, 8.50, 3.65, 5.50, 12.00, 2.67, 6.32, 8.50, 3.65, 5.50, 12.00, -4.50, 4.43, 1.36, -5.30, 4.43, 0.47, -5.30, 6.37, 0.47, -5.30, 4.43, 0.47, -6.40, 4.43, -0.02, -6.40, 6.37, -0.02, -6.40, 4.43, -0.02, -7.60, 4.43, -0.02, -7.60, 6.37, -0.02, -9.14, 6.37, 4.73, -4.50, 6.37, 1.36, -8.70, 6.37, 0.47, -4.50, 6.37, 1.36, -4.50, 4.43, 1.36, -5.30, 6.37, 0.47, -5.30, 6.37, 0.47, -5.30, 4.43, 0.47, -6.40, 6.37, -0.02, -6.40, 6.37, -0.02, -6.40, 4.43, -0.02, -7.60, 6.37, -0.02, -9.14, 6.37, 4.73, -8.17, 6.37, 5.44, -7.00, 6.37, 5.68, -9.87, 6.37, 2.50, -9.74, 6.37, 3.69, -9.14, 6.37, 4.73, -9.14, 6.37, 4.73, -9.50, 6.37, 1.36, -9.87, 6.37, 2.50, -6.40, 6.37, -0.02, -7.60, 6.37, -0.02, -8.70, 6.37, 0.47, -8.70, 6.37, 0.47, -5.30, 6.37, 0.47, -6.40, 6.37, -0.02, -4.26, 6.37, 3.69, -4.13, 6.37, 2.50, -4.50, 6.37, 1.36, -5.83, 6.37, 5.44, -4.86, 6.37, 4.73, -4.26, 6.37, 3.69, -9.14, 6.37, 4.73, -7.00, 6.37, 5.68, -5.83, 6.37, 5.44, -8.70, 6.37, 0.47, -9.50, 6.37, 1.36, -9.14, 6.37, 4.73, -4.50, 6.37, 1.36, -5.30, 6.37, 0.47, -8.70, 6.37, 0.47, -5.83, 6.37, 5.44, -4.26, 6.37, 3.69, -4.50, 6.37, 1.36, -4.50, 6.37, 1.36, -9.14, 6.37, 4.73, -5.83, 6.37, 5.44}, TestHelper.vertices2DoubleArray(itemType.getVertexContainer().getVertices()), 0.01);
        Assert.assertArrayEquals(new double[]{0.12, 0.98, 0.16, 0.12, 0.98, 0.16, 0.12, 0.98, 0.16, -0.03, 0.84, 0.54, -0.03, 0.84, 0.54, -0.03, 0.84, 0.54, -0.22, 0.93, 0.28, -0.22, 0.93, 0.28, -0.22, 0.93, 0.28, -0.25, 0.95, 0.21, -0.25, 0.95, 0.21, -0.25, 0.95, 0.21, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.07, 1.00, 0.02, 0.07, 1.00, 0.02, 0.07, 1.00, 0.02, 0.07, 0.97, 0.21, 0.07, 0.97, 0.21, 0.07, 0.97, 0.21, 0.09, 0.72, 0.69, 0.09, 0.72, 0.69, 0.09, 0.72, 0.69, -0.03, 0.92, 0.39, -0.03, 0.92, 0.39, -0.03, 0.92, 0.39, -0.04, 0.93, 0.36, -0.04, 0.93, 0.36, -0.04, 0.93, 0.36, 0.00, 0.99, 0.11, 0.00, 0.99, 0.11, 0.00, 0.99, 0.11, -0.03, 0.89, 0.46, -0.03, 0.89, 0.46, -0.03, 0.89, 0.46, -0.71, 0.61, 0.34, -0.71, 0.61, 0.34, -0.71, 0.61, 0.34, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.74, 0.00, -0.67, 0.74, 0.00, -0.67, 0.74, 0.00, -0.67, 0.41, 0.00, -0.91, 0.41, 0.00, -0.91, 0.41, 0.00, -0.91, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.74, 0.00, -0.67, 0.74, 0.00, -0.67, 0.74, 0.00, -0.67, 0.41, 0.00, -0.91, 0.41, 0.00, -0.91, 0.41, 0.00, -0.91, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, -0.00, 1.00, 0.00, -0.00, 1.00, 0.00, -0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, -0.00, 1.00, 0.00, -0.00, 1.00, 0.00, -0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00}
                , TestHelper.vertices2DoubleArray(itemType.getVertexContainer().getNorms()), 0.01);
        Assert.assertArrayEquals(new double[]{0.5271, 0.9050, 0.5161, 0.0002, 0.6586, 0.0002, 0.8326, 0.0346, 0.9661, 0.2901, 0.5748, 0.5895, 0.2256, 0.9499, 0.1115, 0.9471, 0.1144, 0.8254, 0.6590, 0.9436, 0.6590, 0.0002, 0.6925, 0.2844, 0.5190, 0.5288, 0.5190, 0.5288, 0.5153, 0.7585, 0.6418, 0.9434, 0.6314, 0.9998, 0.5271, 0.9050, 0.5271, 0.9050, 0.6586, 0.0002, 0.6418, 0.9434, 0.5748, 0.5895, 0.5670, 0.4472, 0.4505, 0.2914, 0.9558, 0.5248, 0.8609, 0.5895, 0.5748, 0.5895, 0.5748, 0.5895, 0.9661, 0.2901, 0.9558, 0.5248, 0.5748, 0.5895, 0.4505, 0.2914, 0.8326, 0.0346, 0.2291, 0.8223, 0.2256, 0.9499, 0.1144, 0.8254, 0.6818, 0.9452, 0.6590, 0.9436, 0.6925, 0.2844, 0.5153, 0.7585, 0.5190, 0.5288, 0.5153, 0.7585, 0.2556, 0.0437, 0.2486, 0.0437, 0.2486, 0.0100, 0.2486, 0.0437, 0.2416, 0.0437, 0.2416, 0.0100, 0.2416, 0.0437, 0.2346, 0.0437, 0.2346, 0.0100, 0.0667, 0.0098, 0.0938, 0.1373, 0.0095, 0.0886, 0.2556, 0.0100, 0.2556, 0.0437, 0.2486, 0.0100, 0.2486, 0.0100, 0.2486, 0.0437, 0.2416, 0.0100, 0.2416, 0.0100, 0.2416, 0.0437, 0.2346, 0.0100, 0.0667, 0.0098, 0.0938, 0.0127, 0.1174, 0.0263, 0.0205, 0.0365, 0.0408, 0.0182, 0.0667, 0.0098, 0.0667, 0.0098, 0.0095, 0.0614, 0.0205, 0.0365, 0.0408, 0.1317, 0.0205, 0.1135, 0.0095, 0.0886, 0.0095, 0.0886, 0.0667, 0.1401, 0.0408, 0.1317, 0.1334, 0.1016, 0.1174, 0.1237, 0.0938, 0.1373, 0.1334, 0.0483, 0.1391, 0.0750, 0.1334, 0.1016, 0.0667, 0.0098, 0.1174, 0.0263, 0.1334, 0.0483, 0.0095, 0.0886, 0.0095, 0.0614, 0.0667, 0.0098, 0.0938, 0.1373, 0.0667, 0.1401, 0.0095, 0.0886, 0.1334, 0.0483, 0.1334, 0.1016, 0.0938, 0.1373, 0.0938, 0.1373, 0.0667, 0.0098, 0.1334, 0.0483}
                , TestHelper.textureCoordinates2DoubleArray(itemType.getVertexContainer().getTextureCoordinates()), 0.0001);
    }


    @Test
    public void testTerrainObject1() throws Exception {
        Map<String, TerrainObject.Type> nameToTypes = new HashMap<>();
        nameToTypes.put("cube", TerrainObject.Type.OPAQUE);
        TerrainObject terrainObject = createTestTerrainObject(getClass().getResourceAsStream("/collada/plane1.dae"), 1, nameToTypes);
        Assert.assertEquals(1, terrainObject.getId());
        Assert.assertEquals(1, terrainObject.getVertexContainers().size());
        VertexContainer vertexContainer = terrainObject.getVertexContainers().get(TerrainObject.Type.OPAQUE);
        Assert.assertEquals(6, vertexContainer.getVerticesCount());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, TestHelper.vertices2DoubleArray(vertexContainer.getVertices()), 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, TestHelper.vertices2DoubleArray(vertexContainer.getNorms()), 0.0001);
    }

    @Test
    public void testTerrainObject2() throws Exception {
        Map<String, TerrainObject.Type> nameToTypes = new HashMap<>();
        nameToTypes.put("Fir needles", TerrainObject.Type.TRANSPARENT_NO_SHADOW_CAST);
        nameToTypes.put("Trunk", TerrainObject.Type.OPAQUE);
        nameToTypes.put("Shadow", TerrainObject.Type.TRANSPARENT_SHADOW_CAST_ONLY);
        TerrainObject terrainObject = createTestTerrainObject(getClass().getResourceAsStream("/collada/TestTerrainObject1.dae"), 1, nameToTypes);
        Assert.assertEquals(1, terrainObject.getId());
        Assert.assertEquals(3, terrainObject.getVertexContainers().size());
        // No shadow vertex container
        VertexContainer noShadow = terrainObject.getVertexContainers().get(TerrainObject.Type.TRANSPARENT_NO_SHADOW_CAST);
        TestHelper.assertColor(new Color(0.1819462, 1, 0.2208172, 1), noShadow.getAmbient());
        TestHelper.assertColor(new Color(0.8, 0.3, 0.2, 1.0), noShadow.getDiffuse());
        Assert.assertNull(noShadow.getSpecular());
        TestHelper.assertColor(new Color(0, 0, 0), noShadow.getEmission());
        double[] expectedNoShadow = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadow.arr"));
        Assert.assertArrayEquals(expectedNoShadow, TestHelper.vertices2DoubleArray(noShadow.getVertices()), 0.01);
        double[] expectedNoShadowNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowNorm.arr"));
        Assert.assertArrayEquals(expectedNoShadowNorm, TestHelper.vertices2DoubleArray(noShadow.getNorms()), 0.01);
        double[] expectedNoShadowTextureCoordinates = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowTextureCoordinates.arr"));
        Assert.assertArrayEquals(expectedNoShadowTextureCoordinates, TestHelper.textureCoordinates2DoubleArray(noShadow.getTextureCoordinates()), 0.0001);
        // Shadow only
        VertexContainer onlyShadow = terrainObject.getVertexContainers().get(TerrainObject.Type.TRANSPARENT_SHADOW_CAST_ONLY);
        double[] expectedOnlyShadow = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentShadowOnly.arr"));
        Assert.assertArrayEquals(expectedOnlyShadow, TestHelper.vertices2DoubleArray(onlyShadow.getVertices()), 0.01);
        double[] expectedOnlyShadowTextureCoordinates = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentShadowOnlyTextureCoordinates.arr"));
        Assert.assertArrayEquals(expectedOnlyShadowTextureCoordinates, TestHelper.textureCoordinates2DoubleArray(onlyShadow.getTextureCoordinates()), 0.0001);
        // Opaque
        VertexContainer opaque = terrainObject.getVertexContainers().get(TerrainObject.Type.OPAQUE);
        TestHelper.assertColor(new Color(0.09097311, 0.5, 0.1104086, 1), opaque.getAmbient());
        TestHelper.assertColor(new Color(0.0, 0.4, 0.8, 1.0), opaque.getDiffuse());
        TestHelper.assertColor(new Color(0.2, 0.3, 0.4, 1.0), opaque.getSpecular());
        TestHelper.assertColor(new Color(123, 123, 123), opaque.getEmission());
        double[] expectedOpaque = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1Opaque.arr"));
        Assert.assertArrayEquals(expectedOpaque, TestHelper.vertices2DoubleArray(opaque.getVertices()), 0.01);
        double[] expectedOpaqueNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueNorm.arr"));
        Assert.assertArrayEquals(expectedOpaqueNorm, TestHelper.vertices2DoubleArray(opaque.getNorms()), 0.01);
        double[] expectedTextureCoordinate = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinate, TestHelper.textureCoordinates2DoubleArray(opaque.getTextureCoordinates()), 0.0001);
    }

    private static TerrainObject createTestTerrainObject(InputStream colladaInputStream, int id, Map<String, TerrainObject.Type> nameToTypes) throws Exception {
        List<TerrainObjectMaterialEntity> materials = new ArrayList<>();
        for (Map.Entry<String, TerrainObject.Type> entry : nameToTypes.entrySet()) {
            TerrainObjectMaterialEntity terrainObjectMaterialEntity = new TerrainObjectMaterialEntity();
            TestHelper.setPrivateField(terrainObjectMaterialEntity, "name", entry.getKey());
            TestHelper.setPrivateField(terrainObjectMaterialEntity, "type", entry.getValue());
            materials.add(terrainObjectMaterialEntity);
        }
        TerrainObjectEntity terrainObjectEntity = new TerrainObjectEntity();
        TestHelper.setPrivateField(terrainObjectEntity, "id", (long) id);
        TestHelper.setPrivateField(terrainObjectEntity, "materials", materials);
        TestHelper.setPrivateField(terrainObjectEntity, "colladaString", IOUtils.toString(colladaInputStream));
        return ColladaConverter.convertToTerrainObject(terrainObjectEntity);
    }


}