package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ui.ParticleSystemService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    protected AbstractBaseEntityCrudService<ParticleSystemEntity> getBaseEntityCrudService() {
        return particleSystemCrudPersistence;
    }

    @Override
    protected ParticleSystemEntity jpa2Json(ParticleSystemEntity particleSystemEntity) {
        return jpa2JsonStatic(particleSystemEntity);
    }

    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id) {
        try {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(particleSystemCrudPersistence.getData(id));
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Can not load BabylonMaterialEntity for id: " + id, e);
            throw e;
        }
    }

    @RolesAllowed(Roles.ADMIN)
    @PutMapping(value = "upload/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void uploadData(@PathVariable("id") int id, @RequestBody byte[] data) {
        particleSystemCrudPersistence.setData(id, data);
    }
}
