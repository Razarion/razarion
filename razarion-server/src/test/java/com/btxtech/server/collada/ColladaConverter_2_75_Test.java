package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.test.JsonAssert;
import com.btxtech.test.shared.SharedTestHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter_2_75_Test {

    @Test
    public void testSimplePlanePolylist1() throws Exception {
        testSimplePlane1("/collada/TestSimplePlanePolylist01.dae");
    }

    @Test
    public void testSimplePlaneTriangle1() throws Exception {
        testSimplePlane1("/collada/TestSimplePlaneTriangles01.dae");
    }

    private void testSimplePlane1(String location) throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text(location, getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(99);
        JsonAssert.assertViaJson("TestSimplePlane01.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(99);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("99-Plane_Id", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -1.00, 0.00, 1.00, 1.00, 0.00, -1.00, 1.00, 0.00, -1.00, -1.00, 0.00, 1.00, -1.00, 0.00, -1.00, 1.00, 0.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, norms, 0.01);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(99);
        JsonAssert.assertViaJson("TestSimplePlane01Config.json",
                null,
                null,
                getClass(),
                shape3DConfig);
    }

    @Test
    public void testSimpleSphere3x3() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestSphere3x3.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(101);
        JsonAssert.assertViaJson("TestSphere3x3.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(99);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{0.43, 0.75, -0.50, 0.43, 0.75, 0.50, 0.43, -0.75, 0.50, 0.00, 0.00, -1.00, -0.87, 0.00, -0.50, 0.43, 0.75, -0.50, -0.87, 0.00, -0.50, -0.87, 0.00, 0.50, 0.43, 0.75, 0.50, -0.87, 0.00,
                        0.50, 0.00, 0.00, 1.00, 0.43, 0.75, 0.50, 0.00, 0.00, -1.00, 0.43, 0.75, -0.50, 0.43, -0.75, -0.50, 0.43, 0.75, 0.50, 0.00, 0.00, 1.00, 0.43, -0.75, 0.50, 0.00, 0.00, -1.00, 0.43, -0.75, -0.50, -0.87, 0.00, -0.50,
                        0.43, -0.75, 0.50, -0.87, 0.00, 0.50, -0.87, 0.00, -0.50, 0.43, -0.75, 0.50, 0.00, 0.00, 1.00, -0.87, 0.00, 0.50, 0.43, -0.75, -0.50, 0.43, 0.75, -0.50, 0.43, -0.75, 0.50, 0.43, 0.75, -0.50, -0.87, 0.00, -0.50, 0.43,
                        0.75, 0.50, 0.43, -0.75, -0.50, 0.43, -0.75, 0.50, -0.87, 0.00, -0.50},
                vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.46, 0.79, -0.41, 0.46, 0.79, 0.41, 0.46, -0.79, 0.41, 0.00, 0.00, -1.00, -0.91, 0.00, -0.41, 0.46, 0.79, -0.41, -0.91, 0.00, -0.41, -0.91, 0.00, 0.41, 0.46, 0.79, 0.41, -0.91, 0.00, 0.41, 0.00, 0.00,
                        1.00, 0.46, 0.79, 0.41, 0.00, 0.00, -1.00, 0.46, 0.79, -0.41, 0.46, -0.79, -0.41, 0.46, 0.79, 0.41, 0.00, 0.00, 1.00, 0.46, -0.79, 0.41, 0.00, 0.00, -1.00, 0.46, -0.79, -0.41, -0.91, 0.00, -0.41, 0.46, -0.79, 0.41, -0.91,
                        0.00, 0.41, -0.91, 0.00, -0.41, 0.46, -0.79, 0.41, 0.00, 0.00, 1.00, -0.91, 0.00, 0.41, 0.46, -0.79, -0.41, 0.46, 0.79, -0.41, 0.46, -0.79, 0.41, 0.46, 0.79, -0.41, -0.91, 0.00, -0.41, 0.46, 0.79, 0.41, 0.46, -0.79, -0.41,
                        0.46, -0.79, 0.41, -0.91, 0.00, -0.41},
                norms, 0.01);
    }

    @Test
    public void testPlaneTranslation() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestPlaneTranslation.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(501);
        JsonAssert.assertViaJson("TestPlaneTranslation.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(501);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("501-Plane_Id", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -1.00, 1.00, 1.00, 1.00, 1.00, -1.00, 1.00, 1.00, -1.00, -1.00, 1.00, 1.00, -1.00, 1.00, -1.00, 1.00, 1.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, norms, 0.01);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(99);
        JsonAssert.assertViaJson("TestPlaneTranslationConfig.json",
                null,
                null,
                getClass(),
                shape3DConfig);
    }

    @Test
    public void testPlaneRotate() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestPlaneRotate.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(105);
        JsonAssert.assertViaJson("TestPlaneRotate.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(105);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("105-Plane_Id", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{0.71, -1.00, -0.71, 0.71, 1.00, -0.71, -0.71, 1.00, 0.71, -0.71, -1.00, 0.71, 0.71, -1.00, -0.71, -0.71, 1.00, 0.71}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71}, norms, 0.01);
    }

    @Test
    public void testPlaneScale() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestPlaneScale.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(1);
        JsonAssert.assertViaJson("TestPlaneScale.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(1);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{2.00, -0.50, 0.00, 2.00, 0.50, 0.00, -2.00, 0.50, 0.00, -2.00, -0.50, 0.00, 2.00, -0.50, 0.00, -2.00, 0.50, 0.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, norms, 0.01);
    }

    @Test
    public void testPlaneTranslRotScale() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestPlaneTranslRotScale.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(2);
        JsonAssert.assertViaJson("TestPlaneTranslRotScale.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(2);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-2.00, -4.00, 3.00, -2.00, -4.00, 13.00, -4.00, -4.00, 13.00, -4.00, -4.00, 3.00, -2.00, -4.00, 3.00, -4.00, -4.00, 13.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, norms, 0.01);
    }

    @Test
    public void testPlaneTranslRotScaleAnimation() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestPlaneTranslRotScaleAnimation.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(22);
        JsonAssert.assertViaJson("TestPlaneTranslRotScaleAnimation.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(22);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -0.00, 0.00, 1.00, 0.00, 20.00, -1.00, 0.00, 20.00, -1.00, -0.00, 0.00, 1.00, -0.00, 0.00, -1.00, 0.00, 20.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, norms, 0.01);
    }

    @Test
    public void testReadItemType1() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestItemType1.dae", getClass()), null, null);
        Shape3D shape3D = shape3DBuilder.createShape3D(22);
        JsonAssert.assertViaJson("TestItemType1.json",
                null,
                null,
                getClass(),
                shape3D);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(22);
        JsonAssert.assertViaJson("TestItemType1Config.json",
                null,
                null,
                getClass(),
                shape3DConfig);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(22);
        Assert.assertEquals(2, vertexContainerBuffers.size());

        VertexContainer vertexContainer = shape3D.getElement3Ds().get(1).getVertexContainers().get(0);
        VertexContainerBuffer buffer = getVertexContainerBuffer4Key(vertexContainer.getKey(), vertexContainerBuffers);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());

        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Vertex.arr")), vertices, 0.01);
        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Norm.arr")), norms, 0.01);
        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1TextureCoordinate.arr")), SharedTestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);

        vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Vertex.arr")), vertices, 0.01);
        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Norm.arr")), norms, 0.01);
        Assert.assertArrayEquals(SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1TextureCoordinate.arr")), SharedTestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);
    }

    @Test
    public void testTerrainObject1() throws Exception {
        TestMapper testMapper = new TestMapper(null);
        testMapper.addVertexContainerMaterial("Material-material", new VertexContainerMaterial()
                .materialId("Material-material")
                .alphaToCoverage(0.5)
                .characterRepresenting(true)
                .phongMaterialConfig(new PhongMaterialConfig()
                        .textureId(99)
                        .bumpMapId(20)
                        .bumpMapDepth(0.5)));

        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/plane1.dae", getClass()), testMapper, null);

        Shape3D shape3D = shape3DBuilder.createShape3D(111);
        JsonAssert.assertViaJson("TestPlane1.json",
                null,
                null,
                getClass(),
                shape3D);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(111);
        JsonAssert.assertViaJson("TestPlane1Config.json",
                null,
                null,
                getClass(),
                shape3DConfig);

        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(111);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = getVertexContainerBuffer4Key(vertexContainer.getKey(), vertexContainerBuffers);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, vertices, 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, norms, 0.0001);
    }

    @Test
    public void testTerrainObject2() throws Exception {
        TestMapper testMapper = new TestMapper(null);
        testMapper.addVertexContainerMaterial("Material-material",
                new VertexContainerMaterial().materialId("Material-material")
                        .phongMaterialConfig(new PhongMaterialConfig().textureId(101).normalMapId(303).normalMapDepth(0.8)));
        testMapper.addVertexContainerMaterial("Material_002-material",
                new VertexContainerMaterial().materialId("Material_002-material")
                        .phongMaterialConfig(new PhongMaterialConfig().textureId(201).shininess(0.765).specularStrength(10.3)));

        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestTerrainObject1.dae", getClass()), testMapper, null);

        Shape3D shape3D = shape3DBuilder.createShape3D(87);
        JsonAssert.assertViaJson("TestTerrainObject1.json",
                null,
                null,
                getClass(),
                shape3D);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(87);
        JsonAssert.assertViaJson("TestTerrainObject1Config.json",
                null,
                null,
                getClass(),
                shape3DConfig);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(87);
        Assert.assertEquals(2, vertexContainerBuffers.size());

        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        VertexContainerBuffer buffer = getVertexContainerBuffer4Key(vertexContainer.getKey(), vertexContainerBuffers);
        double[] vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedVertices = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadow.arr"));
        Assert.assertArrayEquals(expectedVertices, vertices, 0.01);
        double[] expectedNorms = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowNorm.arr"));
        Assert.assertArrayEquals(expectedNorms, norms, 0.01);
        double[] expectedTextureCoordinates = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinates, SharedTestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);

        vertexContainer = shape3D.getElement3Ds().get(1).getVertexContainers().get(0);
        buffer = getVertexContainerBuffer4Key(vertexContainer.getKey(), vertexContainerBuffers);
        vertices = SharedTestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        norms = SharedTestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedOpaque = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1Opaque.arr"));
        Assert.assertArrayEquals(expectedOpaque, vertices, 0.01);
        double[] expectedOpaqueNorm = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueNorm.arr"));
        Assert.assertArrayEquals(expectedOpaqueNorm, norms, 0.01);
        double[] expectedTextureCoordinate = SharedTestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinate, SharedTestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);
    }

    @Test
    public void testAnimation() throws Exception {
        Map<String, AnimationTrigger> animationTriggers = new HashMap<>();
        animationTriggers.put("CubeId_scale_X", AnimationTrigger.ITEM_PROGRESS);
        animationTriggers.put("CubeId_scale_Y", AnimationTrigger.ITEM_PROGRESS);
        animationTriggers.put("PlaneId_location_Y", AnimationTrigger.CONTINUES);
        animationTriggers.put("PlaneId_location_Z", AnimationTrigger.CONTINUES);
        animationTriggers.put("RotPlane_rotation_euler_X", AnimationTrigger.ITEM_PROGRESS);
        animationTriggers.put("RotPlane_rotation_euler_Y", AnimationTrigger.ITEM_PROGRESS);

        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(SharedTestHelper.resource2Text("/collada/TestAnimation01.dae", getClass()), new TestMapper(animationTriggers), null);

        Shape3D shape3D = shape3DBuilder.createShape3D(76);
        JsonAssert.assertViaJson("TestAnimation01.json",
                null,
                null,
                getClass(),
                shape3D);

        Shape3DConfig shape3DConfig = shape3DBuilder.createShape3DConfig(76);
        JsonAssert.assertViaJson("TestAnimation01Config.json",
                null,
                null,
                getClass(),
                shape3DConfig);
    }

    private static class TestMapper implements ColladaConverterMapper {
        private Map<String, VertexContainerMaterial> vertexContainerMaterials = new HashMap<>();
        private Map<String, AnimationTrigger> animationTriggers;

        public TestMapper(Map<String, AnimationTrigger> animationTriggers) {
            this.animationTriggers = animationTriggers;
        }

        public void addVertexContainerMaterial(String materialid, VertexContainerMaterial vertexContainerMaterial) {
            vertexContainerMaterials.put(materialid, vertexContainerMaterial);
        }

        @Override
        public VertexContainerMaterial toVertexContainerMaterial(String materialId) {
            return vertexContainerMaterials.get(materialId);
        }

        @Override
        public AnimationTrigger getAnimationTrigger(String animationId) {
            if (animationTriggers != null) {
                return animationTriggers.get(animationId);
            } else {
                return null;
            }
        }
    }

    private VertexContainerBuffer getVertexContainerBuffer4Key(String key, List<VertexContainerBuffer> vertexContainerBuffers) {
        for (VertexContainerBuffer vertexContainerBuffer : vertexContainerBuffers) {
            if (vertexContainerBuffer.getKey().equals(key)) {
                return vertexContainerBuffer;
            }
        }
        throw new IllegalArgumentException("No VertexContainerBuffer for key: " + key);
    }
}