package com.btxtech.server.service.engine;

import com.btxtech.server.service.ui.AudioService;
import com.btxtech.server.service.ui.ImageService;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.server.service.ui.ParticleSystemService;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderService {
    private final Model3DService model3DCrudPersistence;
    private final ImageService imageService;
    private final AudioService audioPersistence;
    private final ParticleSystemService particleSystemCrudPersistence;
    private final BoxItemTypeCrudService boxItemTypeCrudPersistence;
    private final InventoryItemService inventoryItemCrudPersistence;
    private final BaseItemTypeService baseItemTypeCrudPersistence;

    public ServiceProviderService(Model3DService model3DCrudPersistence, ImageService imageService, AudioService audioPersistence, ParticleSystemService particleSystemCrudPersistence, BoxItemTypeCrudService boxItemTypeCrudPersistence, InventoryItemService inventoryItemCrudPersistence, BaseItemTypeService baseItemTypeCrudPersistence) {
        this.model3DCrudPersistence = model3DCrudPersistence;
        this.imageService = imageService;
        this.audioPersistence = audioPersistence;
        this.particleSystemCrudPersistence = particleSystemCrudPersistence;
        this.boxItemTypeCrudPersistence = boxItemTypeCrudPersistence;
        this.inventoryItemCrudPersistence = inventoryItemCrudPersistence;
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
    }

    public Model3DService getModel3DCrudPersistence() {
        return model3DCrudPersistence;
    }

    public ImageService getImagePersistence() {
        return imageService;
    }

    public AudioService getAudioPersistence() {
        return audioPersistence;
    }

    public ParticleSystemService getParticleSystemCrudPersistence() {
        return particleSystemCrudPersistence;
    }

    public BoxItemTypeCrudService getBoxItemTypeCrudPersistence() {
        return boxItemTypeCrudPersistence;
    }

    public InventoryItemService getInventoryItemCrudPersistence() {
        return inventoryItemCrudPersistence;
    }

    public BaseItemTypeService getBaseItemTypeCrudPersistence() {
        return baseItemTypeCrudPersistence;
    }
}

