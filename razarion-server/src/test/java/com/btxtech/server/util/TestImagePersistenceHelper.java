package com.btxtech.server.util;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;

import javax.persistence.EntityManager;

public class TestImagePersistenceHelper extends ImagePersistence {
    private EntityManager entityManager;

    public TestImagePersistenceHelper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ImageLibraryEntity getImageLibraryEntity(Integer imageId) {
        if(imageId == null) {
            return null;
        }
        return entityManager.find(ImageLibraryEntity.class, imageId);
    }
}
