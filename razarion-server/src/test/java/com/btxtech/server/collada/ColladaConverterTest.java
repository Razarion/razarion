package com.btxtech.server.collada;

import com.btxtech.TestHelper;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.server.itemtype.ItemTypeEntity;
import com.btxtech.server.rest.ImageLibraryEntity;
import com.btxtech.server.terrain.object.TerrainObjectEntity;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.datatypes.Color;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
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
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Vertex.arr")), TestHelper.vertices2DoubleArray(itemType.getVertexContainer().getVertices()), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Norm.arr")), TestHelper.vertices2DoubleArray(itemType.getVertexContainer().getNorms()), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1TextureCoordinate.arr")), TestHelper.textureCoordinates2DoubleArray(itemType.getVertexContainer().getTextureCoordinates()), 0.0001);
    }

    @Test
    public void testTerrainObject1() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 99);
        TerrainObject terrainObject = createTestTerrainObject(getClass().getResourceAsStream("/collada/plane1.dae"), 1, textures);
        Assert.assertEquals(1, terrainObject.getId());
        Assert.assertEquals(1, terrainObject.getVertexContainers().size());
        VertexContainer vertexContainer = CollectionUtils.getFirst(terrainObject.getVertexContainers());
        Assert.assertEquals(6, vertexContainer.getVerticesCount());
        Assert.assertEquals("Material-material", vertexContainer.getMaterialId());
        Assert.assertEquals("Material", vertexContainer.getMaterialName());
        Assert.assertEquals(99, (int)vertexContainer.getTextureId());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, TestHelper.vertices2DoubleArray(vertexContainer.getVertices()), 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, TestHelper.vertices2DoubleArray(vertexContainer.getNorms()), 0.0001);
    }

    @Test
    public void testTerrainObject2() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 101);
        textures.put("Material_002-material", 201);
        TerrainObject terrainObject = createTestTerrainObject(getClass().getResourceAsStream("/collada/TestTerrainObject1.dae"), 1, textures);
        Assert.assertEquals(1, terrainObject.getId());
        Assert.assertEquals(3, terrainObject.getVertexContainers().size());
        // No shadow vertex container
        VertexContainer firNeedles = getVertexContainer("Material-material", terrainObject);
        Assert.assertEquals("Material-material", firNeedles.getMaterialId());
        Assert.assertEquals("Material", firNeedles.getMaterialName());
        Assert.assertEquals(101, (int)firNeedles.getTextureId());
        TestHelper.assertColor(new Color(0.1819462, 1, 0.2208172, 1), firNeedles.getAmbient());
        TestHelper.assertColor(new Color(0.8, 0.3, 0.2, 1.0), firNeedles.getDiffuse());
        Assert.assertNull(firNeedles.getSpecular());
        TestHelper.assertColor(new Color(0, 0, 0), firNeedles.getEmission());
        double[] expectedNoShadow = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadow.arr"));
        Assert.assertArrayEquals(expectedNoShadow, TestHelper.vertices2DoubleArray(firNeedles.getVertices()), 0.01);
        double[] expectedNoShadowNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowNorm.arr"));
        Assert.assertArrayEquals(expectedNoShadowNorm, TestHelper.vertices2DoubleArray(firNeedles.getNorms()), 0.01);
        double[] expectedNoShadowTextureCoordinates = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedNoShadowTextureCoordinates, TestHelper.textureCoordinates2DoubleArray(firNeedles.getTextureCoordinates()), 0.0001);
        // Opaque
        VertexContainer trunk = getVertexContainer("Material_002-material", terrainObject);
        Assert.assertEquals("Material_002-material", trunk.getMaterialId());
        Assert.assertEquals("Material_002", trunk.getMaterialName());
        Assert.assertEquals(201, (int)trunk.getTextureId());
        TestHelper.assertColor(new Color(0.09097311, 0.5, 0.1104086, 1), trunk.getAmbient());
        TestHelper.assertColor(new Color(0.0, 0.4, 0.8, 1.0), trunk.getDiffuse());
        TestHelper.assertColor(new Color(0.2, 0.3, 0.4, 1.0), trunk.getSpecular());
        TestHelper.assertColor(new Color(123, 123, 123), trunk.getEmission());
        double[] expectedOpaque = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1Opaque.arr"));
        Assert.assertArrayEquals(expectedOpaque, TestHelper.vertices2DoubleArray(trunk.getVertices()), 0.01);
        double[] expectedOpaqueNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueNorm.arr"));
        Assert.assertArrayEquals(expectedOpaqueNorm, TestHelper.vertices2DoubleArray(trunk.getNorms()), 0.01);
        double[] expectedTextureCoordinate = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinate, TestHelper.textureCoordinates2DoubleArray(trunk.getTextureCoordinates()), 0.0001);
    }

    private VertexContainer getVertexContainer(String materialId, TerrainObject terrainObject) {
        for (VertexContainer vertexContainer : terrainObject.getVertexContainers()) {
            if (vertexContainer.getMaterialId() != null && vertexContainer.getMaterialId().equalsIgnoreCase(materialId)) {
                return vertexContainer;
            }
        }
        throw new AssertionError("No material id found in vertex container: " + materialId);
    }

    private static TerrainObject createTestTerrainObject(InputStream colladaInputStream, int id, Map<String, Integer> textures) throws Exception {
        Map<String, ImageLibraryEntity> texturesEntities = new HashMap<>();
        for (Map.Entry<String, Integer> entry : textures.entrySet()) {
            ImageLibraryEntity imageLibraryEntity = new ImageLibraryEntity();
            TestHelper.setPrivateField(imageLibraryEntity, "id", entry.getValue().longValue());
            texturesEntities.put(entry.getKey(), imageLibraryEntity);
        }
        TerrainObjectEntity terrainObjectEntity = new TerrainObjectEntity();
        TestHelper.setPrivateField(terrainObjectEntity, "id", (long) id);
        TestHelper.setPrivateField(terrainObjectEntity, "textures", texturesEntities);
        TestHelper.setPrivateField(terrainObjectEntity, "colladaString", IOUtils.toString(colladaInputStream));
        return ColladaConverter.convertToTerrainObject(terrainObjectEntity);
    }


}