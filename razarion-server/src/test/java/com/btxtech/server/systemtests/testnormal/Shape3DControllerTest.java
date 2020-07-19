package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.shared.rest.Shape3DController;
import com.btxtech.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.btxtech.server.systemtests.editors.Shape3DEditorControllerTest.findMaterial;

public class Shape3DControllerTest extends AbstractSystemTest {
    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTableNative("COLLADA_TEXTURES");
        cleanTableNative("COLLADA_BUMP_MAPS");
        cleanTableNative("COLLADA_BUMP_MAP_DEPTS");
        cleanTableNative("COLLADA_ALPHA_CUTOUTS");
        cleanTableNative("COLLADA_CHARACTER_REPRESENTING");
        cleanTableNative("COLLADA_ANIMATIONS");
        cleanTable(ColladaEntity.class);
    }

    @Test
    public void shape3DController() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> Shape3DConfig.class));
        restConnection.loginAdmin();
        Shape3DEditorController editorConnection = restConnection.proxy(Shape3DEditorController.class);
        Shape3DConfig shape3DConfig = editorConnection.create();
        shape3DConfig.setInternalName("Shape 1");
        shape3DConfig.setColladaString(TestHelper.resource2Text("Shape3DControllerTest.dae", getClass()));
        editorConnection.update(shape3DConfig);
        shape3DConfig = editorConnection.read(shape3DConfig.getId());
        PhongMaterialConfig phongMaterialConfig = findMaterial(shape3DConfig, "Material_002-material").getPhongMaterialConfig();
        phongMaterialConfig.setTextureId(IMAGE_2_ID);
        phongMaterialConfig.setBumpMapId(IMAGE_3_ID);
        phongMaterialConfig.setBumpMapDepth(0.9);
        phongMaterialConfig = findMaterial(shape3DConfig, "Material-material").getPhongMaterialConfig();
        phongMaterialConfig.setTextureId(IMAGE_1_ID);
        phongMaterialConfig.setBumpMapId(IMAGE_2_ID);
        phongMaterialConfig.setBumpMapDepth(0.5);
        editorConnection.update(shape3DConfig);

        restConnection.logout();

        String key1 = "\"" + shape3DConfig.getId() + "-Plane_029-Material-material" + "\"";
        String key2 = "\"" + shape3DConfig.getId() + "-Trunk33-Material_002-material" + "\"";

        Shape3DController shape3DController = restConnection.proxy(Shape3DController.class);
        List<VertexContainerBuffer> vertexContainerBuffers = shape3DController.getVertexBuffer();
        assertViaJson("Shape3DControllerTest_vertexContainerBuffers.json",
                s -> s.replace("\"78-Plane_029-Material-material\"", key1)
                        .replace("\"78-Trunk33-Material_002-material\"", key2),
                null,
                getClass(),
                vertexContainerBuffers);
        GameUiContextController gameUiContextController = restConnection.proxy(GameUiContextController.class);
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        assertViaJson("Shape3DControllerTest_Shape3Ds.json",
                s -> s.replace("\"$textureId1$\"", Integer.toString(IMAGE_1_ID))
                        .replace("\"$textureId2$\"", Integer.toString(IMAGE_2_ID))
                        .replace("\"$bumpMapId1$\"", Integer.toString(IMAGE_2_ID))
                        .replace("\"$bumpMapId2$\"", Integer.toString(IMAGE_3_ID))
                        .replace("\"78-Plane_029-Material-material\"", key1)
                        .replace("\"78-Trunk33-Material_002-material\"", key2),
                new IdSuppressor[]{new IdSuppressor("/0", "id")},
                getClass(),
                coldGameUiContext.getShape3Ds());
    }
}
