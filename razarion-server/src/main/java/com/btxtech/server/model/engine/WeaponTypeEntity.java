package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.engine.AudioPersistence;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import jakarta.persistence.*;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity muzzleFlashParticleSystem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity trailParticleSystem;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TurretTypeEntity turretType;

    public WeaponType toWeaponType() {
        WeaponType weaponType = new WeaponType()
                .range(attackRange)
                .damage(damage)
                .detonationRadius(detonationRadius)
                .reloadTime(reloadTime)
                .projectileSpeed(projectileSpeed)
                .muzzleFlashParticleSystemConfigId(extractId(muzzleFlashParticleSystem, ParticleSystemEntity::getId))
                .muzzleFlashAudioItemConfigId(extractId(muzzleFlashAudioLibraryEntity, AudioLibraryEntity::getId))
                .trailParticleSystemConfigId(extractId(trailParticleSystem, ParticleSystemEntity::getId));
        if (disallowedItemTypes != null && !disallowedItemTypes.isEmpty()) {
            List<Integer> disallowedIds = new ArrayList<>();
            for (BaseItemTypeEntity baseItemTypeEntity : disallowedItemTypes) {
                disallowedIds.add(baseItemTypeEntity.getId());
            }
            weaponType.disallowedItemTypes(disallowedIds);
        }
        if (turretType != null) {
            weaponType.turretType(turretType.toTurretType());
        }
        return weaponType;
    }

    public void fromWeaponType(WeaponType weaponType,
                               BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                               AudioPersistence audioPersistence,
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
        muzzleFlashAudioLibraryEntity = audioPersistence.getAudioLibraryEntity(weaponType.getMuzzleFlashAudioItemConfigId());
        muzzleFlashParticleSystem = particleSystemCrudPersistence.getEntity(weaponType.getMuzzleFlashParticleSystemConfigId());
        if (weaponType.getTurretType() != null) {
            if (turretType == null) {
                turretType = new TurretTypeEntity();
            }
            turretType.fromTurretType(weaponType.getTurretType());
        } else {
            turretType = null;
        }
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
