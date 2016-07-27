package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.ActiveProjectileGroup;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncBaseItem;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Dependent
public class ActiveProjectileContainer {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ActivityService activityService;
    private SyncBaseItem syncBaseItem;
    private WeaponType weaponType;
    private List<ActiveProjectileGroup> projectiles = new ArrayList<ActiveProjectileGroup>();
    private Index projectileTarget;
    private SyncBaseItem target;

    public void init(WeaponType weaponType, SyncBaseItem syncBaseItem) {
        this.syncBaseItem = syncBaseItem;
        this.weaponType = weaponType;
    }

    public void createProjectile(SyncBaseItem target) {
        this.target = target;
        projectileTarget = target.getSyncItemArea().getPosition();
        ActiveProjectileGroup projectileGroup = new ActiveProjectileGroup(syncBaseItem, weaponType, projectileTarget);
        projectiles.add(projectileGroup);
        if (weaponType.getProjectileSpeed() == null) {
            projectileDetonation(projectileGroup);
        }
    }

    public boolean tick() {
        Collection<ActiveProjectileGroup> detonation = new ArrayList<ActiveProjectileGroup>();
        for (ActiveProjectileGroup projectileGroup : projectiles) {
            projectileGroup.tick(weaponType);
            if (!projectileGroup.isAlive()) {
                detonation.add(projectileGroup);
            }
        }

        for (ActiveProjectileGroup projectileGroup : detonation) {
            projectileDetonation(projectileGroup);
        }

        return !projectiles.isEmpty();
    }

    public void clear() {
        for (ActiveProjectileGroup projectile : projectiles) {
            projectile.clearActive();
        }
        projectiles.clear();
        projectileTarget = null;
    }

    private void projectileDetonation(ActiveProjectileGroup projectileGroup) {
        activityService.onProjectileDetonation(syncBaseItem);
        if (target != null && target.getSyncItemArea().hasPosition()) {
            if (weaponType.hasDetonationRadius()) {
                Collection<SyncBaseItem> targetItems = baseItemService.getBaseItemsInRadius(target.getSyncItemArea().getPosition(), weaponType.getDetonationRadius(), null, null);
                for (SyncBaseItem baseItem : targetItems) {
                    attackTarget(baseItem);
                }
            } else {
                attackTarget(target);
            }
        }

        projectiles.remove(projectileGroup);
        if (projectiles.isEmpty()) {
            projectileTarget = null;
        }
    }

    private void attackTarget(SyncBaseItem targetItem) {
        if (targetItem.isAlive()) {
            targetItem.decreaseHealth(weaponType.getDamage(targetItem.getBaseItemType()), syncBaseItem.getBase());
            try {
                targetItem.onAttacked(syncBaseItem);
            } catch (TargetHasNoPositionException e) {
                // Ignore
            }
        }
    }

    public Index getProjectileTarget() {
        return projectileTarget;
    }

}
