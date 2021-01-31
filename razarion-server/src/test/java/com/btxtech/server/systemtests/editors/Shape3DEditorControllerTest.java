package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.VertexContainerMaterialConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.test.JsonAssert;
import com.btxtech.test.shared.SharedTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Shape3DConfig shape3DConfig1 = underTest.create();
        JsonAssert.assertViaJson("Shape3dCrudConfig_1_1.json",
                s -> s.replace("\"$ID$\"", Integer.toString(shape3DConfig1.getId())),
                null,
                getClass(),
                shape3DConfig1,
                false);

        // Update via collada file
        shape3DConfig1.setInternalName("Shape 1");
        shape3DConfig1.setColladaString(SharedTestHelper.resource2Text("Shape3dCrud_1.dae", getClass()));
        underTest.update(shape3DConfig1);
        Shape3DConfig shape3DConfig2 = underTest.read(shape3DConfig1.getId());
        JsonAssert.assertViaJson("Shape3dCrudConfig_1_2.json",
                s -> s.replace("\"$ID$\"", Integer.toString(shape3DConfig1.getId())),
                null,
                getClass(),
                shape3DConfig2);
        // Set values directly
        VertexContainerMaterialConfig materialConfig1 = shape3DConfig2.getShape3DElementConfigs().get(0).getVertexContainerMaterialConfigs().get(0);
        materialConfig1.setAlphaToCoverage(0.3);
        materialConfig1.setCharacterRepresenting(false);
        PhongMaterialConfig phongMaterialConfig1 = materialConfig1.getPhongMaterialConfig();
        phongMaterialConfig1.setTextureId(IMAGE_1_ID);
        phongMaterialConfig1.setBumpMapId(IMAGE_2_ID);
        phongMaterialConfig1.setBumpMapDepth(0.5);
        VertexContainerMaterialConfig materialConfig2 = shape3DConfig2.getShape3DElementConfigs().get(1).getVertexContainerMaterialConfigs().get(0);
        materialConfig2.setAlphaToCoverage(0.1);
        materialConfig2.setCharacterRepresenting(true);
        PhongMaterialConfig phongMaterialConfig2 = materialConfig2.getPhongMaterialConfig();
        phongMaterialConfig2.setTextureId(IMAGE_2_ID);
        phongMaterialConfig2.setBumpMapId(IMAGE_3_ID);
        phongMaterialConfig2.setBumpMapDepth(0.9);
        underTest.update(shape3DConfig2);
        // Verify
        Shape3DConfig shape3DConfig3 = underTest.read(shape3DConfig1.getId());
        JsonAssert.assertViaJson("Shape3dCrudConfig_1_3.json",
                s -> s.replace("\"$ID$\"", Integer.toString(shape3DConfig1.getId()))
                        .replace("\"$_IMAGE_1_ID_$\"", Integer.toString(IMAGE_1_ID))
                        .replace("\"$_IMAGE_2_ID_$\"", Integer.toString(IMAGE_2_ID))
                        .replace("\"$_IMAGE_3_ID_$\"", Integer.toString(IMAGE_3_ID)),
                null,
                getClass(),
                shape3DConfig2);

        // Delete
        underTest.delete(shape3DConfig1.getId());
        Assert.assertTrue(underTest.getObjectNameIds().isEmpty());
    }

    @Deprecated
    public static VertexContainerMaterial findMaterial(Shape3DConfig shape3DConfig, String materialId) {
        throw new UnsupportedOperationException();
//        return shape3DConfig.getShape3DElementConfigs()
//                .stream()
//                .filter(shape3DMaterialConfig -> shape3DMaterialConfig.getMaterialId().equals(materialId))
//                .findFirst()
//                .orElseThrow(IllegalAccessError::new);
    }
}