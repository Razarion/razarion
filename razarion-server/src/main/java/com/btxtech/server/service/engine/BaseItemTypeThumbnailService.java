package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.model.engine.ThumbnailCameraEmbeddable;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.repository.engine.BaseItemTypeRepository;
import com.btxtech.server.rest.editor.BaseItemTypeThumbnailConfig;
import com.btxtech.server.service.ui.ImageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read/write the studio-only thumbnail metadata for BaseItemTypes:
 *   - Camera framing (alpha/beta/radius/target) embedded in BASE_ITEM_TYPE
 *   - The thumbnail PNG itself, stored in the existing IMAGE_LIBRARY table
 *     and linked via BaseItemTypeEntity.thumbnail (the same FK that ItemCockpit
 *     reads through /rest/image/{id}).
 *
 * Uploads update an existing ImageLibraryEntity in place when one is linked,
 * otherwise create a new one and set the FK. Reset wipes both the camera
 * override and the image.
 */
@Service
public class BaseItemTypeThumbnailService {
    private final BaseItemTypeRepository baseItemTypeRepository;
    private final ImageService imageService;

    public BaseItemTypeThumbnailService(BaseItemTypeRepository baseItemTypeRepository,
                                        ImageService imageService) {
        this.baseItemTypeRepository = baseItemTypeRepository;
        this.imageService = imageService;
    }

    @Transactional
    public List<BaseItemTypeThumbnailConfig> readAll() {
        // Returns ALL baseItemTypes (not just those with framing) so the studio
        // grid can render every item — with or without thumbnail/framing.
        return baseItemTypeRepository.findAll().stream()
                .map(this::toConfig)
                .toList();
    }

    @Transactional
    public BaseItemTypeThumbnailConfig read(int baseItemTypeId) {
        return toConfig(loadEntity(baseItemTypeId));
    }

    @Transactional
    public void update(BaseItemTypeThumbnailConfig config) {
        BaseItemTypeEntity entity = loadEntity(config.getBaseItemTypeId());
        ThumbnailCameraEmbeddable camera = entity.getThumbnailCamera();
        if (camera == null) {
            camera = new ThumbnailCameraEmbeddable();
        }
        camera.setAlpha(config.getAlpha());
        camera.setBeta(config.getBeta());
        camera.setRadius(config.getRadius());
        camera.setTargetX(config.getTargetX());
        camera.setTargetY(config.getTargetY());
        camera.setTargetZ(config.getTargetZ());
        camera.setDiplomacy(config.getDiplomacy());
        entity.setThumbnailCamera(camera);
        baseItemTypeRepository.save(entity);
    }

    @Transactional
    public void uploadImage(int baseItemTypeId, byte[] pngBytes) {
        BaseItemTypeEntity entity = loadEntity(baseItemTypeId);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            imageService.save(existing.getId(), pngBytes, "image/png");
        } else {
            ImageLibraryEntity created = imageService.createImage(pngBytes, "image/png");
            entity.setThumbnail(created);
            baseItemTypeRepository.save(entity);
        }
    }

    /** Reset: drop camera override and delete the linked thumbnail image. */
    @Transactional
    public void reset(int baseItemTypeId) {
        BaseItemTypeEntity entity = loadEntity(baseItemTypeId);
        entity.setThumbnailCamera(null);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            entity.setThumbnail(null);
            baseItemTypeRepository.save(entity);
            imageService.delete(existing.getId());
        } else {
            baseItemTypeRepository.save(entity);
        }
    }

    private BaseItemTypeEntity loadEntity(int id) {
        return baseItemTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BaseItemType not found: " + id));
    }

    private BaseItemTypeThumbnailConfig toConfig(BaseItemTypeEntity entity) {
        BaseItemTypeThumbnailConfig config = new BaseItemTypeThumbnailConfig();
        config.setBaseItemTypeId(entity.getId());
        config.setInternalName(entity.getInternalName());
        if (entity.getModel3DEntity() != null) {
            config.setModel3DId(entity.getModel3DEntity().getId());
        }
        if (entity.getThumbnail() != null) {
            config.setThumbnailImageId(entity.getThumbnail().getId());
        }
        ThumbnailCameraEmbeddable camera = entity.getThumbnailCamera();
        if (camera != null) {
            config.setAlpha(camera.getAlpha());
            config.setBeta(camera.getBeta());
            config.setRadius(camera.getRadius());
            config.setTargetX(camera.getTargetX());
            config.setTargetY(camera.getTargetY());
            config.setTargetZ(camera.getTargetZ());
            config.setDiplomacy(camera.getDiplomacy());
        }
        return config;
    }
}
