package com.btxtech.server.rest.ui;

import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.server.user.SecurityCheck;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@RestController
@RequestMapping("/rest/editor/particle-system/")
public class ParticleSystemController extends AbstractBaseController<ParticleSystemEntity> {
    private final Logger logger = Logger.getLogger(ParticleSystemController.class.getName());
    private final ParticleSystemService particleSystemCrudPersistence;

    public ParticleSystemController(ParticleSystemService particleSystemCrudPersistence) {
        this.particleSystemCrudPersistence = particleSystemCrudPersistence;
    }

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

    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_OCTET_STREAM)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .body(particleSystemCrudPersistence.getData(id));
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
