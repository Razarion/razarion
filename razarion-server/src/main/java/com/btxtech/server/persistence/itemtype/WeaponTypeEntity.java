package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.ParticleSystemCrudPersistence;
import com.btxtech.server.persistence.ParticleSystemEntity;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_WEAPON_TYPE")
public class WeaponTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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
    private ColladaEntity projectileShape3D;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity muzzleFlashParticleSystem;
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
                .projectileShape3DId(extractId(projectileShape3D, ColladaEntity::getId));
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

    public void fromWeaponType(WeaponType weaponType, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, Shape3DCrudPersistence shape3DPersistence, ParticleSystemCrudPersistence particleSystemCrudPersistence) {
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
        projectileShape3D = shape3DPersistence.getEntity(weaponType.getProjectileShape3DId());
        muzzleFlashParticleSystem = particleSystemCrudPersistence.getEntity(weaponType.getMuzzleFlashParticleSystemConfigId());
        if (weaponType.getTurretType() != null) {
            if (turretType == null) {
                turretType = new TurretTypeEntity();
            }
            turretType.fromTurretType(weaponType.getTurretType());
        } else {
            turretType = null;
        }

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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
