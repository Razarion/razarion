package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.Shape3DMaterialConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.test.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class Shape3DEditorControllerTest extends AbstractSystemTest {
    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTableNative("COLLADA_TEXTURES");
        cleanTableNative("COLLADA_BUMP_MAPS");
        cleanTableNative("COLLADA_BUMP_MAP_DEPTS");
        cleanTableNative("COLLADA_ALPHA_TO_COVERAGE");
        cleanTableNative("COLLADA_CHARACTER_REPRESENTING");
        cleanTableNative("COLLADA_ANIMATIONS");
        cleanTable(ColladaEntity.class);
    }

    @Test
    public void testNotAuthorized() {
        runUnauthorizedTest(Shape3DEditorController.class, Shape3DEditorController::create, RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
        runUnauthorizedTest(Shape3DEditorController.class, shape3DEditorController -> shape3DEditorController.update(new Shape3DConfig()), RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
        runUnauthorizedTest(Shape3DEditorController.class, shape3DEditorController -> shape3DEditorController.delete(1), RestConnection.TestUser.NONE, RestConnection.TestUser.USER);
    }

    @Test
    public void crud() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> Shape3DConfig.class));
        restConnection.loginAdmin();
        Shape3DEditorController underTest = restConnection.proxy(Shape3DEditorController.class);
        Assert.assertTrue(underTest.getObjectNameIds().isEmpty());
        // Create
        Shape3DConfig shape3DConfig = underTest.create();
        Assert.assertNull(shape3DConfig.getColladaString());
        Assert.assertNull(shape3DConfig.getAnimations());
        // Update via collada file
        shape3DConfig.setInternalName("Shape 1");
        shape3DConfig.setColladaString(TestHelper.resource2Text("Shape3dCrud_1.dae", getClass()));
        underTest.update(shape3DConfig);
        Shape3DConfig shape3DConfig2 = underTest.read(shape3DConfig.getId());
        assertThat(shape3DConfig2, allOf(
                hasProperty("colladaString", nullValue()),
                hasProperty("internalName", equalTo("Shape 1")),
                hasProperty("animations", nullValue()),
                hasProperty("shape3DMaterialConfigs",
                        containsInAnyOrder(
                                allOf(
                                        hasProperty("materialId", equalTo("Material_002-material")),
                                        hasProperty("materialName", equalTo("Material_002")),
                                        hasProperty("characterRepresenting", equalTo(false)),
                                        hasProperty("alphaToCoverage", equalTo(false)),
                                        hasProperty("phongMaterialConfig", allOf(
                                                hasProperty("textureId", nullValue()),
                                                hasProperty("scale", equalTo(1.0)),
                                                hasProperty("bumpMapId", nullValue()),
                                                hasProperty("bumpMapDepth", nullValue()),
                                                hasProperty("shininess", equalTo(50.0)),
                                                hasProperty("specularStrength", equalTo(0.2))
                                        ))
                                ),
                                allOf(
                                        hasProperty("materialId", equalTo("Material-material")),
                                        hasProperty("materialName", equalTo("Material")),
                                        hasProperty("characterRepresenting", equalTo(false)),
                                        hasProperty("alphaToCoverage", equalTo(false)),
                                        hasProperty("phongMaterialConfig", allOf(
                                                hasProperty("textureId", nullValue()),
                                                hasProperty("scale", equalTo(1.0)),
                                                hasProperty("bumpMapId", nullValue()),
                                                hasProperty("bumpMapDepth", nullValue()),
                                                hasProperty("shininess", nullValue()),
                                                hasProperty("specularStrength", nullValue())
                                        ))
                                )
                        )
                )
        ));
        // Set values directly
        Shape3DMaterialConfig shape3DMaterialConfig = findMaterial(shape3DConfig2, "Material_002-material");
        shape3DMaterialConfig.setAlphaToCoverage(true);
        shape3DMaterialConfig.setCharacterRepresenting(true);
        PhongMaterialConfig phongMaterialConfig = shape3DMaterialConfig.getPhongMaterialConfig();
        phongMaterialConfig.setTextureId(IMAGE_2_ID);
        phongMaterialConfig.setBumpMapId(IMAGE_3_ID);
        phongMaterialConfig.setBumpMapDepth(0.9);
        phongMaterialConfig = findMaterial(shape3DConfig2, "Material-material").getPhongMaterialConfig();
        phongMaterialConfig.setTextureId(IMAGE_1_ID);
        phongMaterialConfig.setBumpMapId(IMAGE_2_ID);
        phongMaterialConfig.setBumpMapDepth(0.5);
        underTest.update(shape3DConfig2);
        // Verify
        Shape3DConfig shape3DConfig3 = underTest.read(shape3DConfig.getId());
        assertThat(shape3DConfig3, allOf(
                hasProperty("colladaString", nullValue()),
                hasProperty("internalName", equalTo("Shape 1")),
                hasProperty("animations", nullValue()),
                hasProperty("shape3DMaterialConfigs",
                        containsInAnyOrder(
                                allOf(
                                        hasProperty("materialId", equalTo("Material_002-material")),
                                        hasProperty("materialName", equalTo("Material_002")),
                                        hasProperty("characterRepresenting", equalTo(true)),
                                        hasProperty("alphaToCoverage", equalTo(true)),
                                        hasProperty("phongMaterialConfig", allOf(
                                                hasProperty("textureId", equalTo(IMAGE_2_ID)),
                                                hasProperty("scale", equalTo(1.0)),
                                                hasProperty("bumpMapId", equalTo(IMAGE_3_ID)),
                                                hasProperty("bumpMapDepth", equalTo(0.9)),
                                                hasProperty("shininess", equalTo(50.0)),
                                                hasProperty("specularStrength", equalTo(0.2))
                                        ))
                                ),
                                allOf(
                                        hasProperty("materialId", equalTo("Material-material")),
                                        hasProperty("materialName", equalTo("Material")),
                                        hasProperty("characterRepresenting", equalTo(false)),
                                        hasProperty("alphaToCoverage", equalTo(false)),
                                        hasProperty("phongMaterialConfig", allOf(
                                                hasProperty("textureId", equalTo(IMAGE_1_ID)),
                                                hasProperty("scale", equalTo(1.0)),
                                                hasProperty("bumpMapId", equalTo(IMAGE_2_ID)),
                                                hasProperty("bumpMapDepth", equalTo(0.5)),
                                                hasProperty("shininess", nullValue()),
                                                hasProperty("specularStrength", nullValue())
                                        ))
                                )
                        )
                )
        ));
        // Delete
        underTest.delete(shape3DConfig.getId());
        Assert.assertTrue(underTest.getObjectNameIds().isEmpty());
    }

    public static Shape3DMaterialConfig findMaterial(Shape3DConfig shape3DConfig, String materialId) {
        return shape3DConfig.getShape3DMaterialConfigs()
                .stream()
                .filter(shape3DMaterialConfig -> shape3DMaterialConfig.getMaterialId().equals(materialId))
                .findFirst()
                .orElseThrow(IllegalAccessError::new);
    }
}