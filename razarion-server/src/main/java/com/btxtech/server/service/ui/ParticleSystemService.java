package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.repository.ui.ParticleSystemRepository;
import com.btxtech.server.rest.ui.ParticleSystemController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticleSystemService extends AbstractBaseEntityCrudService<ParticleSystemEntity> {
    private final ImageService imageService;

    public ParticleSystemService(ImageService imageService, ParticleSystemRepository particleSystemRepository) {
        super(ParticleSystemEntity.class, particleSystemRepository);
        this.imageService = imageService;
    }

    @Transactional
    public List<ParticleSystemEntity> readAllBaseEntitiesJson() {
        return getEntities()
                .stream()
                .map(ParticleSystemController::jpa2JsonStatic)
                .collect(Collectors.toList());
    }

    @Transactional
    public byte[] getData(int id) {
        return getEntity(id).getData();
    }

    @Transactional
    public void setData(int id, byte[] data) {
        ParticleSystemEntity entity = getEntity(id);
        entity.setData(data);
        getJpaRepository().save(entity);
    }

    @Override
    protected ParticleSystemEntity jsonToJpa(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageLibraryEntity(imageService.getImageLibraryEntity(particleSystemEntity.getImageId()));
        return particleSystemEntity;
    }

}
