package com.btxtech.server.rest.ui;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.rest.AbstractBaseController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.NoSuchEntityException;
import com.btxtech.server.service.ContentDigest;
import com.btxtech.server.service.ui.ParticleSystemService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.server.service.PersistenceUtil.extractId;


@RestController
@RequestMapping("/rest/editor/particle-system/")
public class ParticleSystemController extends AbstractBaseController<ParticleSystemEntity> {
    /** Hold it, but ask before every use. Safe only in the presence of an entity tag. */
    private static final CacheControl REVALIDATE = CacheControl.noCache().mustRevalidate();
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

    /**
     * The particle systems, as a conditional GET.
     * <p>
     * Two of these are read on every game start and weigh 1.5 MB between them - and they were the
     * larger half of an anonymous 2.5 MB that the payload measurement could only call "other".
     * They were found by naming the heaviest unrecognised resource in the telemetry detail, which
     * is the whole reason that field exists.
     * <p>
     * Same arrangement as the model and the materials: {@code no-cache} plus an entity tag keeps
     * the guarantee that an edit reaches the player, while unchanged costs a comparison instead of
     * a megabyte. The upload beside it stays under the blanket no-store rule.
     */
    @GetMapping(value = "/data/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getData(@PathVariable("id") int id,
                                          @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false)
                                          String ifNoneMatch) {
        try {
            String digest = particleSystemCrudPersistence.getDataDigest(id);
            if (digest == null) {
                // No bytes to tag. Answer as before rather than inventing a tag for nothing.
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(particleSystemCrudPersistence.getData(id));
            }
            String eTag = ContentDigest.eTag(digest);
            if (ContentDigest.matches(ifNoneMatch, eTag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(eTag)
                        .cacheControl(REVALIDATE)
                        .build();
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .eTag(eTag)
                    .cacheControl(REVALIDATE)
                    .body(particleSystemCrudPersistence.getData(id));
        } catch (NoSuchEntityException e) {
            // Not there is not broken. The 404 says which.
            throw e;
        } catch (Throwable e) {
            logger.log(Level.WARNING, "Can not load ParticleSystemEntity for id: " + id, e);
            throw e;
        }
    }

    @GetMapping("sizes")
    public List<MaterialSizeInfo> getParticleSizes() {
        return particleSystemCrudPersistence.getParticleSizes();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(value = "upload/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void uploadData(@PathVariable("id") int id, @RequestBody byte[] data) {
        particleSystemCrudPersistence.setData(id, data);
    }
}
