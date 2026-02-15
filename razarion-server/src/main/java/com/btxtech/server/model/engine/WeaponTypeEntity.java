package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.server.service.ui.AudioService;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.AudioItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_WEAPON_TYPE")
public class WeaponTypeEntity extends BaseEntity {
    private double attackRange;
    private int damage;
    private double detonationRadius;
    private double reloadTime;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BASE_ITEM_WEAPON_TYPE_DISALLOWED_ITEM_TYPES",
            joinColumns = @JoinColumn(name = "weapon"),
            inverseJoinColumns = @JoinColumn(name = "baseItemType"))
    private List<BaseItemTypeEntity> disallowedItemTypes;
    private Double projectileSpeed; // Meter per second
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity muzzleFlashAudioLibraryEntity;
    private int muzzleFlashPitchCentsMin = -200;
    private int muzzleFlashPitchCentsMax = 200;
    private double muzzleFlashVolumeMin = 0.8;
    private double muzzleFlashVolumeMax = 1.0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity impactAudioLibraryEntity;
    private int impactPitchCentsMin = -200;
    private int impactPitchCentsMax = 200;
    private double impactVolumeMin = 0.8;
    private double impactVolumeMax = 1.0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity impactParticleSystem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity trailParticleSystem;
    private Double turretAngleVelocity;

    public WeaponType toWeaponType() {
        AudioItemConfig muzzleFlashAudioConfig = null;
        Integer muzzleFlashAudioId = extractId(muzzleFlashAudioLibraryEntity, AudioLibraryEntity::getId);
        if (muzzleFlashAudioId != null) {
            muzzleFlashAudioConfig = new AudioItemConfig()
                    .audioId(muzzleFlashAudioId)
                    .pitchCentsMin(muzzleFlashPitchCentsMin)
                    .pitchCentsMax(muzzleFlashPitchCentsMax)
                    .volumeMin(muzzleFlashVolumeMin)
                    .volumeMax(muzzleFlashVolumeMax);
        }

        AudioItemConfig impactAudioConfig = null;
        Integer impactAudioId = extractId(impactAudioLibraryEntity, AudioLibraryEntity::getId);
        if (impactAudioId != null) {
            impactAudioConfig = new AudioItemConfig()
                    .audioId(impactAudioId)
                    .pitchCentsMin(impactPitchCentsMin)
                    .pitchCentsMax(impactPitchCentsMax)
                    .volumeMin(impactVolumeMin)
                    .volumeMax(impactVolumeMax);
        }

        WeaponType weaponType = new WeaponType()
                .range(attackRange)
                .damage(damage)
                .detonationRadius(detonationRadius)
                .reloadTime(reloadTime)
                .projectileSpeed(projectileSpeed)
                .impactParticleSystemId(extractId(impactParticleSystem, ParticleSystemEntity::getId))
                .muzzleFlashAudioConfig(muzzleFlashAudioConfig)
                .impactAudioConfig(impactAudioConfig)
                .trailParticleSystemConfigId(extractId(trailParticleSystem, ParticleSystemEntity::getId))
                .turretAngleVelocity(turretAngleVelocity);
        if (disallowedItemTypes != null && !disallowedItemTypes.isEmpty()) {
            List<Integer> disallowedIds = new ArrayList<>();
            for (BaseItemTypeEntity baseItemTypeEntity : disallowedItemTypes) {
                disallowedIds.add(baseItemTypeEntity.getId());
            }
            weaponType.disallowedItemTypes(disallowedIds);
        }
        return weaponType;
    }

    public void fromWeaponType(WeaponType weaponType,
                               BaseItemTypeService baseItemTypeCrudPersistence,
                               AudioService audioPersistence,
                               ParticleSystemService particleSystemCrudPersistence) {
        attackRange = weaponType.getRange();
        damage = weaponType.getDamage();
        detonationRadius = weaponType.getDetonationRadius();
        reloadTime = weaponType.getReloadTime();
        if (weaponType.getDisallowedItemTypes() != null) {
            if (disallowedItemTypes == null) {
                disallowedItemTypes = new ArrayList<>();
            }
            disallowedItemTypes.clear();
            for (Integer disallowedId : weaponType.getDisallowedItemTypes()) {
                disallowedItemTypes.add(baseItemTypeCrudPersistence.getEntity(disallowedId));
            }
        } else {
            disallowedItemTypes = null;
        }
        projectileSpeed = weaponType.getProjectileSpeed();
        if (weaponType.getMuzzleFlashAudioConfig() != null) {
            muzzleFlashAudioLibraryEntity = audioPersistence.getAudioLibraryEntity(weaponType.getMuzzleFlashAudioConfig().getAudioId());
            muzzleFlashPitchCentsMin = weaponType.getMuzzleFlashAudioConfig().getPitchCentsMin();
            muzzleFlashPitchCentsMax = weaponType.getMuzzleFlashAudioConfig().getPitchCentsMax();
            muzzleFlashVolumeMin = weaponType.getMuzzleFlashAudioConfig().getVolumeMin();
            muzzleFlashVolumeMax = weaponType.getMuzzleFlashAudioConfig().getVolumeMax();
        } else {
            muzzleFlashAudioLibraryEntity = null;
        }
        if (weaponType.getImpactAudioConfig() != null) {
            impactAudioLibraryEntity = audioPersistence.getAudioLibraryEntity(weaponType.getImpactAudioConfig().getAudioId());
            impactPitchCentsMin = weaponType.getImpactAudioConfig().getPitchCentsMin();
            impactPitchCentsMax = weaponType.getImpactAudioConfig().getPitchCentsMax();
            impactVolumeMin = weaponType.getImpactAudioConfig().getVolumeMin();
            impactVolumeMax = weaponType.getImpactAudioConfig().getVolumeMax();
        } else {
            impactAudioLibraryEntity = null;
        }
        impactParticleSystem = particleSystemCrudPersistence.getEntity(weaponType.getImpactParticleSystemId());
        turretAngleVelocity = weaponType.getTurretAngleVelocity();
        trailParticleSystem = particleSystemCrudPersistence.getEntity(weaponType.getTrailParticleSystemConfigId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WeaponTypeEntity that = (WeaponTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
