package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.GltfCrudPersistence;
import com.btxtech.server.persistence.Model3DCrudPersistence;
import com.btxtech.server.persistence.ui.Model3DEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path(CommonUrl.MODEL_3D_CONTROLLER)
public class Model3DController extends BaseEntityController<Model3DEntity> {
    private final Logger logger = Logger.getLogger(Model3DController.class.getName());
    @Inject
    private Model3DCrudPersistence model3DCrudPersistence;
    @Inject
    private GltfCrudPersistence gltfCrudPersistence;

    @Override
    protected AbstractEntityCrudPersistence<Model3DEntity> getEntityCrudPersistence() {
        return model3DCrudPersistence;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getModel3DsByGltf/{gltfId}")
    @SecurityCheck
    @Transactional
    public List<Model3DEntity> getModel3DsByGltf(@PathParam("gltfId") int gltfId) {
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
    protected Model3DEntity jsonToJpa(Model3DEntity model3DEntity) {
        return Model3DController.jsonToJpaStatic(gltfCrudPersistence, model3DEntity);
    }

    @Override
    protected Model3DEntity jpa2Json(Model3DEntity model3DEntity) {
        return jpa2JsonStatic(model3DEntity);
    }

    public static Model3DEntity jsonToJpaStatic(GltfCrudPersistence gltfCrudPersistence, Model3DEntity model3DEntity) {
        if (model3DEntity.getGltfEntityId() != null) {
            model3DEntity.setGltfEntity(gltfCrudPersistence.getEntity(model3DEntity.getGltfEntityId()));
        }
        return model3DEntity;
    }

    public static Model3DEntity jpa2JsonStatic(Model3DEntity model3DEntity) {
        if (model3DEntity.getGltfEntity() != null) {
            model3DEntity.setGltfEntityId(model3DEntity.getGltfEntity().getId());
        }
        return model3DEntity;
    }
}
