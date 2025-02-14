package com.btxtech.server.persistence;

import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
import com.btxtech.server.persistence.ui.ParticleSystemEntity;
import com.btxtech.server.rest.crud.ParticleSystemController;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Basic;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ParticleSystemCrudPersistence extends AbstractEntityCrudPersistence<ParticleSystemEntity> {
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private byte[] data;
    @Inject
    private ImagePersistence imagePersistence;
    @PersistenceContext
    private EntityManager entityManager;

    public ParticleSystemCrudPersistence() {
        super(ParticleSystemEntity.class);
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
        entityManager.merge(entity);
    }


    @Override
    protected ParticleSystemEntity jsonToJpa(ParticleSystemEntity particleSystemEntity) {
        particleSystemEntity.setImageLibraryEntity(imagePersistence.getImageLibraryEntity(particleSystemEntity.getImageId()));
        return particleSystemEntity;
    }

}
