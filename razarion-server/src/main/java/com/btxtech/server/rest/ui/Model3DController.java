package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.shared.CommonUrl;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping(CommonUrl.MODEL_3D_CONTROLLER)
public class Model3DController extends AbstractBaseController<Model3DEntity> {
    private final Logger logger = Logger.getLogger(Model3DController.class.getName());
    private final Model3DService model3DCrudPersistence;

    public Model3DController(Model3DService model3DCrudPersistence) {
        this.model3DCrudPersistence = model3DCrudPersistence;
    }

    public static Model3DEntity jpa2JsonStatic(Model3DEntity model3DEntity) {
        if (model3DEntity.getGltfEntity() != null) {
            model3DEntity.setGltfEntityId(model3DEntity.getGltfEntity().getId());
        }
        return model3DEntity;
    }

    @Override
    protected AbstractBaseEntityCrudService<Model3DEntity> getBaseEntityCrudService() {
        return model3DCrudPersistence;
    }

    @RolesAllowed(Roles.ADMIN)
    @Transactional
    @GetMapping(value = "getModel3DsByGltf/{gltfId}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<Model3DEntity> getModel3DsByGltf(@PathVariable("gltfId") int gltfId) {
        try {
            return model3DCrudPersistence.getModel3DsByGltf(gltfId)
                    .stream()
                    .map(this::jpa2Json)
                    .toList();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load Model3DEntities for gltfId: " + gltfId, e);
            throw e;
        }
    }

    @Override
    protected Model3DEntity jpa2Json(Model3DEntity model3DEntity) {
        return jpa2JsonStatic(model3DEntity);
    }
}
