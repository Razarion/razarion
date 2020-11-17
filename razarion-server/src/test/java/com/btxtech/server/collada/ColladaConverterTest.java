package com.btxtech.server.collada;

import com.btxtech.server.JsonAssert;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.test.TestHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testSimplePlanePolylist1() throws Exception {
        testSimplePlane1("/collada/TestSimplePlanePolylist01.dae");
    }

    @Test
    public void testSimplePlaneTriangle1() throws Exception {
        testSimplePlane1("/collada/TestSimplePlaneTriangles01.dae");
    }

    private void testSimplePlane1(String location) throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text(location, getClass()), null);
        Shape3D shape3D = shape3DBuilder.createShape3D(99);
        JsonAssert.assertViaJson("TestSimplePlane01.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(99);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("99-Plane_Id-PlaneMat-material", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
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
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestSphere3x3.dae", getClass()), null);
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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
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
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestPlaneTranslation.dae", getClass()), null);
        Shape3D shape3D = shape3DBuilder.createShape3D(501);
        JsonAssert.assertViaJson("TestPlaneTranslation.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(501);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("501-Plane_Id-null", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
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
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestPlaneRotate.dae", getClass()), null);
        Shape3D shape3D = shape3DBuilder.createShape3D(105);
        JsonAssert.assertViaJson("TestPlaneRotate.json",
                null,
                null,
                getClass(),
                shape3D);

        List<VertexContainerBuffer> vertexContainerBuffers = shape3DBuilder.createVertexContainerBuffer(105);
        Assert.assertEquals(1, vertexContainerBuffers.size());
        VertexContainerBuffer buffer = CollectionUtils.getFirst(vertexContainerBuffers);
        Assert.assertEquals("105-Plane_Id-null", buffer.getKey());
        VertexContainer vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{0.71, -1.00, -0.71, 0.71, 1.00, -0.71, -0.71, 1.00, 0.71, -0.71, -1.00, 0.71, 0.71, -1.00, -0.71, -0.71, 1.00, 0.71}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71}, norms, 0.01);
    }

    @Test
    public void testPlaneScale() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestPlaneScale.dae", getClass()), null);
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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{2.00, -0.50, 0.00, 2.00, 0.50, 0.00, -2.00, 0.50, 0.00, -2.00, -0.50, 0.00, 2.00, -0.50, 0.00, -2.00, 0.50, 0.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, norms, 0.01);
    }

    @Test
    public void testPlaneTranslRotScale() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestPlaneTranslRotScale.dae", getClass()), null);
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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-2.00, -4.00, 3.00, -2.00, -4.00, 13.00, -4.00, -4.00, 13.00, -4.00, -4.00, 3.00, -2.00, -4.00, 3.00, -4.00, -4.00, 13.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, norms, 0.01);
    }

    @Test
    public void testPlaneTranslRotScaleAnimation() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestPlaneTranslRotScaleAnimation.dae", getClass()), null);
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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -0.00, 0.00, 1.00, 0.00, 20.00, -1.00, 0.00, 20.00, -1.00, -0.00, 0.00, 1.00, -0.00, 0.00, -1.00, 0.00, 20.00}, vertices, 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, norms, 0.01);
    }

    @Test
    public void testReadItemType1() throws Exception {
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestItemType1.dae", getClass()), null);
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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());

        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Vertex.arr")), vertices, 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Norm.arr")), norms, 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1TextureCoordinate.arr")), TestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);

        vertexContainer = shape3D.getElement3Ds().get(0).getVertexContainers().get(0);
        vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Vertex.arr")), vertices, 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Norm.arr")), norms, 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1TextureCoordinate.arr")), TestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);
    }

    @Test
    public void testTerrainObject1() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 99);
        Map<String, Integer> bumpMapIds = new HashMap<>();
        bumpMapIds.put("Material-material", 20);
        Map<String, Double> bumpMapDepths = new HashMap<>();
        bumpMapDepths.put("Material-material", 0.5);
        Map<String, Double> alphaToCoverages = new HashMap<>();
        alphaToCoverages.put("Material-material", 0.5);
        Map<String, Boolean> characterRepresenting = new HashMap<>();
        characterRepresenting.put("Material-material", true);
        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/plane1.dae", getClass()),
                new TestMapper(textures, bumpMapIds, bumpMapDepths, null, alphaToCoverages, characterRepresenting));

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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, vertices, 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, norms, 0.0001);
    }

    @Test
    public void testTerrainObject2() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 101);
        textures.put("Material_002-material", 201);

        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestTerrainObject1.dae", getClass()),
                new TestMapper(textures, null, null, null, null, null));

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
        double[] vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        double[] norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedVertices = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadow.arr"));
        Assert.assertArrayEquals(expectedVertices, vertices, 0.01);
        double[] expectedNorms = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowNorm.arr"));
        Assert.assertArrayEquals(expectedNorms, norms, 0.01);
        double[] expectedTextureCoordinates = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinates, TestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);

        vertexContainer = shape3D.getElement3Ds().get(1).getVertexContainers().get(0);
        buffer = getVertexContainerBuffer4Key(vertexContainer.getKey(), vertexContainerBuffers);
        vertices = TestHelper.transform(buffer.getVertexData(), vertexContainer.getShapeTransform().setupMatrix());
        norms = TestHelper.transformNorm(buffer.getNormData(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedOpaque = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1Opaque.arr"));
        Assert.assertArrayEquals(expectedOpaque, vertices, 0.01);
        double[] expectedOpaqueNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueNorm.arr"));
        Assert.assertArrayEquals(expectedOpaqueNorm, norms, 0.01);
        double[] expectedTextureCoordinate = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinate, TestHelper.floatList2DoubleArray(buffer.getTextureCoordinate()), 0.0001);
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

        Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(TestHelper.resource2Text("/collada/TestAnimation01.dae", getClass()),
                new TestMapper(null, null, null, animationTriggers, null, null));

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
        private Map<String, Integer> textures;
        private Map<String, Integer> bumpMapIds;
        private Map<String, Double> bumpMapDepths;
        private Map<String, AnimationTrigger> animationTriggers;
        private Map<String, Double> alphaToCoverages;
        private Map<String, Boolean> characterRepresenting;

        public TestMapper(Map<String, Integer> textures, Map<String, Integer> bumpMapIds, Map<String, Double> bumpMapDepths, Map<String, AnimationTrigger> animationTriggers, Map<String, Double> alphaToCoverages, Map<String, Boolean> characterRepresenting) {
            this.textures = textures;
            this.bumpMapIds = bumpMapIds;
            this.bumpMapDepths = bumpMapDepths;
            this.animationTriggers = animationTriggers;
            this.alphaToCoverages = alphaToCoverages;
            this.characterRepresenting = characterRepresenting;
        }

        @Override
        public Integer getTextureId(String materialId) {
            if (textures != null) {
                return textures.get(materialId);
            } else {
                return null;
            }
        }

        @Override
        public Integer getBumpMapId(String materialId) {
            if (bumpMapIds != null) {
                return bumpMapIds.get(materialId);
            } else {
                return null;
            }
        }

        @Override
        public Double getBumpMapDepth(String materialId) {
            if (bumpMapDepths != null) {
                return bumpMapDepths.get(materialId);
            } else {
                return null;
            }
        }

        @Override
        public Double getAlphaToCoverage(String materialId) {
            if (alphaToCoverages != null) {
                return alphaToCoverages.get(materialId);
            } else {
                return null;
            }
        }

        @Override
        public boolean isCharacterRepresenting(String materialId) {
            if (characterRepresenting != null) {
                Boolean b = characterRepresenting.get(materialId);
                return b != null && b;
            } else {
                return false;
            }
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