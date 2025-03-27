package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.repository.ui.ParticleSystemRepository;
import com.btxtech.server.rest.ui.ParticleSystemController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticleSystemService extends AbstractBaseEntityCrudService<ParticleSystemEntity> {
    private final ImagePersistence imagePersistence;
    private final ParticleSystemRepository particleSystemRepository;

    public ParticleSystemService(ImagePersistence imagePersistence, ParticleSystemRepository particleSystemRepository) {
        super(ParticleSystemEntity.class);
        this.imagePersistence = imagePersistence;
        this.particleSystemRepository = particleSystemRepository;
    }

    @Override
    protected JpaRepository<ParticleSystemEntity, Integer> getJpaRepository() {
        return particleSystemRepository;
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
        particleSystemRepository.save(entity);
    }

    @Override
    protected ParticleSystemEntity jsonToJpa(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageLibraryEntity(imagePersistence.getImageLibraryEntity(particleSystemEntity.getImageId()));
        return particleSystemEntity;
    }

}
