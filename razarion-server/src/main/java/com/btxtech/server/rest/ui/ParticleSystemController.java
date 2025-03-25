package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@RestController
@RequestMapping(CommonUrl.PARTICLE_SYSTEM_CONTROLLER)
public class ParticleSystemController extends AbstractBaseController<ParticleSystemEntity> {
    private final Logger logger = Logger.getLogger(ParticleSystemController.class.getName());
    @Inject
    private ParticleSystemService particleSystemCrudPersistence;

    public static ParticleSystemEntity jpa2JsonStatic(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageId(extractId(particleSystemEntity.getImageLibraryEntity(), ImageLibraryEntity::getId));
        return particleSystemEntity;
    }

    @Override
    protected AbstractBaseEntityCrudService<ParticleSystemEntity> getEntityCrudPersistence() {
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
