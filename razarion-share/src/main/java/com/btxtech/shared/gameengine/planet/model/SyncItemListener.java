package com.btxtech.shared.gameengine.planet.model;

/**
 * User: Razarion contributors
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
