package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AudioLibraryEntity;
import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.tracker.I18nBundleEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2016.
 */
@Entity
@Table(name = "BASE_ITEM_TYPE")
public class BaseItemTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private double radius;
    private boolean fixVerticalNorm;
    private Double angularVelocity; //Rad per second
    private Double speed;
    private Double acceleration;
    private int health;
    private int price;
    private int xpOnKilling;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nName;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private I18nBundleEntity i18nDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3DId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity spawnShape3DId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity spawnAudioId;
    private int spawnDurationMillis;
    private double dropBoxPossibility;
    private double boxPickupRange;
    private Integer unlockCrystals;
    private Integer explosionParticleConfigId_TMP;
    // TODO private List<DemolitionStepEffect> demolitionStepEffects;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity buildupTexture;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity demolitionImage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity wreckageShape3DId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private WeaponTypeEntity weaponType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private FactoryTypeEntity factoryType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HarvesterTypeEntity harvesterType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private BuilderTypeEntity builderType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private GeneratorTypeEntity generatorType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsumerTypeEntity consumerType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ItemContainerTypeEntity itemContainerType;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HouseTypeEntity houseType;

    public Integer getId() {
        return id;
    }

    public BaseItemType toBaseItemType() {
        BaseItemType baseItemType = new BaseItemType().setPrice(price).setXpOnKilling(xpOnKilling).setDropBoxPossibility(dropBoxPossibility);
        baseItemType.setBoxPickupRange(boxPickupRange).setUnlockCrystals(unlockCrystals).setHealth(health);
        baseItemType.setName(name).setId(id);
        if (i18nName != null) {
            baseItemType.setI18nName(i18nName.createI18nString());
        }
        if (i18nDescription != null) {
            baseItemType.setI18nDescription(i18nDescription.createI18nString());
        }
        baseItemType.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(radius).setFixVerticalNorm(fixVerticalNorm).setSpeed(speed).setAcceleration(acceleration).setAngularVelocity(angularVelocity));
        if (shape3DId != null) {
            baseItemType.setShape3DId(shape3DId.getId());
        }
        if (spawnShape3DId != null) {
            baseItemType.setSpawnShape3DId(spawnShape3DId.getId());
        }
        if (spawnAudioId != null) {
            baseItemType.setSpawnAudioId(spawnAudioId.getId());
        }
        baseItemType.setSpawnDurationMillis(spawnDurationMillis);
        if (thumbnail != null) {
            baseItemType.setThumbnail(thumbnail.getId());
        }
        if (demolitionImage != null) {
            baseItemType.setBaseItemDemolitionImageId(demolitionImage.getId());
        }
        if (buildupTexture != null) {
            baseItemType.setBuildupTextureId(buildupTexture.getId());
        }
        if (wreckageShape3DId != null) {
            baseItemType.setWreckageShape3DId(wreckageShape3DId.getId());
        }
        if (weaponType != null) {
            baseItemType.setWeaponType(weaponType.toWeaponType());
        }
        if (factoryType != null) {
            baseItemType.setFactoryType(factoryType.toFactoryType());
        }
        if (harvesterType != null) {
            baseItemType.setHarvesterType(harvesterType.toHarvesterType());
        }
        if (builderType != null) {
            baseItemType.setBuilderType(builderType.toBuilderType());
        }
        if (generatorType != null) {
            baseItemType.setGeneratorType(generatorType.toGeneratorType());
        }
        if (consumerType != null) {
            baseItemType.setConsumerType(consumerType.toConsumerType());
        }
        if (itemContainerType != null) {
            baseItemType.setItemContainerType(itemContainerType.toItemContainerType());
        }
        if (houseType != null) {
            baseItemType.setHouseType(houseType.toHouseType());
        }
        if (explosionParticleConfigId_TMP != null) {
            baseItemType.setExplosionParticleConfigId(explosionParticleConfigId_TMP);
        }
        return baseItemType;
    }

    public void fromBaseItemType(BaseItemType baseItemType) {
        name = baseItemType.getName();
        radius = baseItemType.getPhysicalAreaConfig().getRadius();
        fixVerticalNorm = baseItemType.getPhysicalAreaConfig().isFixVerticalNorm();
        angularVelocity = baseItemType.getPhysicalAreaConfig().getAngularVelocity();
        speed = baseItemType.getPhysicalAreaConfig().getSpeed();
        acceleration = baseItemType.getPhysicalAreaConfig().getAcceleration();
        health = baseItemType.getHealth();
        spawnDurationMillis = baseItemType.getSpawnDurationMillis();
        price = baseItemType.getPrice();
        xpOnKilling = baseItemType.getXpOnKilling();
        // TODO i18nName i18nDescription
        dropBoxPossibility = baseItemType.getDropBoxPossibility();
        boxPickupRange = baseItemType.getBoxPickupRange();
        unlockCrystals = baseItemType.getUnlockCrystals();
        explosionParticleConfigId_TMP = baseItemType.getExplosionParticleConfigId();

        if (baseItemType.getWeaponType() != null) {
            if (weaponType == null) {
                weaponType = new WeaponTypeEntity();
            }
            weaponType.fromWeaponType(baseItemType.getWeaponType());
        } else {
            weaponType = null;
        }

        if (baseItemType.getFactoryType() != null) {
            if (factoryType == null) {
                factoryType = new FactoryTypeEntity();
            }
            factoryType.fromFactoryTypeEntity(baseItemType.getFactoryType());
        } else {
            factoryType = null;
        }

        if (baseItemType.getHarvesterType() != null) {
            if (harvesterType == null) {
                harvesterType = new HarvesterTypeEntity();
            }
            harvesterType.fromHarvesterType(baseItemType.getHarvesterType());
        } else {
            harvesterType = null;
        }

        if (baseItemType.getBuilderType() != null) {
            if (builderType == null) {
                builderType = new BuilderTypeEntity();
            }
            builderType.fromBuilderType(baseItemType.getBuilderType());
        } else {
            builderType = null;
        }

        if (baseItemType.getGeneratorType() != null) {
            if (generatorType == null) {
                generatorType = new GeneratorTypeEntity();
            }
            generatorType.fromGeneratorType(baseItemType.getGeneratorType());
        } else {
            generatorType = null;
        }

        if (baseItemType.getConsumerType() != null) {
            if (consumerType == null) {
                consumerType = new ConsumerTypeEntity();
            }
            consumerType.fromConsumerType(baseItemType.getConsumerType());
        } else {
            consumerType = null;
        }

        if (baseItemType.getItemContainerType() != null) {
            if (itemContainerType == null) {
                itemContainerType = new ItemContainerTypeEntity();
            }
            itemContainerType.fromItemContainerType(baseItemType.getItemContainerType());
        } else {
            itemContainerType = null;
        }

        if (baseItemType.getHouseType() != null) {
            if (houseType == null) {
                houseType = new HouseTypeEntity();
            }
            houseType.fromHouseType(baseItemType.getHouseType());
        } else {
            houseType = null;
        }
    }

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
    }

    public void setSpawnShape3DId(ColladaEntity spawnShape3DId) {
        this.spawnShape3DId = spawnShape3DId;
    }

    public void setBuildupTexture(ImageLibraryEntity buildupTextureId) {
        this.buildupTexture = buildupTextureId;
    }

    public void setDemolitionImage(ImageLibraryEntity demolitionImageId) {
        this.demolitionImage = demolitionImageId;
    }

    public void setWreckageShape3DId(ColladaEntity wreckageShape3DId) {
        this.wreckageShape3DId = wreckageShape3DId;
    }

    public void setSpawnAudioId(AudioLibraryEntity spawnAudioId) {
        this.spawnAudioId = spawnAudioId;
    }

    public void setThumbnail(ImageLibraryEntity thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseItemTypeEntity that = (BaseItemTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
