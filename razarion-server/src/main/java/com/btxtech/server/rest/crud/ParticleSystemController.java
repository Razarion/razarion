package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ParticleSystemCrudPersistence;
import com.btxtech.server.persistence.ui.ParticleSystemEntity;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;


@Path(CommonUrl.PARTICLE_SYSTEM_CONTROLLER)
public class ParticleSystemController extends BaseEntityController<ParticleSystemEntity> {
    private final Logger logger = Logger.getLogger(ParticleSystemController.class.getName());
    @Inject
    private ParticleSystemCrudPersistence particleSystemCrudPersistence;

    public static ParticleSystemEntity jpa2JsonStatic(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageId(extractId(particleSystemEntity.getImageLibraryEntity(), ImageLibraryEntity::getId));
        return particleSystemEntity;
    }

    @Override
    protected AbstractEntityCrudPersistence<ParticleSystemEntity> getEntityCrudPersistence() {
        return particleSystemCrudPersistence;
    }

    @Override
    protected ParticleSystemEntity jpa2Json(ParticleSystemEntity particleSystemEntity) {
        return jpa2JsonStatic(particleSystemEntity);
    }

    @GET
    @Path("data/{id}")
    public Response getData(@PathParam("id") int id) {
        try {
            return Response.ok(particleSystemCrudPersistence.getData(id),
                    MediaType.APPLICATION_OCTET_STREAM).lastModified(new Date()).build();
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @PUT
    @Path("upload/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @SecurityCheck
    public void uploadData(@PathParam("id") int id, byte[] data) {
        particleSystemCrudPersistence.setData(id, data);
    }
}
