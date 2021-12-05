package com.btxtech.server.rest;

import com.btxtech.server.persistence.asset.AssetConfigEntity;
import com.btxtech.server.persistence.asset.AssetCrudPersistence;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.rest.AssetEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class AssetEditorControllerImpl extends AbstractCrudController<AssetConfig, AssetConfigEntity> implements AssetEditorController {
    @Inject
    private AssetCrudPersistence assetCrudPersistence;

    @Override
    protected AssetCrudPersistence getCrudPersistence() {
        return assetCrudPersistence;
    }
}
