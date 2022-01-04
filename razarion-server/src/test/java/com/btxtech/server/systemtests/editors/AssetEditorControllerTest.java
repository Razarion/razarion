package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ColladaMaterialEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.asset.AssetConfigEntity;
import com.btxtech.server.persistence.asset.MeshContainerEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.AssetEditorController;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.rest.MeshContainerEditorController;
import com.btxtech.shared.rest.Shape3DController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.test.JsonAssert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssetEditorControllerTest extends AbstractSystemTest {
    @After
    public void cleanTables() {
        runInTransaction(entityManager -> entityManager.createNativeQuery("UPDATE ASSET_MESH_CONTAINER SET parent_id = NULL").executeUpdate());
        cleanTable(MeshContainerEntity.class);
        cleanTable(AssetConfigEntity.class);
        cleanTableNative("COLLADA_ANIMATIONS");
        cleanTable(ColladaMaterialEntity.class);
        cleanTable(ColladaEntity.class);
        cleanTable(ImageLibraryEntity.class);
    }

    @Test
    public void crud() {
        RestConnection assetRestConnection = new RestConnection(new ObjectMapperResolver(() -> AssetConfig.class));
        assetRestConnection.loginAdmin();
        AssetEditorController underTest = assetRestConnection.proxy(AssetEditorController.class);

        RestConnection meshContainerRestConnection = new RestConnection(new ObjectMapperResolver(() -> MeshContainer.class));
        meshContainerRestConnection.loginAdmin();
        MeshContainerEditorController meshContainerEditorController = meshContainerRestConnection.proxy(MeshContainerEditorController.class);

        Assert.assertTrue(underTest.getObjectNameIds().isEmpty());
        // Create
        AssetConfig assetConfig = underTest.create();
        Assert.assertEquals(0, meshContainerEditorController.getObjectNameIds().size());
        // Update
        assetConfig.assetMetaFileHint("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");
        underTest.update(assetConfig);
        assetConfig = underTest.read(assetConfig.getId());
        int meshControllerCount1 = meshContainerEditorController.getObjectNameIds().size();
        underTest.update(assetConfig);
        AssetConfig assetConfig2 = underTest.read(assetConfig.getId());
        int meshControllerCount2 = meshContainerEditorController.getObjectNameIds().size();
        Assert.assertEquals(meshControllerCount1, meshControllerCount2);
//        // -----------------------------------------
        try {
            writeJsonFiles("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-ui-service\\src\\test\\resources\\", assetConfig);
            writeJsonFiles("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\mesh_container\\", assetConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        // -----------------------------------------
        JsonAssert.assertViaJson(assetConfig, assetConfig2, null);


//        JsonAssert.assertViaJson("TestAsset1.json",
//                null,
//                new JsonAssert.IdSuppressor[]{
//                        new JsonAssert.IdSuppressor("", "id"),
//                        new JsonAssert.IdSuppressor("/meshContainers", "id", true),
//                        new JsonAssert.IdSuppressor("/children", "id", true)
//                },
//                getClass(),
//                assetConfig,
//                true);
//        // Check Shape3D controller
//        Shape3DEditorController shape3DEditorController = assetRestConnection.proxy(Shape3DEditorController.class);
//
//        Set<Integer> shape3Ds = new HashSet<>();
//
//        recursiveFillShape3DIds(assetConfig.getMeshContainers(), shape3Ds);
//
//        shape3DEditorController.read(shape3Ds.stream().findFirst().orElseThrow(IllegalArgumentException::new));

        System.out.println("------------------------------------");
        System.out.println("Id: " + assetConfig.getId());
        System.out.println("UnityAssetGuid: " + assetConfig.getUnityAssetGuid());
        System.out.println("AssetMetaFileHint: " + assetConfig.getAssetMetaFileHint());
        dumpMeshContainer(assetConfig.getMeshContainers(), "--");
        System.out.println("------------------------------------");

    }

    private void writeJsonFiles(String path, AssetConfig assetConfig) throws IOException {
        // AssetConfig
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path, "assetConfig.json"),
                assetConfig);
        // Shape3D
        GameUiContextController gameUiContextController = setupRestAccess(GameUiContextController.class);
        ColdGameUiContext coldGameUiContext = gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path, "shape3Ds.json"),
                coldGameUiContext.getShape3Ds());
        // Fbx GUID to Shape3D
        Map<String, Integer> guid2Shape3DId = new HashMap<>();
        RestConnection shape3DEditorControllerRestConnection = new RestConnection(new ObjectMapperResolver(() -> Shape3DConfig.class));
        Shape3DEditorController shape3DEditorController = shape3DEditorControllerRestConnection.proxy(Shape3DEditorController.class);
        shape3DEditorController.getObjectNameIds().stream()
                .filter(objectNameId -> objectNameId.getInternalName() != null && !objectNameId.getInternalName().trim().isEmpty())
                .forEach(objectNameId -> guid2Shape3DId.put(objectNameId.getInternalName(), objectNameId.getId()));
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path, "guid2Shape3DId.json"), guid2Shape3DId);
        // VertexContainerBuffer
        RestConnection shape3DControllerRestConnection = new RestConnection(new ObjectMapperResolver(() -> MeshContainer.class));
        shape3DControllerRestConnection.loginAdmin();
        Shape3DController shape3DController = shape3DControllerRestConnection.proxy(Shape3DController.class);
        List<VertexContainerBuffer> vertexContainerBuffers = shape3DController.getVertexBuffer();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path, "vertexContainerBuffers.json"),
                vertexContainerBuffers);
    }

    private void dumpMeshContainer(List<MeshContainer> meshContainers, String space) {
        meshContainers.forEach(meshContainer -> {
            System.out.println(space + "-------------------------------------------");
            System.out.println(space + "InternalName: " + meshContainer.getInternalName());
            System.out.println(space + "Guid: " + meshContainer.getGuid());
            String meshString = "-";
            if (meshContainer.getMesh() != null) {
                meshString = "Shape3DId: " + meshContainer.getMesh().getShape3DId() + ", Element3DId: " + meshContainer.getMesh().getElement3DId();
            }
            System.out.println(space + "Mesh: " + meshString);
            if (meshContainer.getChildren() != null) {
                dumpMeshContainer(meshContainer.getChildren(), space + "--");
            }
        });
    }

    private void recursiveFillShape3DIds(List<MeshContainer> meshContainers, Set<Integer> shape3Ds) {
        meshContainers.forEach(meshContainer -> {
            if (meshContainer.getMesh() != null) {
                shape3Ds.add(meshContainer.getMesh().getShape3DId());
            }
            if (meshContainer.getChildren() != null) {
                recursiveFillShape3DIds(meshContainer.getChildren(), shape3Ds);
            }
        });
    }
}
