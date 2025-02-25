package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.BrushCrudPersistence;
import com.btxtech.server.persistence.ui.BrushConfigEntity;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path(CommonUrl.BRUSH_EDITOR_PATH)
public class BrushEditorController extends BaseEntityController<BrushConfigEntity> {
    @Inject
    private BrushCrudPersistence persistenceService;

    @Override
    protected AbstractEntityCrudPersistence<BrushConfigEntity> getEntityCrudPersistence() {
        return persistenceService;
    }
}
