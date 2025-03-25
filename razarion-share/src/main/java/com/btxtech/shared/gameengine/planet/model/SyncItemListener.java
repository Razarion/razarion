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

package com.btxtech.shared.gameengine.planet.model;

/**
 * User: beat
 * Date: 22.11.2009
 * Time: 22:21:39
 */
public interface SyncItemListener {
    enum Change {
        ANGEL,
        POSITION,
        PROJECTILE_LAUNCHED,
        HEALTH,
        FACTORY_PROGRESS,
        RESOURCE,
        BUILD,
        ITEM_TYPE_CHANGED,
        UPGRADE_PROGRESS_CHANGED,
        CONTAINED_IN_CHANGED,
        CONTAINER_COUNT_CHANGED,
        LAUNCHER_PROGRESS,
        PROJECTILE_DETONATION,
        UNDER_ATTACK
    }

    void onItemChanged(Change change, SyncItem syncItem, Object additionalCustomInfo);
}
