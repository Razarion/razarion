package com.btxtech.server.persistence;

import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DbPropertyKey;
import com.btxtech.shared.datatypes.DbPropertyType;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.btxtech.shared.datatypes.DbPropertyKey.fromKey;
import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PROPERTY;

/**
 * Created by Beat
 * 15.05.2017.
 */
@Singleton
public class DbPropertiesService {
    private final Logger logger = Logger.getLogger(DbPropertiesService.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private AudioPersistence audioPersistence;
    @Inject
    private ImagePersistence imagePersistence;
    @Inject
    private BabylonMaterialCrudPersistence babylonMaterialCrudPersistence;
    @Inject
    private AlarmService alarmService;

    @Transactional
    public Integer getAudioIdProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getAudio() != null) {
            return dbPropertiesEntity.getAudio().getId();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "Audio: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public Integer getBabylonMaterialProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getBabylonMaterialEntity() != null) {
            return dbPropertiesEntity.getBabylonMaterialEntity().getId();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "Babylon material: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public int getIntProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getIntValue() != null) {
            return dbPropertiesEntity.getIntValue();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "int property: " + dbPropertyKey);
        return 0;
    }

    @Transactional
    public double getDoubleProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getDoubleValue() != null) {
            return dbPropertiesEntity.getDoubleValue();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "double property: " + dbPropertyKey);
        return 0;
    }

    @Transactional
    public Color getColorProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getColor() != null) {
            return dbPropertiesEntity.getColor();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "color property: " + dbPropertyKey);
        return null;
    }

    @Transactional
    public Integer getImageIdProperty(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity != null && dbPropertiesEntity.getImage() != null) {
            return dbPropertiesEntity.getImage().getId();
        }
        alarmService.riseAlarm(INVALID_PROPERTY, "image property: " + dbPropertyKey);
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
    public void setBabylonMaterialProperty(Integer babylonModelId, DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        if (dbPropertiesEntity == null) {
            dbPropertiesEntity = new DbPropertiesEntity(dbPropertyKey.getKey());
        }
        if (babylonModelId != null) {
            dbPropertiesEntity.setBabylonMaterialEntity(babylonMaterialCrudPersistence.getBaseEntity(babylonModelId));
        } else {
            dbPropertiesEntity.setBabylonMaterialEntity(null);
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

    @Transactional
    public List<DbPropertyConfig> getDbPropertyConfigs() {
        return Arrays.stream(DbPropertyKey.values())
                .map(this::getDbPropertyConfig)
                .collect(Collectors.toList());
    }

    public DbPropertyConfig getDbPropertyConfig(DbPropertyKey dbPropertyKey) {
        DbPropertiesEntity dbPropertiesEntity = getProperty(dbPropertyKey);
        DbPropertyConfig dbPropertyConfig = new DbPropertyConfig()
                .key(dbPropertyKey.getKey())
                .dbPropertyType(dbPropertyKey.getDbPropertyType());

        if (dbPropertiesEntity == null) {
            return dbPropertyConfig;
        }

        fillDbPropertyConfig(dbPropertyConfig, dbPropertyKey.getDbPropertyType(), dbPropertiesEntity);

        return dbPropertyConfig;
    }

    private void fillDbPropertyConfig(DbPropertyConfig dbPropertyConfig, DbPropertyType dbPropertyType, DbPropertiesEntity dbPropertiesEntity) {
        switch (dbPropertyType) {
            case AUDIO:
                dbPropertyConfig.setIntValue(dbPropertiesEntity.getAudio() != null ? dbPropertiesEntity.getAudio().getId() : null);
                break;
            case BABYLON_MATERIAL:
                dbPropertyConfig.setIntValue(dbPropertiesEntity.getBabylonMaterialEntity() != null ? dbPropertiesEntity.getBabylonMaterialEntity().getId() : null);
                break;
            case INTEGER:
                dbPropertyConfig.setIntValue(dbPropertiesEntity.getIntValue());
                break;
            case DOUBLE:
                dbPropertyConfig.setDoubleValue(dbPropertiesEntity.getDoubleValue());
                break;
            case COLOR:
                logger.warning("DbPropertiesService COLOR not supported");
                break;
            case IMAGE:
                dbPropertyConfig.setIntValue(dbPropertiesEntity.getImage() != null ? dbPropertiesEntity.getImage().getId() : null);
                break;
            case UNKNOWN:
                logger.warning("DbPropertiesService UNKNOWN not supported");
                break;
            default:
                logger.warning("DbPropertiesService " + dbPropertyType + " not supported");
        }
    }


    @Transactional
    public void saveDbPropertyConfig(DbPropertyConfig dbPropertyConfig) {
        DbPropertyKey dbPropertyKey = fromKey(dbPropertyConfig.getKey());
        switch (dbPropertyConfig.getDbPropertyType()) {
            case AUDIO:
                setAudioIdProperty(dbPropertyConfig.getIntValue(), dbPropertyKey);
                break;
            case BABYLON_MATERIAL:
                setBabylonMaterialProperty(dbPropertyConfig.getIntValue(), dbPropertyKey);
                break;
            case INTEGER:
                setIntProperty(dbPropertyConfig.getIntValue(), dbPropertyKey);
                break;
            case DOUBLE:
                setDoubleProperty(dbPropertyConfig.getDoubleValue(), dbPropertyKey);
                break;
            case COLOR:
                logger.warning("DbPropertiesService COLOR not supported");
                break;
            case IMAGE:
                setImageIdProperty(dbPropertyConfig.getIntValue(), dbPropertyKey);
                break;
            case UNKNOWN:
                logger.warning("DbPropertiesService UNKNOWN not supported");
                break;
            default:
                logger.warning("DbPropertiesService " + dbPropertyConfig.getDbPropertyType() + " not supported");
        }
    }
}
