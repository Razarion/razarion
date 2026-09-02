package com.btxtech.server.service.ui;

import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.repository.ui.ParticleSystemRepository;
import com.btxtech.server.rest.ui.MaterialSizeInfo;
import com.btxtech.server.rest.ui.ParticleSystemController;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.service.ContentDigest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticleSystemService extends AbstractBaseEntityCrudService<ParticleSystemEntity> {
    private final ImageService imageService;
    private final ParticleSystemRepository particleSystemRepository;

    public ParticleSystemService(ImageService imageService, ParticleSystemRepository particleSystemRepository) {
        super(ParticleSystemEntity.class, particleSystemRepository);
        this.imageService = imageService;
        this.particleSystemRepository = particleSystemRepository;
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

    /**
     * The entity tag for one particle system, or null if there is nothing to tag.
     * <p>
     * Reads the digest column and nothing else, so answering "still the same?" costs a small row
     * instead of a megabyte of blob. A row written before this column existed has none yet; it is
     * computed once, on the first request that needs it, and kept.
     */
    @Transactional
    public String getDataDigest(int id) {
        ParticleSystemEntity entity = getEntity(id);
        if (entity.getDataDigest() == null) {
            byte[] data = entity.getData();
            if (data == null) {
                return null;
            }
            entity.setDataDigest(ContentDigest.of(data));
            getJpaRepository().save(entity);
        }
        return entity.getDataDigest();
    }

    @Transactional
    public void setData(int id, byte[] data) {
        ParticleSystemEntity entity = getEntity(id);
        entity.setData(data);
        // In the same transaction as the bytes. A digest written separately could survive a failed
        // write, and every browser holding the old copy would then be told it is current.
        entity.setDataDigest(data != null ? ContentDigest.of(data) : null);
        getJpaRepository().save(entity);
    }

    @Transactional
    public List<MaterialSizeInfo> getParticleSizes() {
        return particleSystemRepository.findAllParticleSizes().stream()
                .map(row -> new MaterialSizeInfo(
                        (Integer) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                ))
                .toList();
    }

    @Override
    protected ParticleSystemEntity jsonToJpa(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageLibraryEntity(imageService.getImageLibraryEntity(particleSystemEntity.getImageId()));
        return particleSystemEntity;
    }

}
