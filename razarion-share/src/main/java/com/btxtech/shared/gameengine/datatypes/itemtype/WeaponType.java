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


import com.btxtech.shared.datatypes.Vertex;

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
    private Map<Integer, Double> itemTypeFactors;
    private Double projectileSpeed; // Pixel per second
    private Vertex muzzlePosition;
    private Integer projectileShape3DId;
    private Integer muzzleFlashClipId;
    private Integer detonationClipId;

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

    public Map<Integer, Double> getItemTypeFactors() {
        return itemTypeFactors;
    }

    public WeaponType setItemTypeFactors(Map<Integer, Double> itemTypeFactors) {
        this.itemTypeFactors = itemTypeFactors;
        return this;
    }

    public Double getProjectileSpeed() {
        return projectileSpeed;
    }

    public WeaponType setProjectileSpeed(Double projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
        return this;
    }

    public Vertex getMuzzlePosition() {
        return muzzlePosition;
    }

    public WeaponType setMuzzlePosition(Vertex muzzlePosition) {
        this.muzzlePosition = muzzlePosition;
        return this;
    }

    public Integer getProjectileShape3DId() {
        return projectileShape3DId;
    }

    public WeaponType setProjectileShape3DId(Integer projectileShape3DId) {
        this.projectileShape3DId = projectileShape3DId;
        return this;
    }

    public Integer getMuzzleFlashClipId() {
        return muzzleFlashClipId;
    }

    public WeaponType setMuzzleFlashClipId(Integer muzzleFlashClipId) {
        this.muzzleFlashClipId = muzzleFlashClipId;
        return this;
    }

    public Integer getDetonationClipId() {
        return detonationClipId;
    }

    public WeaponType setDetonationClipId(Integer detonationClipId) {
        this.detonationClipId = detonationClipId;
        return this;
    }
}
