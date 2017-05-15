package com.btxtech.server.persistence;

import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DbPropertyKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 15.05.2017.
 */
@Singleton
public class DbPropertiesService {
    private Logger logger = Logger.getLogger(DbPropertiesService.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Transactional
    public Integer getAudioIdProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getAudio() != null) {
            return dbPropertiesEntity.getAudio().getId();
        }
        logger.warning("No audio property for: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public Integer getShape3DIdProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getShape3DId() != null) {
            return dbPropertiesEntity.getShape3DId().getId();
        }
        logger.warning("No Shape3D property for: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public int getIntProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getIntValue() != null) {
            return dbPropertiesEntity.getIntValue();
        }
        logger.warning("No int property for: " + dbPropertyKey);
        return 0;
    }

    @Transactional
    public double getDoubleProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getDoubleValue() != null) {
            return dbPropertiesEntity.getDoubleValue();
        }
        logger.warning("No double property for: " + dbPropertyKey);
        return 0;
    }

    @Transactional
    public Color getColorProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getColor() != null) {
            return dbPropertiesEntity.getColor();
        }
        logger.warning("No color property for: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public Integer getImageIdProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getImage() != null) {
            return dbPropertiesEntity.getImage().getId();
        }
        logger.warning("No image property for: " + dbPropertyKey);
        return null;
    }

    @Transactional
    @SecurityCheck
    public void setIntProperty(Integer value, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        dbPropertiesEntity.setIntValue(value);
        entityManager.merge(dbPropertiesEntity);
    }

    @Transactional
    @SecurityCheck
    public void setDoubleProperty(Double value, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        dbPropertiesEntity.setDoubleValue(value);
        entityManager.merge(dbPropertiesEntity);
    }

    @Transactional
    @SecurityCheck
    public void setAudioIdProperty(Integer audioId, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        if (audioId != null) {
            dbPropertiesEntity.setAudio(audioPersistence.getAudioLibraryEntity(audioId));
        } else {
            dbPropertiesEntity.setAudio(null);
        }
        entityManager.merge(dbPropertiesEntity);
    }

    @Transactional
    @SecurityCheck
    public void setImageIdProperty(Integer imageId, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        if (imageId != null) {
            dbPropertiesEntity.setImage(imagePersistence.getImageLibraryEntity(imageId));
        } else {
            dbPropertiesEntity.setImage(null);
        }
        entityManager.merge(dbPropertiesEntity);
    }

    @Transactional
    @SecurityCheck
    public void setshape3DIdProperty(Integer shape3DId, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        if (shape3DId != null) {
            dbPropertiesEntity.setShape3DId(shape3DPersistence.getColladaEntity(shape3DId));
        } else {
            dbPropertiesEntity.setShape3DId(null);
        }
        entityManager.merge(dbPropertiesEntity);
    }

    @Transactional
    @SecurityCheck
    public void setColorProperty(Color color, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        dbPropertiesEntity.setColor(color);
        entityManager.merge(dbPropertiesEntity);
    }

    private DbPropertiesEntity getProperty(DbPropertyKey dbPropertyKey) {
        return entityManager.find(DbPropertiesEntity.class, dbPropertyKey.getKey());
    }
}
