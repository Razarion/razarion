package com.btxtech.servercommon.collada;

import com.btxtech.TestHelper;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.TimeValueSample;
import com.btxtech.shared.datatypes.shape.TransformationModification;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.Shape3DUtils;
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
    public void testSimplePlane1() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestSimplePlane01.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -1.00, 0.00, 1.00, 1.00, 0.00, -1.00, 1.00, 0.00, -1.00, -1.00, 0.00, 1.00, -1.00, 0.00, -1.00, 1.00, 0.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testSimpleSphere3x3() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestSphere3x3.dae", getClass()), null);
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Element", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);

        Assert.assertEquals(12 * 3, vertexContainer.verticesCount());
        Assert.assertEquals(12 * 3, vertexContainer.getNorms().size());

        // Assert vertices
        Map<Vertex, Integer> vertexGroup = GeometricUtil.groupVertices(vertexContainer.getVertices(), 0.0001);
        // Top
//        Assert.assertEquals(3, vertexGroup.get(new Vertex(0, 0, 1)).intValue());
//        // Top middle
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(0.43301, -0.75, 0.5)).intValue());
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(-0.86603, 0, 0.5)).intValue());
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(0.43301, 0.75, 0.5)).intValue());
//        // Bottom middle
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(-0.86603, 0, -0.5)).intValue());
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(0.43301, -0.75, -0.5)).intValue());
//        Assert.assertEquals(5, vertexGroup.get(new Vertex(0.43301, 0.75, -0.5)).intValue());
//        // Bottom
//        Assert.assertEquals(3, vertexGroup.get(new Vertex(0, 0, -1)).intValue());

        // Assert norms
        Map<Vertex, Integer> normGroup = GeometricUtil.groupVertices(vertexContainer.getNorms(), 0.0001);
        // Top
        //  Assert.assertEquals(3, normGroup.get(new Vertex(0, 0, 1)).intValue());


//        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
//        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
//        Assert.assertArrayEquals(new double[]{1.00, -1.00, 0.00, 1.00, 1.00, 0.00, -1.00, 1.00, 0.00, -1.00, -1.00, 0.00, 1.00, -1.00, 0.00, -1.00, 1.00, 0.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
//        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testPlaneTranslation() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestPlaneTranslation.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -1.00, 1.00, 1.00, 1.00, 1.00, -1.00, 1.00, 1.00, -1.00, -1.00, 1.00, 1.00, -1.00, 1.00, -1.00, 1.00, 1.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testPlaneRotate() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestPlaneRotate.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{0.71, -1.00, -0.71, 0.71, 1.00, -0.71, -0.71, 1.00, 0.71, -0.71, -1.00, 0.71, 0.71, -1.00, -0.71, -0.71, 1.00, 0.71}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71, 0.71, 0.00, 0.71}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testPlaneScale() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestPlaneScale.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{2.00, -0.50, 0.00, 2.00, 0.50, 0.00, -2.00, 0.50, 0.00, -2.00, -0.50, 0.00, 2.00, -0.50, 0.00, -2.00, 0.50, 0.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 1.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testPlaneTranslRotScale() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestPlaneTranslRotScale.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-2.00, -4.00, 3.00, -2.00, -4.00, 13.00, -4.00, -4.00, 13.00, -4.00, -4.00, 3.00, -2.00, -4.00, 3.00, -4.00, -4.00, 13.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }

    @Test
    public void testPlaneTranslRotScaleAnimation() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestPlaneTranslRotScaleAnimation.dae", getClass()), null);
        Assert.assertEquals(3, shape3D.getModelMatrixAnimations().size());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());
        Element3D planeElement = Shape3DUtils.getElement3D("Plane_Id", shape3D);
        Assert.assertEquals(1, planeElement.getVertexContainers().size());
        VertexContainer vertexContainer = planeElement.getVertexContainers().get(0);
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{1.00, -0.00, 0.00, 1.00, 0.00, 20.00, -1.00, 0.00, 20.00, -1.00, -0.00, 0.00, 1.00, -0.00, 0.00, -1.00, 0.00, 20.00}, TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(new double[]{0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00, 0.00, -1.00, -0.00}, TestHelper.vertices2DoubleArray(norms), 0.01);
    }


    @Test
    public void testReadItemType1() throws Exception {
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestItemType1.dae", getClass()), null);
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(2, shape3D.getElement3Ds().size());

        Element3D chassisElement = Shape3DUtils.getElement3D("Chassis1", shape3D);
        Assert.assertEquals("Chassis1", chassisElement.getId());
        Assert.assertEquals(1, chassisElement.getVertexContainers().size());
        VertexContainer vertexContainer = chassisElement.getVertexContainers().get(0);
        Assert.assertEquals("Chassis_Material", vertexContainer.getMaterialName());
        Assert.assertEquals("Chassis_Material-material", vertexContainer.getMaterialId());
        Assert.assertNull(vertexContainer.getTextureId());
        Assert.assertEquals(new Color(0, 0, 0), vertexContainer.getAmbient());
        Assert.assertEquals(new Color(0, 0, 0), vertexContainer.getEmission());
        Assert.assertEquals(new Color(0.64, 0.64, 0.64), vertexContainer.getDiffuse());
        Assert.assertEquals(new Color(0.5, 0.5, 0.5), vertexContainer.getSpecular());
        Assert.assertEquals(42, vertexContainer.verticesCount());
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Vertex.arr")), TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1Norm.arr")), TestHelper.vertices2DoubleArray(norms), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Chassis1TextureCoordinate.arr")), TestHelper.textureCoordinates2DoubleArray(vertexContainer.getTextureCoordinates()), 0.0001);

        Element3D wheelElement = Shape3DUtils.getElement3D("Wheel1", shape3D);
        Assert.assertEquals("Wheel1", wheelElement.getId());
        Assert.assertEquals(1, wheelElement.getVertexContainers().size());
        vertexContainer = wheelElement.getVertexContainers().get(0);
        Assert.assertNull("Chassis_Material", vertexContainer.getMaterialName());
        Assert.assertNull("Chassis_Material-material", vertexContainer.getMaterialId());
        Assert.assertNull(vertexContainer.getTextureId());
        Assert.assertNull(vertexContainer.getAmbient());
        Assert.assertNull(vertexContainer.getEmission());
        Assert.assertNull(vertexContainer.getDiffuse());
        Assert.assertNull(vertexContainer.getSpecular());
        Assert.assertEquals(57, vertexContainer.verticesCount());
        vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Vertex.arr")), TestHelper.vertices2DoubleArray(vertices), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1Norm.arr")), TestHelper.vertices2DoubleArray(norms), 0.01);
        Assert.assertArrayEquals(TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestItem1Wheel1TextureCoordinate.arr")), TestHelper.textureCoordinates2DoubleArray(vertexContainer.getTextureCoordinates()), 0.0001);
    }

    @Test
    public void testTerrainObject1() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 99);
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/plane1.dae", getClass()), new TestMapper(textures, null));

        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(1, shape3D.getElement3Ds().size());

        Element3D cube1Element = Shape3DUtils.getElement3D("Cube1", shape3D);
        Assert.assertEquals("Cube1", cube1Element.getId());
        Assert.assertEquals(1, cube1Element.getVertexContainers().size());
        VertexContainer vertexContainer = cube1Element.getVertexContainers().get(0);
        Assert.assertEquals("Material", vertexContainer.getMaterialName());
        Assert.assertEquals("Material-material", vertexContainer.getMaterialId());
        Assert.assertEquals(99, (int) vertexContainer.getTextureId());
        Assert.assertEquals(new Color(0, 0, 0), vertexContainer.getAmbient());
        Assert.assertEquals(new Color(0, 0, 0), vertexContainer.getEmission());
        Assert.assertEquals(new Color(0.64, 0.64, 0.64), vertexContainer.getDiffuse());
        Assert.assertEquals(new Color(0.5, 0.5, 0.5), vertexContainer.getSpecular());
        Assert.assertEquals(6, vertexContainer.verticesCount());
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, TestHelper.vertices2DoubleArray(vertices), 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, TestHelper.vertices2DoubleArray(norms), 0.0001);
    }

    @Test
    public void testTerrainObject2() throws Exception {
        Map<String, Integer> textures = new HashMap<>();
        textures.put("Material-material", 101);
        textures.put("Material_002-material", 201);
        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestTerrainObject1.dae", getClass()), new TestMapper(textures, null));
        Assert.assertNull(shape3D.getModelMatrixAnimations());
        Assert.assertEquals(2, shape3D.getElement3Ds().size());

        Element3D plane029 = Shape3DUtils.getElement3D("Plane_029", shape3D);
        Assert.assertEquals("Plane_029", plane029.getId());
        Assert.assertEquals(1, plane029.getVertexContainers().size());
        VertexContainer vertexContainer = plane029.getVertexContainers().get(0);
        Assert.assertEquals("Material", vertexContainer.getMaterialName());
        Assert.assertEquals("Material-material", vertexContainer.getMaterialId());
        Assert.assertEquals(101, (int) vertexContainer.getTextureId());
        TestHelper.assertColor(new Color(0.1819462, 1, 0.2208172, 1), vertexContainer.getAmbient());
        TestHelper.assertColor(new Color(0.8, 0.3, 0.2, 1.0), vertexContainer.getDiffuse());
        Assert.assertNull(vertexContainer.getSpecular());
        TestHelper.assertColor(new Color(0, 0, 0), vertexContainer.getEmission());
        List<Vertex> vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        List<Vertex> norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedNoShadow = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadow.arr"));
        Assert.assertArrayEquals(expectedNoShadow, TestHelper.vertices2DoubleArray(vertices), 0.01);
        double[] expectedNoShadowNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowNorm.arr"));
        Assert.assertArrayEquals(expectedNoShadowNorm, TestHelper.vertices2DoubleArray(norms), 0.01);
        double[] expectedNoShadowTextureCoordinates = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1TransparentNoShadowTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedNoShadowTextureCoordinates, TestHelper.textureCoordinates2DoubleArray(vertexContainer.getTextureCoordinates()), 0.0001);

        Element3D trunk33 = Shape3DUtils.getElement3D("Trunk33", shape3D);
        Assert.assertEquals("Trunk33", trunk33.getId());
        Assert.assertEquals(1, trunk33.getVertexContainers().size());
        vertexContainer = trunk33.getVertexContainers().get(0);
        Assert.assertEquals("Material_002-material", vertexContainer.getMaterialId());
        Assert.assertEquals("Material_002", vertexContainer.getMaterialName());
        Assert.assertEquals(201, (int) vertexContainer.getTextureId());
        TestHelper.assertColor(new Color(0.09097311, 0.5, 0.1104086, 1), vertexContainer.getAmbient());
        TestHelper.assertColor(new Color(0.0, 0.4, 0.8, 1.0), vertexContainer.getDiffuse());
        TestHelper.assertColor(new Color(0.2, 0.3, 0.4, 1.0), vertexContainer.getSpecular());
        TestHelper.assertColor(new Color(123, 123, 123), vertexContainer.getEmission());
        vertices = GeometricUtil.transform(vertexContainer.getVertices(), vertexContainer.getShapeTransform().setupMatrix());
        norms = GeometricUtil.transformNorm(vertexContainer.getNorms(), vertexContainer.getShapeTransform().setupMatrix().normTransformation());
        double[] expectedOpaque = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1Opaque.arr"));
        Assert.assertArrayEquals(expectedOpaque, TestHelper.vertices2DoubleArray(vertices), 0.01);
        double[] expectedOpaqueNorm = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueNorm.arr"));
        Assert.assertArrayEquals(expectedOpaqueNorm, TestHelper.vertices2DoubleArray(norms), 0.01);
        double[] expectedTextureCoordinate = TestHelper.readArrayFromFile(getClass().getResourceAsStream("TestTerrainObject1OpaqueTextureCoordinate.arr"));
        Assert.assertArrayEquals(expectedTextureCoordinate, TestHelper.textureCoordinates2DoubleArray(vertexContainer.getTextureCoordinates()), 0.0001);
    }

    @Test
    public void testAnimation() throws Exception {
        Map<String, AnimationTrigger> animationTriggers = new HashMap<>();
        animationTriggers.put("CubeId_scale_X", AnimationTrigger.ITEM_PROGRESS);
        animationTriggers.put("CubeId_scale_Y", AnimationTrigger.ITEM_PROGRESS);
        animationTriggers.put("PlaneId_location_Y", AnimationTrigger.CONTINUES);
        animationTriggers.put("PlaneId_location_Z", AnimationTrigger.CONTINUES);

        Shape3D shape3D = ColladaConverter.convertShape3D(TestHelper.resource2Text("/collada/TestAnnimation01.dae", getClass()), new TestMapper(null, animationTriggers));
        Assert.assertEquals(6, shape3D.getModelMatrixAnimations().size());
        Assert.assertEquals(2, shape3D.getElement3Ds().size());

        Element3D cube = Shape3DUtils.getElement3D("CubeId", shape3D);

        ModelMatrixAnimation modelMatrixAnimation = getModelMatrixAnimation4Axis("CubeId_scale_X", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.X, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.SCALE, modelMatrixAnimation.getModification());
        Assert.assertTrue(cube == modelMatrixAnimation.getElement3D());
        Assert.assertEquals(AnimationTrigger.ITEM_PROGRESS, modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 1), createTvs(4166L, 10), createTvs(8333, 20));

        modelMatrixAnimation = getModelMatrixAnimation4Axis("CubeId_scale_Y", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.Y, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.SCALE, modelMatrixAnimation.getModification());
        Assert.assertTrue(cube == modelMatrixAnimation.getElement3D());
        Assert.assertEquals(AnimationTrigger.ITEM_PROGRESS, modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 1), createTvs(4166L, 10), createTvs(8333, 30));

        modelMatrixAnimation = getModelMatrixAnimation4Axis("CubeId_scale_Z", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.Z, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.SCALE, modelMatrixAnimation.getModification());
        Assert.assertTrue(cube == modelMatrixAnimation.getElement3D());
        Assert.assertNull(modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 1), createTvs(4166L, 20), createTvs(8333, 10));

        Element3D plane = Shape3DUtils.getElement3D("PlaneId", shape3D);

        modelMatrixAnimation = getModelMatrixAnimation4Axis("PlaneId_location_X", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.X, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.LOCATION, modelMatrixAnimation.getModification());
        Assert.assertTrue(plane == modelMatrixAnimation.getElement3D());
        Assert.assertNull(modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 0), createTvs(4166, 0));

        modelMatrixAnimation = getModelMatrixAnimation4Axis("PlaneId_location_Y", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.Y, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.LOCATION, modelMatrixAnimation.getModification());
        Assert.assertTrue(plane == modelMatrixAnimation.getElement3D());
        Assert.assertEquals(AnimationTrigger.CONTINUES, modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 5), createTvs(4166, 15));

        modelMatrixAnimation = getModelMatrixAnimation4Axis("PlaneId_location_Z", shape3D);
        Assert.assertEquals(ModelMatrixAnimation.Axis.Z, modelMatrixAnimation.getAxis());
        Assert.assertEquals(TransformationModification.LOCATION, modelMatrixAnimation.getModification());
        Assert.assertTrue(plane == modelMatrixAnimation.getElement3D());
        Assert.assertEquals(AnimationTrigger.CONTINUES, modelMatrixAnimation.getAnimationTrigger());
        assertTimeValueSample(modelMatrixAnimation, createTvs(41L, 10), createTvs(4166, -50));
    }

    private void assertTimeValueSample(ModelMatrixAnimation modelMatrixAnimation, TimeValueSample... expectedTimeValueSamples) {
        Assert.assertEquals(expectedTimeValueSamples.length, modelMatrixAnimation.getTimeValueSamples().size());
        List<TimeValueSample> timeValueSamples = modelMatrixAnimation.getTimeValueSamples();
        for (int i = 0; i < timeValueSamples.size(); i++) {
            TimeValueSample expected = expectedTimeValueSamples[i];
            TimeValueSample actual = timeValueSamples.get(i);
            Assert.assertEquals(expected, actual);
        }
    }

    public TimeValueSample createTvs(long timeStamp, double value) {
        return new TimeValueSample().setTimeStamp(timeStamp).setValue(value);
    }

    private class TestMapper implements ColladaConverterMapper {
        private Map<String, Integer> textures;
        private Map<String, AnimationTrigger> animationTriggers;

        public TestMapper(Map<String, Integer> textures, Map<String, AnimationTrigger> animationTriggers) {
            this.textures = textures;
            this.animationTriggers = animationTriggers;
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
        public AnimationTrigger getAnimationTrigger(String animationId) {
            if (animationTriggers != null) {
                return animationTriggers.get(animationId);
            } else {
                return null;
            }
        }
    }

    private ModelMatrixAnimation getModelMatrixAnimation4Axis(String animationId, Shape3D shape3D) {
        for (ModelMatrixAnimation modelMatrixAnimation : shape3D.getModelMatrixAnimations()) {
            if (modelMatrixAnimation.getId().equals(animationId)) {
                return modelMatrixAnimation;
            }
        }
        throw new IllegalArgumentException("No ModelMatrixAnimation for id: " + animationId);
    }
}