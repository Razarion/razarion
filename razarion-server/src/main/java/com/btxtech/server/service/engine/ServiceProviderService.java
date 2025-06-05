package com.btxtech.server.service.engine;

import com.btxtech.server.service.ui.AudioPersistence;
import com.btxtech.server.service.ui.ImagePersistence;
import com.btxtech.server.service.ui.Model3DService;
import com.btxtech.server.service.ui.ParticleSystemService;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderService {
    private final Model3DService model3DCrudPersistence;
    private final ImagePersistence imagePersistence;
    private final AudioPersistence audioPersistence;
    private final ParticleSystemService particleSystemCrudPersistence;
    private final BoxItemTypeCrudPersistence boxItemTypeCrudPersistence;
    private final InventoryItemCrudPersistence inventoryItemCrudPersistence;
    private final BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;

    public ServiceProviderService(Model3DService model3DCrudPersistence, ImagePersistence imagePersistence, AudioPersistence audioPersistence, ParticleSystemService particleSystemCrudPersistence, BoxItemTypeCrudPersistence boxItemTypeCrudPersistence, InventoryItemCrudPersistence inventoryItemCrudPersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence) {
        this.model3DCrudPersistence = model3DCrudPersistence;
        this.imagePersistence = imagePersistence;
        this.audioPersistence = audioPersistence;
        this.particleSystemCrudPersistence = particleSystemCrudPersistence;
        this.boxItemTypeCrudPersistence = boxItemTypeCrudPersistence;
        this.inventoryItemCrudPersistence = inventoryItemCrudPersistence;
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
    }

    public Model3DService getModel3DCrudPersistence() {
        return model3DCrudPersistence;
    }

    public ImagePersistence getImagePersistence() {
        return imagePersistence;
    }

    public AudioPersistence getAudioPersistence() {
        return audioPersistence;
    }

    public ParticleSystemService getParticleSystemCrudPersistence() {
        return particleSystemCrudPersistence;
    }

    public BoxItemTypeCrudPersistence getBoxItemTypeCrudPersistence() {
        return boxItemTypeCrudPersistence;
    }

    public InventoryItemCrudPersistence getInventoryItemCrudPersistence() {
        return inventoryItemCrudPersistence;
    }

    public BaseItemTypeCrudPersistence getBaseItemTypeCrudPersistence() {
        return baseItemTypeCrudPersistence;
    }
}

