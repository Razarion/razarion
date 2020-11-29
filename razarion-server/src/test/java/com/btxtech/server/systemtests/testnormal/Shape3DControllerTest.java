package com.btxtech.server.systemtests.testnormal;

import com.btxtech.server.JsonAssert;
import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.rest.Shape3DController;
import com.btxtech.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shape3DControllerTest extends AbstractSystemTest {
    private int shape3DId;

    @Before
    public void fillTables() {
        setupImages();
        ColladaEntity colladaEntity = new ColladaEntity();
        colladaEntity.setInternalName("Shape 1");
        colladaEntity.setColladaString(TestHelper.resource2Text("Shape3DControllerTest.dae", getClass()));
        Map<String, ImageLibraryEntity> textures = new HashMap<>();
        textures.put("Material-material", getEntityManager().find(ImageLibraryEntity.class, IMAGE_1_ID));
        textures.put("Material_002-material", getEntityManager().find(ImageLibraryEntity.class, IMAGE_2_ID));
        colladaEntity.setTextures(textures);
        Map<String, ImageLibraryEntity> bumpMaps = new HashMap<>();
        bumpMaps.put("Material-material", getEntityManager().find(ImageLibraryEntity.class, IMAGE_2_ID));
        bumpMaps.put("Material_002-material", getEntityManager().find(ImageLibraryEntity.class, IMAGE_3_ID));
        colladaEntity.setBumpMaps(bumpMaps);
        Map<String, Double> bumpMapDepts = new HashMap<>();
        bumpMapDepts.put("Material-material", 0.5);
        bumpMapDepts.put("Material_002-material", 0.9);
        colladaEntity.setBumpMapDepts(bumpMapDepts);
        Map<String, Double> alphaToCoverages = new HashMap<>();
        alphaToCoverages.put("Material_002-material", 0.2);
        colladaEntity.setAlphaToCoverages(alphaToCoverages);
        Map<String, Boolean> characterRepresentings = new HashMap<>();
        characterRepresentings.put("Material_002-material", true);
        colladaEntity.setCharacterRepresentings(characterRepresentings);
        shape3DId = persistInTransaction(colladaEntity).getId();
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
    public void shape3DController() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> Shape3DConfig.class));

        String key1 = "\"" + shape3DId + "-Plane_029-Material-material" + "\"";
        String key2 = "\"" + shape3DId + "-Trunk33-Material_002-material" + "\"";

        Shape3DController shape3DController = restConnection.proxy(Shape3DController.class);
        List<VertexContainerBuffer> vertexContainerBuffers = shape3DController.getVertexBuffer();
        JsonAssert.assertViaJson("Shape3DControllerTest_vertexContainerBuffers.json",
                s -> s.replace("\"78-Plane_029-Material-material\"", key1)
                        .replace("\"78-Trunk33-Material_002-material\"", key2),
                null,
                getClass(),
                vertexContainerBuffers);
        GameUiContextController gameUiContextController = restConnection.proxy(GameUiContextController.class);
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        JsonAssert.assertViaJson("Shape3DControllerTest_Shape3Ds.json",
                s -> s.replace("\"$IMAGE_1_ID$\"", Integer.toString(IMAGE_1_ID))
                        .replace("\"$IMAGE_2_ID$\"", Integer.toString(IMAGE_2_ID))
                        .replace("\"$IMAGE_3_ID$\"", Integer.toString(IMAGE_3_ID))
                        .replace("\"78-Plane_029-Material-material\"", key1)
                        .replace("\"78-Trunk33-Material_002-material\"", key2),
                new JsonAssert.IdSuppressor[]{new JsonAssert.IdSuppressor("/0", "id")},
                getClass(),
                coldGameUiContext.getShape3Ds());
    }
}
