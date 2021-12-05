package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.asset.AssetConfigEntity;
import com.btxtech.server.persistence.asset.MeshContainerEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.rest.AssetEditorController;
import org.junit.After;

public class AssetEditorControllerTest extends AbstractCrudTest<AssetEditorController, AssetConfig> {
    public AssetEditorControllerTest() {
        super(AssetEditorController.class, AssetConfig.class);
        enabledIgnoreInternalName();
    }

    @After
    public void cleanTables() {
        cleanTable(MeshContainerEntity.class);
        cleanTable(AssetConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(assetConfig -> assetConfig.assetMetaFileHint("C:\\dev\\projects\\razarion\\razarion-media\\unity\\Vehicles\\Assets\\Vehicles Constructor.meta"));
    }
}
