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


import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:13:22
 */
@JsType
@JsonPersistable
public class WeaponType {
    private double range;
    private int damage;
    private double detonationRadius;
    private double reloadTime;
    private List<Integer> disallowedItemTypes;
    private Double projectileSpeed; // Meter per second
    private Integer impactParticleSystemId;
    private Double turretAngleVelocity;
    private Integer muzzleFlashAudioItemConfigId;
    private Integer trailParticleSystemConfigId;

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

    public @Nullable Double getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(@Nullable Double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public Integer getMuzzleFlashAudioItemConfigId() {
        return muzzleFlashAudioItemConfigId;
    }

    public void setMuzzleFlashAudioItemConfigId(Integer muzzleFlashAudioItemConfigId) {
        this.muzzleFlashAudioItemConfigId = muzzleFlashAudioItemConfigId;
    }

    public @Nullable Integer getImpactParticleSystemId() {
        return impactParticleSystemId;
    }

    public void setImpactParticleSystemId(@Nullable Integer impactParticleSystemId) {
        this.impactParticleSystemId = impactParticleSystemId;
    }

    public @Nullable Integer getTrailParticleSystemConfigId() {
        return trailParticleSystemConfigId;
    }

    public void setTrailParticleSystemConfigId(@Nullable Integer trailParticleSystemConfigId) {
        this.trailParticleSystemConfigId = trailParticleSystemConfigId;
    }

    public Double getTurretAngleVelocity() {
        return turretAngleVelocity;
    }

    public void setTurretAngleVelocity(Double turretAngleVelocity) {
        this.turretAngleVelocity = turretAngleVelocity;
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

    public WeaponType muzzleFlashAudioItemConfigId(Integer muzzleFlashAudioItemConfigId) {
        setMuzzleFlashAudioItemConfigId(muzzleFlashAudioItemConfigId);
        return this;
    }

    public WeaponType impactParticleSystemId(Integer muzzleFlashParticleConfigId) {
        setImpactParticleSystemId(muzzleFlashParticleConfigId);
        return this;
    }

    public WeaponType turretAngleVelocity(Double turretAngleVelocity) {
        setTurretAngleVelocity(turretAngleVelocity);
        return this;
    }

    public WeaponType trailParticleSystemConfigId(Integer trailParticleSystemConfigId) {
        setTrailParticleSystemConfigId(trailParticleSystemConfigId);
        return this;
    }

    public boolean checkItemTypeDisallowed(int itemTypeId) {
        return disallowedItemTypes != null && disallowedItemTypes.contains(itemTypeId);
    }

}
