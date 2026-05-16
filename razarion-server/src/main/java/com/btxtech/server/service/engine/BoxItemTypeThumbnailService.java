package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BoxItemTypeEntity;
import com.btxtech.server.model.engine.ThumbnailCameraEmbeddable;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.repository.engine.BoxItemTypeRepository;
import com.btxtech.server.rest.editor.BoxItemTypeThumbnailConfig;
import com.btxtech.server.service.ui.ImageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Same shape as BaseItemTypeThumbnailService — keeps the box side of the
 * studio grid in sync with the matching BoxItemTypeEntity row.
 */
@Service
public class BoxItemTypeThumbnailService {
    private final BoxItemTypeRepository boxItemTypeRepository;
    private final ImageService imageService;

    public BoxItemTypeThumbnailService(BoxItemTypeRepository boxItemTypeRepository,
                                       ImageService imageService) {
        this.boxItemTypeRepository = boxItemTypeRepository;
        this.imageService = imageService;
    }

    @Transactional
    public List<BoxItemTypeThumbnailConfig> readAll() {
        return boxItemTypeRepository.findAll().stream()
                .map(this::toConfig)
                .toList();
    }

    @Transactional
    public BoxItemTypeThumbnailConfig read(int boxItemTypeId) {
        return toConfig(loadEntity(boxItemTypeId));
    }

    @Transactional
    public void update(BoxItemTypeThumbnailConfig config) {
        BoxItemTypeEntity entity = loadEntity(config.getBoxItemTypeId());
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
        boxItemTypeRepository.save(entity);
    }

    @Transactional
    public void uploadImage(int boxItemTypeId, byte[] pngBytes) {
        BoxItemTypeEntity entity = loadEntity(boxItemTypeId);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            imageService.save(existing.getId(), pngBytes, "image/png");
        } else {
            ImageLibraryEntity created = imageService.createImage(pngBytes, "image/png");
            entity.setThumbnail(created);
            boxItemTypeRepository.save(entity);
        }
    }

    @Transactional
    public void reset(int boxItemTypeId) {
        BoxItemTypeEntity entity = loadEntity(boxItemTypeId);
        entity.setThumbnailCamera(null);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            entity.setThumbnail(null);
            boxItemTypeRepository.save(entity);
            imageService.delete(existing.getId());
        } else {
            boxItemTypeRepository.save(entity);
        }
    }

    private BoxItemTypeEntity loadEntity(int id) {
        return boxItemTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BoxItemType not found: " + id));
    }

    private BoxItemTypeThumbnailConfig toConfig(BoxItemTypeEntity entity) {
        BoxItemTypeThumbnailConfig config = new BoxItemTypeThumbnailConfig();
        config.setBoxItemTypeId(entity.getId());
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
