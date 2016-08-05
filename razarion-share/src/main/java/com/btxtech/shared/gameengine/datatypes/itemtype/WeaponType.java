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


import com.btxtech.shared.datatypes.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;
import java.util.Map;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:13:22
 */
@Portable
public class WeaponType {
    private int range;
    private int damage;
    private Integer detonationRadius;
    private double reloadTime;
    private Collection<Integer> disallowedItemTypes;
    private Map<Integer, Double> itemTypeFactors;
    // Pixel per second
    private Integer projectileSpeed;
    // dimension 1: muzzle nr, dimension 2: image nr
    private Index[][] muzzleFlashPositions;
    private Integer muzzleFlashClipId;
    private Integer projectileClipId;
    private Integer projectileDetonationClipId;

    /**
     * Used by GWT
     */
    public WeaponType() {
    }

    public WeaponType(int range, Integer projectileSpeed, int damage, Integer detonationRadius, double reloadTime, Integer muzzleFlashClipId, Integer projectileClipId, Integer projectileDetonationClipId, Collection<Integer> disallowedItemTypes, Map<Integer, Double> itemTypeFactors, Index[][] muzzleFlashPositions) {
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.damage = damage;
        this.detonationRadius = detonationRadius;
        this.reloadTime = reloadTime;
        this.muzzleFlashClipId = muzzleFlashClipId;
        this.projectileClipId = projectileClipId;
        this.projectileDetonationClipId = projectileDetonationClipId;
        this.disallowedItemTypes = disallowedItemTypes;
        this.itemTypeFactors = itemTypeFactors;
        this.muzzleFlashPositions = muzzleFlashPositions;
    }

    public void changeTo(WeaponType weaponType) {
        range = weaponType.range;
        projectileSpeed = weaponType.projectileSpeed;
        damage = weaponType.damage;
        detonationRadius = weaponType.detonationRadius;
        reloadTime = weaponType.reloadTime;
        muzzleFlashClipId = weaponType.muzzleFlashClipId;
        projectileClipId = weaponType.projectileClipId;
        projectileDetonationClipId = weaponType.projectileDetonationClipId;
        disallowedItemTypes = weaponType.disallowedItemTypes;
        itemTypeFactors = weaponType.itemTypeFactors;
        muzzleFlashPositions = weaponType.muzzleFlashPositions;
    }

    public int getRange() {
        return range;
    }

    public Integer getProjectileSpeed() {
        return projectileSpeed;
    }

    public double getDamage(BaseItemType baseItemType) {
        Double factor = itemTypeFactors.get(baseItemType.getId());
        if (factor != null) {
            return damage * factor;
        } else {
            return damage;
        }
    }

    public boolean hasDetonationRadius() {
        return detonationRadius != null;
    }

    public Integer getDetonationRadius() {
        return detonationRadius;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public Integer getMuzzleFlashClipId() {
        return muzzleFlashClipId;
    }

    public Integer getProjectileClipId() {
        return projectileClipId;
    }

    public Integer getProjectileDetonationClipId() {
        return projectileDetonationClipId;
    }

    public boolean isItemTypeDisallowed(int itemTypeId) {
        return disallowedItemTypes.contains(itemTypeId);
    }

    public Index getMuzzleFlashPosition(int muzzleNr, int angelIndex) {
        return muzzleFlashPositions[muzzleNr][angelIndex];
    }

    public void setMuzzleFlashPosition(int muzzleNr, int angelIndex, Index position) {
        muzzleFlashPositions[muzzleNr][angelIndex] = position;
    }

    public int getMuzzleFlashCount() {
        return muzzleFlashPositions.length;
    }

    public Index[][] getMuzzleFlashPositions() {
        return muzzleFlashPositions;
    }

    public void changeMuzzleFlashCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Item must have at least one muzzle flash");
        }
        Index[][] saveMuzzleFlashPositions = muzzleFlashPositions;
        int imageCount = muzzleFlashPositions[0].length;
        muzzleFlashPositions = new Index[count][];
        for (int muzzleFireNr = 0; muzzleFireNr < count; muzzleFireNr++) {
            muzzleFlashPositions[muzzleFireNr] = new Index[imageCount];
            for (int imageNr = 0; imageNr < imageCount; imageNr++) {
                if (muzzleFireNr < saveMuzzleFlashPositions.length) {
                    muzzleFlashPositions[muzzleFireNr][imageNr] = saveMuzzleFlashPositions[muzzleFireNr][imageNr];
                } else {
                    muzzleFlashPositions[muzzleFireNr][imageNr] = new Index(0, 0);
                }
            }
        }
    }
}
