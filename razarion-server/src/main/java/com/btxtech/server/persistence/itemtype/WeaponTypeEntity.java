package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
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
    private Integer muzzleFlashParticleConfigId_TMP;
    private Integer detonationParticleConfigId_TMP;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TurretTypeEntity turretType;

    public WeaponType toWeaponType() {
        WeaponType weaponType = new WeaponType().setRange(attackRange).setDamage(damage).setDetonationRadius(detonationRadius).setReloadTime(reloadTime);
        weaponType.setProjectileSpeed(projectileSpeed).setMuzzleFlashParticleConfigId(muzzleFlashParticleConfigId_TMP).setDetonationParticleConfigId(detonationParticleConfigId_TMP);
        if (disallowedItemTypes != null && !disallowedItemTypes.isEmpty()) {
            List<Integer> disallowedIds = new ArrayList<>();
            for (BaseItemTypeEntity baseItemTypeEntity : disallowedItemTypes) {
                disallowedIds.add(baseItemTypeEntity.getId());
            }
            weaponType.setDisallowedItemTypes(disallowedIds);
        }
        if (projectileShape3D != null) {
            weaponType.setProjectileShape3DId(projectileShape3D.getId());
        }
        if (turretType != null) {
            weaponType.setTurretType(turretType.toTurretType());
        }
        return weaponType;
    }

    public void fromWeaponType(WeaponType weaponType, ItemTypePersistence itemTypePersistence, Shape3DCrudPersistence shape3DPersistence) {
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
                disallowedItemTypes.add(itemTypePersistence.readBaseItemTypeEntity(disallowedId));
            }
        } else {
            disallowedItemTypes = null;
        }
        projectileSpeed = weaponType.getProjectileSpeed();
        projectileShape3D = shape3DPersistence.getEntity(weaponType.getProjectileShape3DId());
        muzzleFlashParticleConfigId_TMP = weaponType.getMuzzleFlashParticleConfigId();
        detonationParticleConfigId_TMP = weaponType.getDetonationParticleConfigId();
        if(weaponType.getTurretType() != null) {
            if(turretType == null) {
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
