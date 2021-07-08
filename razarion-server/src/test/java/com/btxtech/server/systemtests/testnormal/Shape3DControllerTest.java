package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ColladaMaterialEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.server.util.TestImagePersistenceHelper;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.VertexContainerMaterialConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.rest.Shape3DController;
import com.btxtech.test.JsonAssert;
import com.btxtech.test.shared.SharedTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Shape3DControllerTest extends AbstractSystemTest {
    private int shape3DId;

    @Before
    public void fillTables() {
        setupImages();
        runInTransaction(entityManager -> {
            ColladaEntity colladaEntity = new ColladaEntity();
            colladaEntity.setInternalName("Shape 1");
            colladaEntity.setColladaString(SharedTestHelper.resource2Text("Shape3DControllerTest.dae", getClass()));
            List<ColladaMaterialEntity> colladaMaterials = new ArrayList<>();
            colladaMaterials.add(new ColladaMaterialEntity().from(
                    new VertexContainerMaterialConfig()
                            .materialId("Material-material")
                            .phongMaterialConfig(new PhongMaterialConfig()
                                    .textureId(IMAGE_1_ID)
                                    .bumpMapId(IMAGE_2_ID)
                                    .bumpMapDepth(0.5)), new TestImagePersistenceHelper(getEntityManager())));
            colladaMaterials.add(new ColladaMaterialEntity().from(
                    new VertexContainerMaterialConfig()
                            .materialId("Material_002-material")
                            .alphaToCoverage(0.2)
                            .characterRepresenting(true)
                            .phongMaterialConfig(new PhongMaterialConfig()
                                    .textureId(IMAGE_2_ID)
                                    .bumpMapId(IMAGE_3_ID)
                                    .bumpMapDepth(0.9)), new TestImagePersistenceHelper(getEntityManager())));
            colladaEntity.setColladaMaterials(colladaMaterials);
            entityManager.persist(colladaEntity);
            shape3DId = colladaEntity.getId();
        });
    }

    @After
    public void cleanTables() {
        cleanTableNative("COLLADA_ANIMATIONS");
        cleanTable(ColladaMaterialEntity.class);
        cleanTable(ColladaEntity.class);
    }

    @Test
    public void shape3DController() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> Shape3DConfig.class));

        String key1 = "\"" + shape3DId + "-Plane_029" + "\"";
        String key2 = "\"" + shape3DId + "-Trunk33" + "\"";

        Shape3DController shape3DController = restConnection.proxy(Shape3DController.class);
        List<VertexContainerBuffer> vertexContainerBuffers = shape3DController.getVertexBuffer();
        JsonAssert.assertViaJson("Shape3DControllerTest_vertexContainerBuffers.json",
                s -> s.replace("\"78-Plane_029\"", key1)
                        .replace("\"78-Trunk33\"", key2),
                null,
                getClass(),
                vertexContainerBuffers);
        GameUiContextController gameUiContextController = restConnection.proxy(GameUiContextController.class);
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        JsonAssert.assertViaJson("Shape3DControllerTest_Shape3Ds.json",
                s -> s.replace("\"$IMAGE_1_ID$\"", Integer.toString(IMAGE_1_ID))
                        .replace("\"$IMAGE_2_ID$\"", Integer.toString(IMAGE_2_ID))
                        .replace("\"$IMAGE_3_ID$\"", Integer.toString(IMAGE_3_ID))
                        .replace("\"78-Plane_029\"", key1)
                        .replace("\"78-Trunk33\"", key2),
                new JsonAssert.IdSuppressor[]{new JsonAssert.IdSuppressor("/0", "id")},
                getClass(),
                coldGameUiContext.getShape3Ds());
    }
}
