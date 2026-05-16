package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.ResourceItemTypeEntity;
import com.btxtech.server.model.engine.ThumbnailCameraEmbeddable;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.repository.engine.ResourceItemTypeRepository;
import com.btxtech.server.rest.editor.ResourceItemTypeThumbnailConfig;
import com.btxtech.server.service.ui.ImageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Same shape as BaseItemTypeThumbnailService — keeps the resource side of the
 * studio grid in sync with the matching ResourceItemTypeEntity row.
 *
 * Image upload reuses ImageLibrary (and the existing thumbnail FK on the
 * resource entity), framing lives in the embedded thumb_* columns.
 */
@Service
public class ResourceItemTypeThumbnailService {
    private final ResourceItemTypeRepository resourceItemTypeRepository;
    private final ImageService imageService;

    public ResourceItemTypeThumbnailService(ResourceItemTypeRepository resourceItemTypeRepository,
                                            ImageService imageService) {
        this.resourceItemTypeRepository = resourceItemTypeRepository;
        this.imageService = imageService;
    }

    @Transactional
    public List<ResourceItemTypeThumbnailConfig> readAll() {
        return resourceItemTypeRepository.findAll().stream()
                .map(this::toConfig)
                .toList();
    }

    @Transactional
    public ResourceItemTypeThumbnailConfig read(int resourceItemTypeId) {
        return toConfig(loadEntity(resourceItemTypeId));
    }

    @Transactional
    public void update(ResourceItemTypeThumbnailConfig config) {
        ResourceItemTypeEntity entity = loadEntity(config.getResourceItemTypeId());
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
        resourceItemTypeRepository.save(entity);
    }

    @Transactional
    public void uploadImage(int resourceItemTypeId, byte[] pngBytes) {
        ResourceItemTypeEntity entity = loadEntity(resourceItemTypeId);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            imageService.save(existing.getId(), pngBytes, "image/png");
        } else {
            ImageLibraryEntity created = imageService.createImage(pngBytes, "image/png");
            entity.setThumbnail(created);
            resourceItemTypeRepository.save(entity);
        }
    }

    @Transactional
    public void reset(int resourceItemTypeId) {
        ResourceItemTypeEntity entity = loadEntity(resourceItemTypeId);
        entity.setThumbnailCamera(null);
        ImageLibraryEntity existing = entity.getThumbnail();
        if (existing != null) {
            entity.setThumbnail(null);
            resourceItemTypeRepository.save(entity);
            imageService.delete(existing.getId());
        } else {
            resourceItemTypeRepository.save(entity);
        }
    }

    private ResourceItemTypeEntity loadEntity(int id) {
        return resourceItemTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ResourceItemType not found: " + id));
    }

    private ResourceItemTypeThumbnailConfig toConfig(ResourceItemTypeEntity entity) {
        ResourceItemTypeThumbnailConfig config = new ResourceItemTypeThumbnailConfig();
        config.setResourceItemTypeId(entity.getId());
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
