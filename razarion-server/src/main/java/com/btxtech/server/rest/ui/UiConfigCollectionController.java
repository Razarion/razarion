package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.UiConfigCollection;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.server.service.ui.GltfService;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.server.service.ui.ParticleSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/ui-config-collection")
public class UiConfigCollectionController {
    @Autowired
    private BabylonMaterialService babylonMaterialPersistence;
    @Autowired
    private GltfService gltfPersistence;
    @Autowired
    private Model3DService model3DPersistence;
    @Autowired
    private ParticleSystemService particleSystemPersistence;

    @GetMapping(value = "/get", produces = "application/json")
    public UiConfigCollection getUiConfigCollection() {
        return new UiConfigCollection()
                .babylonMaterials(babylonMaterialPersistence.readAllBaseEntities())
                .gltfs(gltfPersistence.readAllBaseEntitiesJson())
                .model3DEntities(model3DPersistence.readAllBaseEntitiesJson())
                .particleSystemEntities(particleSystemPersistence.readAllBaseEntitiesJson())
//TODO                .selectionItemMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_SELECTION_MATERIAL))
//                .healthBarNodeMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_HEALTH_BAR_NODE_MATERIAL))
//                .progressBarNodeMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_PROGRESS_BAR_NODE_MATERIAL))
                ;
    }
}
