package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.server.model.ui.Model3DEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.engine.AudioPersistence;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.engine.BoxItemTypeCrudPersistence;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;

@Entity
@Table(name = "BASE_ITEM_TYPE")
public class BaseItemTypeEntity extends BaseEntity {
    private double radius;
    private boolean fixVerticalNorm;
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;
    private Double angularVelocity; //Rad per second
    private Double speed;
    private Double acceleration;
    private Double startAngleSlowDown;
    private Double endAngleSlowDown;
    private int health;
    private int price;
    private int buildup;
    private int xpOnKilling;
    private int consumingHouseSpace;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity thumbnail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Model3DEntity model3DEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity spawnAudio;
    private int spawnDurationMillis;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BoxItemTypeEntity dropBoxItemTypeEntity;
    private double dropBoxPossibility;
    private double boxPickupRange;
    private Integer unlockCrystals;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity buildupTexture;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity demolitionImage;
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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private SpecialTypeEntity specialType;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "baseItemType", nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<DemolitionStepEffectEntity> demolitionStepEffectEntities;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity explosionAudioLibraryEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity explosionParticleSystem;

    

    public BaseItemType toBaseItemType() {
        BaseItemType baseItemType = new BaseItemType()
                .price(price)
                .xpOnKilling(xpOnKilling)
                .consumingHouseSpace(consumingHouseSpace)
                .dropBoxPossibility(dropBoxPossibility)
                .explosionAudioItemConfigId(extractId(explosionAudioLibraryEntity, AudioLibraryEntity::getId))
                .explosionParticleId(extractId(explosionParticleSystem, ParticleSystemEntity::getId));
        if (dropBoxItemTypeEntity != null) {
            baseItemType.setDropBoxItemTypeId(dropBoxItemTypeEntity.getId());
        }
        baseItemType.boxPickupRange(boxPickupRange).unlockCrystals(unlockCrystals).health(health).buildup(buildup);
        baseItemType.id(getId()).internalName(getInternalName());
        baseItemType.setPhysicalAreaConfig(new PhysicalAreaConfig()
                .radius(radius)
                .fixVerticalNorm(fixVerticalNorm)
                .terrainType(terrainType)
                .speed(speed)
                .acceleration(acceleration)
                .angularVelocity(angularVelocity)
                .startAngleSlowDown(startAngleSlowDown)
                .endAngleSlowDown(endAngleSlowDown));
        if (model3DEntity != null) {
            baseItemType.setModel3DId(model3DEntity.getId());
        }
        if (spawnAudio != null) {
            baseItemType.setSpawnAudioId(spawnAudio.getId());
        }
        baseItemType.setSpawnDurationMillis(spawnDurationMillis);
        if (thumbnail != null) {
            baseItemType.setThumbnail(thumbnail.getId());
        }
        if (demolitionImage != null) {
            baseItemType.setDemolitionImageId(demolitionImage.getId());
        }
        if (buildupTexture != null) {
            baseItemType.setBuildupTextureId(buildupTexture.getId());
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
        if (specialType != null) {
            baseItemType.setSpecialType(specialType.toSpecialType());
        }
        if (demolitionStepEffectEntities != null && !demolitionStepEffectEntities.isEmpty()) {
            List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
            for (DemolitionStepEffectEntity stepEffectEntity : demolitionStepEffectEntities) {
                demolitionStepEffects.add(stepEffectEntity.toDemolitionStepEffect());
            }
            baseItemType.setDemolitionStepEffects(demolitionStepEffects);
        }
        return baseItemType;
    }

    public void fromBaseItemType(BaseItemType baseItemType, 
                                 BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, 
                                 BoxItemTypeCrudPersistence boxItemTypeCrudPersistence, 
                                 AudioPersistence audioPersistence,
                                 ParticleSystemService particleSystemCrudPersistence) {
        radius = baseItemType.getPhysicalAreaConfig().getRadius();
        fixVerticalNorm = baseItemType.getPhysicalAreaConfig().isFixVerticalNorm();
        terrainType = baseItemType.getPhysicalAreaConfig().getTerrainType();
        angularVelocity = baseItemType.getPhysicalAreaConfig().getAngularVelocity();
        speed = baseItemType.getPhysicalAreaConfig().getSpeed();
        acceleration = baseItemType.getPhysicalAreaConfig().getAcceleration();
        startAngleSlowDown = baseItemType.getPhysicalAreaConfig().getStartAngleSlowDown();
        endAngleSlowDown = baseItemType.getPhysicalAreaConfig().getEndAngleSlowDown();
        health = baseItemType.getHealth();
        buildup = baseItemType.getBuildup();
        spawnDurationMillis = baseItemType.getSpawnDurationMillis();
        price = baseItemType.getPrice();
        xpOnKilling = baseItemType.getXpOnKilling();
        consumingHouseSpace = baseItemType.getConsumingHouseSpace();
        dropBoxItemTypeEntity = boxItemTypeCrudPersistence.getEntity(baseItemType.getDropBoxItemTypeId());
        dropBoxPossibility = baseItemType.getDropBoxPossibility();
        boxPickupRange = baseItemType.getBoxPickupRange();
        unlockCrystals = baseItemType.getUnlockCrystals();
        explosionAudioLibraryEntity = audioPersistence.getAudioLibraryEntity(baseItemType.getExplosionAudioItemConfigId());

        if (baseItemType.getWeaponType() != null) {
            if (weaponType == null) {
                weaponType = new WeaponTypeEntity();
            }
            weaponType.fromWeaponType(baseItemType.getWeaponType(), baseItemTypeCrudPersistence, audioPersistence, particleSystemCrudPersistence);
        } else {
            weaponType = null;
        }

        if (baseItemType.getFactoryType() != null) {
            if (factoryType == null) {
                factoryType = new FactoryTypeEntity();
            }
            factoryType.fromFactoryTypeEntity(baseItemType.getFactoryType(), baseItemTypeCrudPersistence);
        } else {
            factoryType = null;
        }

        if (baseItemType.getHarvesterType() != null) {
            if (harvesterType == null) {
                harvesterType = new HarvesterTypeEntity();
            }
            harvesterType.fromHarvesterType(baseItemType.getHarvesterType(), particleSystemCrudPersistence);
        } else {
            harvesterType = null;
        }

        if (baseItemType.getBuilderType() != null) {
            if (builderType == null) {
                builderType = new BuilderTypeEntity();
            }
            builderType.fromBuilderType(baseItemType.getBuilderType(), baseItemTypeCrudPersistence, particleSystemCrudPersistence);
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
            itemContainerType.fromItemContainerType(baseItemType.getItemContainerType(), baseItemTypeCrudPersistence);
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
        if (baseItemType.getSpecialType() != null) {
            if (specialType == null) {
                specialType = new SpecialTypeEntity();
            }
            specialType.fromSpecialType(baseItemType.getSpecialType());
        } else {
            specialType = null;
        }
        if (demolitionStepEffectEntities == null) {
            demolitionStepEffectEntities = new ArrayList<>();
        }
        demolitionStepEffectEntities.clear();
        explosionParticleSystem = particleSystemCrudPersistence.getEntity(baseItemType.getExplosionParticleId());
    }

    public void setModel3DEntity(Model3DEntity model3DEntity) {
        this.model3DEntity = model3DEntity;
    }

    public void setBuildupTexture(ImageLibraryEntity buildupTexture) {
        this.buildupTexture = buildupTexture;
    }

    public void setDemolitionImage(ImageLibraryEntity demolitionImage) {
        this.demolitionImage = demolitionImage;
    }

    public void setSpawnAudio(AudioLibraryEntity spawnAudio) {
        this.spawnAudio = spawnAudio;
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
