package com.btxtech.server.rest;

import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import com.btxtech.server.persistence.DbPropertiesService;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

import static com.btxtech.shared.datatypes.DbPropertyKey.*;

@Path(CommonUrl.UI_CONFIG_COLLECTION_CONTROLLER)
public class UiConfigCollectionController {
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;
    @Inject
    private DbPropertiesService dbPropertiesService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get")
    public UiConfigCollection getUiConfigCollection() {
        return new UiConfigCollection()
                .babylonMaterials(babylonMaterialCrudPersistence.readAllBaseEntities()
                        .stream()
                        .map(babylonMaterialEntity -> babylonMaterialEntity.data(null))
                        .collect(Collectors.toList()))
                .selectionItemMaterialId(dbPropertiesService.getBabylonModelProperty(ITEM_SELECTION_MATERIAL))
                .healthBarNodeMaterialId(dbPropertiesService.getBabylonModelProperty(ITEM_HEALTH_BAR_NODE_MATERIAL))
                .progressBarNodeMaterialId(dbPropertiesService.getBabylonModelProperty(ITEM_PROGRESS_BAR_NODE_MATERIAL));
    }
}