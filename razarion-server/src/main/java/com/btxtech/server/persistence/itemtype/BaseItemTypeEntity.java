package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.AudioLibraryEntity;
import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.I18nBundleEntity;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.server.persistence.asset.MeshContainerEntity;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceCrudPersistence;
import com.btxtech.server.persistence.particle.ParticleEmitterSequenceEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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
    private String internalName;
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
    private MeshContainerEntity meshContainer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity spawnShape3DId;
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
    private ParticleEmitterSequenceEntity explosionParticle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity buildupTexture;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity demolitionImage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity wreckageShape3D;
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

    public Integer getId() {
        return id;
    }

    public BaseItemType toBaseItemType() {
        BaseItemType baseItemType = new BaseItemType().setPrice(price).setXpOnKilling(xpOnKilling).setDropBoxPossibility(dropBoxPossibility);
        if (dropBoxItemTypeEntity != null) {
            baseItemType.setDropBoxItemTypeId(dropBoxItemTypeEntity.getId());
        }
        baseItemType.setBoxPickupRange(boxPickupRange).setUnlockCrystals(unlockCrystals).setHealth(health).setBuildup(buildup);
        baseItemType.setId(id).setInternalName(internalName);
        if (i18nName != null) {
            baseItemType.setI18nName(i18nName.toI18nString());
        }
        if (i18nDescription != null) {
            baseItemType.setI18nDescription(i18nDescription.toI18nString());
        }
        baseItemType.setPhysicalAreaConfig(new PhysicalAreaConfig()
                .radius(radius)
                .fixVerticalNorm(fixVerticalNorm)
                .terrainType(terrainType)
                .speed(speed)
                .acceleration(acceleration)
                .angularVelocity(angularVelocity)
                .startAngleSlowDown(startAngleSlowDown)
                .endAngleSlowDown(endAngleSlowDown));
        if (shape3DId != null) {
            baseItemType.setShape3DId(shape3DId.getId());
        }
        if (spawnShape3DId != null) {
            baseItemType.setSpawnShape3DId(spawnShape3DId.getId());
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
        if (wreckageShape3D != null) {
            baseItemType.setWreckageShape3DId(wreckageShape3D.getId());
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
        if (explosionParticle != null) {
            baseItemType.setExplosionParticleConfigId(explosionParticle.getId());
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

    public void fromBaseItemType(BaseItemType baseItemType, ItemTypePersistence itemTypePersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, Shape3DCrudPersistence shape3DPersistence, ParticleEmitterSequenceCrudPersistence particleEmitterSequenceCrudPersistence) {
        internalName = baseItemType.getInternalName();
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
        i18nName = I18nBundleEntity.fromI18nStringSafe(baseItemType.getI18nName(), i18nName);
        i18nDescription = I18nBundleEntity.fromI18nStringSafe(baseItemType.getI18nDescription(), i18nDescription);
        dropBoxItemTypeEntity = itemTypePersistence.readBoxItemTypeEntity(baseItemType.getDropBoxItemTypeId());
        dropBoxPossibility = baseItemType.getDropBoxPossibility();
        boxPickupRange = baseItemType.getBoxPickupRange();
        unlockCrystals = baseItemType.getUnlockCrystals();
        explosionParticle = particleEmitterSequenceCrudPersistence.getEntity(baseItemType.getExplosionParticleConfigId());

        if (baseItemType.getWeaponType() != null) {
            if (weaponType == null) {
                weaponType = new WeaponTypeEntity();
            }
            weaponType.fromWeaponType(baseItemType.getWeaponType(), baseItemTypeCrudPersistence, shape3DPersistence, particleEmitterSequenceCrudPersistence);
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
            harvesterType.fromHarvesterType(baseItemType.getHarvesterType(), shape3DPersistence);
        } else {
            harvesterType = null;
        }

        if (baseItemType.getBuilderType() != null) {
            if (builderType == null) {
                builderType = new BuilderTypeEntity();
            }
            builderType.fromBuilderType(baseItemType.getBuilderType(), baseItemTypeCrudPersistence, shape3DPersistence, particleEmitterSequenceCrudPersistence);
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
        if (baseItemType.getDemolitionStepEffects() != null) {
            for (DemolitionStepEffect demolitionStepEffect : baseItemType.getDemolitionStepEffects()) {
                DemolitionStepEffectEntity demolitionStepEffectEntity = new DemolitionStepEffectEntity();
                demolitionStepEffectEntity.fromDemolitionStepEffect(demolitionStepEffect, particleEmitterSequenceCrudPersistence);
                demolitionStepEffectEntities.add(demolitionStepEffectEntity);
            }
        }
    }

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
    }

    public void setMeshContainer(MeshContainerEntity meshContainer) {
        this.meshContainer = meshContainer;
    }

    public void setSpawnShape3DId(ColladaEntity spawnShape3DId) {
        this.spawnShape3DId = spawnShape3DId;
    }

    public void setBuildupTexture(ImageLibraryEntity buildupTexture) {
        this.buildupTexture = buildupTexture;
    }

    public void setDemolitionImage(ImageLibraryEntity demolitionImage) {
        this.demolitionImage = demolitionImage;
    }

    public void setWreckageShape3D(ColladaEntity wreckageShape3D) {
        this.wreckageShape3D = wreckageShape3D;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
