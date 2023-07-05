/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

import java.util.List;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:13:22
 */
@JsType
public class WeaponType {
    private double range;
    private int damage;
    private double detonationRadius;
    private double reloadTime;
    private List<Integer> disallowedItemTypes;
    private Double projectileSpeed; // Meter per second
    private Integer projectileShape3DId;
    @CollectionReference(CollectionReferenceType.PARTICLE_SYSTEM)
    private Integer muzzleFlashParticleSystemConfigId;
    private Integer detonationParticleConfigId;
    private TurretType turretType;

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public double getDetonationRadius() {
        return detonationRadius;
    }

    public void setDetonationRadius(double detonationRadius) {
        this.detonationRadius = detonationRadius;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(double reloadTime) {
        this.reloadTime = reloadTime;
    }

    public List<Integer> getDisallowedItemTypes() {
        return disallowedItemTypes;
    }

    public void setDisallowedItemTypes(List<Integer> disallowedItemTypes) {
        this.disallowedItemTypes = disallowedItemTypes;
    }

    public Double getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(Double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public Integer getProjectileShape3DId() {
        return projectileShape3DId;
    }

    public void setProjectileShape3DId(Integer projectileShape3DId) {
        this.projectileShape3DId = projectileShape3DId;
    }

    public Integer getMuzzleFlashParticleSystemConfigId() {
        return muzzleFlashParticleSystemConfigId;
    }

    public void setMuzzleFlashParticleSystemConfigId(Integer muzzleFlashParticleSystemConfigId) {
        this.muzzleFlashParticleSystemConfigId = muzzleFlashParticleSystemConfigId;
    }

    public Integer getDetonationParticleConfigId() {
        return detonationParticleConfigId;
    }

    public void setDetonationParticleConfigId(Integer detonationParticleConfigId) {
        this.detonationParticleConfigId = detonationParticleConfigId;
    }

    public TurretType getTurretType() {
        return turretType;
    }

    public void setTurretType(TurretType turretType) {
        this.turretType = turretType;
    }

    public WeaponType range(double range) {
        setRange(range);
        return this;
    }

    public WeaponType damage(int damage) {
        setDamage(damage);
        return this;
    }

    public WeaponType detonationRadius(double detonationRadius) {
        setDetonationRadius(detonationRadius);
        return this;
    }

    public WeaponType reloadTime(double reloadTime) {
        setReloadTime(reloadTime);
        return this;
    }

    public WeaponType disallowedItemTypes(List<Integer> disallowedItemTypes) {
        setDisallowedItemTypes(disallowedItemTypes);
        return this;
    }

    public WeaponType projectileSpeed(Double projectileSpeed) {
        setProjectileSpeed(projectileSpeed);
        return this;
    }

    public WeaponType projectileShape3DId(Integer projectileShape3DId) {
        setProjectileShape3DId(projectileShape3DId);
        return this;
    }

    public WeaponType muzzleFlashParticleSystemConfigId(Integer muzzleFlashParticleConfigId) {
        setMuzzleFlashParticleSystemConfigId(muzzleFlashParticleConfigId);
        return this;
    }

    public WeaponType detonationParticleConfigId(Integer detonationParticleConfigId) {
        setDetonationParticleConfigId(detonationParticleConfigId);
        return this;
    }

    public WeaponType turretType(TurretType turretType) {
        setTurretType(turretType);
        return this;
    }

    public boolean checkItemTypeDisallowed(int itemTypeId) {
        return disallowedItemTypes != null && disallowedItemTypes.contains(itemTypeId);
    }

}
