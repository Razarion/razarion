package com.btxtech.server.rest;

import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/ui-config-collection")
public class UiConfigCollectionController {
    @Autowired
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;

    @GetMapping(value = "/get", produces = "application/json")
    public UiConfigCollection getUiConfigCollection() {
        return new UiConfigCollection()
                .babylonMaterials(babylonMaterialCrudPersistence.readAllBaseEntities())
//                .gltfs(gltfCrudPersistence.readAllBaseEntitiesJson())
//                .model3DEntities(model3DCrudPersistence.readAllBaseEntitiesJson())
//                .particleSystemEntities(particleSystemCrudPersistence.readAllBaseEntitiesJson())
//                .selectionItemMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_SELECTION_MATERIAL))
//                .healthBarNodeMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_HEALTH_BAR_NODE_MATERIAL))
//                .progressBarNodeMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_PROGRESS_BAR_NODE_MATERIAL))
                ;
    }
}
