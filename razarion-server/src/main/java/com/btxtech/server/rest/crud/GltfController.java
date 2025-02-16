package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.GltfCrudPersistence;
import com.btxtech.server.persistence.ui.GltfEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path(CommonUrl.GLTF_CONTROLLER)
public class GltfController extends BaseEntityController<GltfEntity> {
    private final Logger logger = Logger.getLogger(GltfController.class.getName());
    @Inject
    private GltfCrudPersistence gltfCrudPersistence;

    public static GltfEntity jpa2JsonStatic(GltfEntity gltfEntity) {
        Map<String, Integer> materialGltfNames = new HashMap<>();
        if (gltfEntity.getGltfBabylonMaterials() != null) {
            gltfEntity.getGltfBabylonMaterials()
                    .forEach(gltfBabylonMaterial -> materialGltfNames.put(gltfBabylonMaterial.getGltfMaterialName(), gltfBabylonMaterial.getBabylonMaterialEntity().getId()));
        }
        gltfEntity.setMaterialGltfNames(materialGltfNames);
        return gltfEntity;
    }

    @Override
    protected AbstractEntityCrudPersistence<GltfEntity> getEntityCrudPersistence() {
        return gltfCrudPersistence;
    }

    @GET
    @Path("glb/{id}")
    public Response getGlb(@PathParam("id") int id) {
        try {
            return Response.ok(gltfCrudPersistence.getGlb(id),
                    MediaType.APPLICATION_OCTET_STREAM).lastModified(new Date()).build();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Can not load GltfEntity for id: " + id, e);
            throw e;
        }
    }

    @PUT
    @Path("upload-glb/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void uploadGlb(@PathParam("id") int id, byte[] data) {
        gltfCrudPersistence.setGlb(id, data);
    }

    @Override
    protected GltfEntity jpa2Json(GltfEntity gltfEntity) {
        return jpa2JsonStatic(gltfEntity);
    }

}
