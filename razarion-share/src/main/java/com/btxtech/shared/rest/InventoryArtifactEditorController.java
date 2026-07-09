package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.datatypes.InventoryArtifact;

import javax.ws.rs.Path;

/**
 * CRUD editor endpoint for {@link InventoryArtifact}. Ported from the legacy
 * controltheland project.
 */
@Path(CommonUrl.INVENTORY_ARTIFACT_EDITOR_PATH)
public interface InventoryArtifactEditorController extends CrudController<InventoryArtifact> {
}
