package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.model.ui.GltfEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
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

import java.util.List;

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
    private static final long SHARED_TTL_MILLIS = 60_000;
    /** The part of the answer that is the same for every player, see {@link #getUiConfigCollection()}. */
    private volatile SharedPart sharedPart;

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

    /**
     * Every start asks for this, and every material, glb, model and particle system was read from
     * the database for it and converted - the same answer for every player but the name and the
     * register state. On PROD about 200 ms of server time for 1 KB gzip (2026-09-27). The shared part
     * is kept for a minute now: an edit in the editors reaches new starts within that minute, and an
     * edited glb gets a new digest url, so nothing is served stale for longer.
     */
    @GetMapping(value = "get", produces = "application/json")
    public UiConfigCollection getUiConfigCollection() {
        var userContext = userService.getUserContextFromContext();
        SharedPart shared = sharedPart();
        return new UiConfigCollection()
                .registerState(userContext.getRegisterState())
                .name(userContext.getName())
                .babylonMaterials(shared.babylonMaterials())
                .gltfs(shared.gltfs())
                .model3DEntities(shared.model3DEntities())
                .particleSystemEntities(shared.particleSystemEntities())
                .selectionItemMaterialId(shared.selectionItemMaterialId());
    }

    private SharedPart sharedPart() {
        SharedPart shared = sharedPart;
        long now = System.currentTimeMillis();
        if (shared == null || now - shared.loadedAt() > SHARED_TTL_MILLIS) {
            shared = new SharedPart(babylonMaterialPersistence.readAllBaseEntities(),
                    gltfPersistence.readAllBaseEntitiesJson(),
                    model3DPersistence.readAllBaseEntitiesJson(),
                    particleSystemPersistence.readAllBaseEntitiesJson(),
                    dbPropertiesService.getBabylonMaterialProperty(ITEM_SELECTION_MATERIAL),
                    now);
            sharedPart = shared;
        }
        return shared;
    }

    private record SharedPart(List<BabylonMaterialEntity> babylonMaterials,
                              List<GltfEntity> gltfs,
                              List<Model3DEntity> model3DEntities,
                              List<ParticleSystemEntity> particleSystemEntities,
                              Integer selectionItemMaterialId,
                              long loadedAt) {
    }
}
