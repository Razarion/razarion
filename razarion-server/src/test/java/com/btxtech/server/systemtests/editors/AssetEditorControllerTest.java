package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ColladaMaterialEntity;
import com.btxtech.server.persistence.asset.AssetConfigEntity;
import com.btxtech.server.persistence.asset.MeshContainerEntity;
import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.server.systemtests.framework.ObjectMapperResolver;
import com.btxtech.server.systemtests.framework.RestConnection;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.rest.AssetEditorController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.test.JsonAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
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
    }

    @Test
    public void crud() {
        RestConnection restConnection = new RestConnection(new ObjectMapperResolver(() -> AssetConfig.class));
        restConnection.loginAdmin();
        AssetEditorController underTest = restConnection.proxy(AssetEditorController.class);
        Assert.assertTrue(underTest.getObjectNameIds().isEmpty());
        // Create
        AssetConfig assetConfig = underTest.create();
        // Update
        assetConfig.assetMetaFileHint("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta");
        underTest.update(assetConfig);
        assetConfig = underTest.read(assetConfig.getId());
        JsonAssert.assertViaJson("TestAsset1.json",
                null,
                new JsonAssert.IdSuppressor[]{
                        new JsonAssert.IdSuppressor("", "id"),
                        new JsonAssert.IdSuppressor("/meshContainers", "id", true)
                },
                getClass(),
                assetConfig);
        // Check Shape3D controller
        Shape3DEditorController shape3DEditorController = restConnection.proxy(Shape3DEditorController.class);

        Set<Integer> shape3Ds = new HashSet<>();

        recursiveFillShape3DIds(assetConfig.getMeshContainers(), shape3Ds);

        shape3DEditorController.read(shape3Ds.stream().findFirst().orElseThrow(IllegalArgumentException::new));

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
