package com.btxtech.server.rest;

import com.btxtech.server.persistence.asset.MeshContainerCrudPersistence;
import com.btxtech.server.persistence.asset.MeshContainerEntity;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.rest.MeshContainerEditorController;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class MeshContainerEditorControllerImpl extends AbstractCrudController<MeshContainer, MeshContainerEntity> implements MeshContainerEditorController {
    @Inject
    private MeshContainerCrudPersistence meshContainerCrudPersistence;

    @Override
    protected MeshContainerCrudPersistence getCrudPersistence() {
        return meshContainerCrudPersistence;
    }
}
