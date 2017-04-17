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


import java.util.Collection;
import java.util.Map;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:13:22
 */
public class WeaponType {
    private double range;
    private int damage;
    private double detonationRadius;
    private double reloadTime;
    private Collection<Integer> disallowedItemTypes;
    private Double projectileSpeed; // Pixel per second
    private Integer projectileShape3DId;
    private Integer muzzleFlashParticleEmitterSequenceConfigId;
    private Integer detonationParticleEmitterSequenceConfigId;
    private TurretType turretType;

    public double getRange() {
        return range;
    }

    public WeaponType setRange(double range) {
        this.range = range;
        return this;
    }

    public int getDamage() {
        return damage;
    }

    public WeaponType setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public double getDetonationRadius() {
        return detonationRadius;
    }

    public WeaponType setDetonationRadius(double detonationRadius) {
        this.detonationRadius = detonationRadius;
        return this;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public WeaponType setReloadTime(double reloadTime) {
        this.reloadTime = reloadTime;
        return this;
    }

    public Collection<Integer> getDisallowedItemTypes() {
        return disallowedItemTypes;
    }

    public WeaponType setDisallowedItemTypes(Collection<Integer> disallowedItemTypes) {
        this.disallowedItemTypes = disallowedItemTypes;
        return this;
    }

    public boolean checkItemTypeDisallowed(int itemTypeId) {
        return disallowedItemTypes != null && disallowedItemTypes.contains(itemTypeId);
    }

    public Double getProjectileSpeed() {
        return projectileSpeed;
    }

    public WeaponType setProjectileSpeed(Double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
        return this;
    }

    public Integer getProjectileShape3DId() {
        return projectileShape3DId;
    }

    public WeaponType setProjectileShape3DId(Integer projectileShape3DId) {
        this.projectileShape3DId = projectileShape3DId;
        return this;
    }

    public Integer getMuzzleFlashParticleEmitterSequenceConfigId() {
        return muzzleFlashParticleEmitterSequenceConfigId;
    }

    public WeaponType setMuzzleFlashParticleEmitterSequenceConfigId(Integer muzzleFlashParticleEmitterSequenceConfigId) {
        this.muzzleFlashParticleEmitterSequenceConfigId = muzzleFlashParticleEmitterSequenceConfigId;
        return this;
    }

    public Integer getDetonationParticleEmitterSequenceConfigId() {
        return detonationParticleEmitterSequenceConfigId;
    }

    public WeaponType setDetonationParticleEmitterSequenceConfigId(Integer detonationParticleEmitterSequenceConfigId) {
        this.detonationParticleEmitterSequenceConfigId = detonationParticleEmitterSequenceConfigId;
        return this;
    }

    public TurretType getTurretType() {
        return turretType;
    }

    public WeaponType setTurretType(TurretType turretType) {
        this.turretType = turretType;
        return this;
    }
}
