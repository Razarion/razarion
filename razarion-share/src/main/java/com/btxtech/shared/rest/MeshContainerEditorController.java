package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import javax.ws.rs.Path;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Path(CommonUrl.MESH_CONTAINER_EDITOR_PATH)
public interface MeshContainerEditorController extends CrudController<MeshContainer> {
}
