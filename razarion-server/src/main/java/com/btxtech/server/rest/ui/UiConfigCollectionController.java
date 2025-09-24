package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.UiConfigCollection;
import com.btxtech.server.service.engine.DbPropertiesService;
import com.btxtech.server.service.ui.BabylonMaterialService;
import com.btxtech.server.service.ui.GltfService;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.server.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.btxtech.shared.datatypes.DbPropertyKey.*;

@RestController
@RequestMapping("/rest/ui-config-collection")
public class UiConfigCollectionController {
    private final BabylonMaterialService babylonMaterialPersistence;
    private final GltfService gltfPersistence;
    private final Model3DService model3DPersistence;
    private final ParticleSystemService particleSystemPersistence;
    private final DbPropertiesService dbPropertiesService;
    private final UserService userService;

    public UiConfigCollectionController(BabylonMaterialService babylonMaterialPersistence,
                                        GltfService gltfPersistence,
                                        Model3DService model3DPersistence,
                                        ParticleSystemService particleSystemPersistence,
                                        DbPropertiesService dbPropertiesService,
                                        UserService userService) {
        this.babylonMaterialPersistence = babylonMaterialPersistence;
        this.gltfPersistence = gltfPersistence;
        this.model3DPersistence = model3DPersistence;
        this.particleSystemPersistence = particleSystemPersistence;
        this.dbPropertiesService = dbPropertiesService;
        this.userService = userService;
    }

    @GetMapping(value = "get", produces = "application/json")
    public UiConfigCollection getUiConfigCollection() {
        var userContext = userService.getUserContextFromContext();
        return new UiConfigCollection()
                .registerState(userContext.getRegisterState())
                .name(userContext.getName())
                .babylonMaterials(babylonMaterialPersistence.readAllBaseEntities())
                .gltfs(gltfPersistence.readAllBaseEntitiesJson())
                .model3DEntities(model3DPersistence.readAllBaseEntitiesJson())
                .particleSystemEntities(particleSystemPersistence.readAllBaseEntitiesJson())
                .selectionItemMaterialId(dbPropertiesService.getBabylonMaterialProperty(ITEM_SELECTION_MATERIAL));
    }
}
