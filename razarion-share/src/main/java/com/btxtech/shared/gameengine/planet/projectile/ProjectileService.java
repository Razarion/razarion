package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 15.07.2016.
 */
@ApplicationScoped
public class ProjectileService {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ActivityService activityService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private final List<Projectile> projectiles = new ArrayList<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent ignore) {
        projectiles.clear();
    }

    public void fireProjectile(long timeStamp, SyncBaseItem actor, SyncBaseItem target) {
        WeaponType weaponType = actor.getSyncWeapon().getWeaponType();
        if (weaponType.getMuzzlePosition() == null) {
            throw new IllegalArgumentException("No MuzzlePosition configured for BaseItemType: " + actor);
        }
        Vertex muzzle = actor.createModelMatrices().getModel().multiply(weaponType.getMuzzlePosition(), 1.0);
        if (weaponType.getProjectileSpeed() == null) {
            // projectileDetonation(projectileGroup);
            throw new UnsupportedOperationException();
        }
        Projectile projectile = new Projectile(timeStamp, actor, muzzle, target.getSyncPhysicalArea().getPosition());
        synchronized (projectiles) {
            projectiles.add(projectile);
        }

        activityService.onProjectileFired(actor, muzzle, target.getSyncPhysicalArea().getPosition(), weaponType.getDetonationClipId(), timeStamp);
    }

    public void tick(long timeStamp) {
        Collection<Projectile> detonationProjectiles = new ArrayList<>();
        for (Iterator<Projectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
            Projectile projectile = iterator.next();
            if (projectile.isTargetReached(timeStamp)) {
                detonationProjectiles.add(projectile);
                iterator.remove();
            }
        }

        detonationProjectiles.forEach(projectile -> projectileDetonation(projectile, timeStamp));
    }

    public List<Projectile> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    private void projectileDetonation(Projectile detonationProjectile, long timeStamp) {
        WeaponType weaponType = detonationProjectile.getActor().getSyncWeapon().getWeaponType();
        activityService.onProjectileDetonation(detonationProjectile.getActor(), detonationProjectile.getTarget(), weaponType.getDetonationClipId(), timeStamp);
        Collection<SyncBaseItem> possibleTargets = syncItemContainerService.findBaseItemInRect(Rectangle2D.generateRectangleFromMiddlePoint(detonationProjectile.getTarget().toXY(), weaponType.getRange(), weaponType.getRange()));
        for (SyncBaseItem target : possibleTargets) {
            if (!target.getSyncPhysicalArea().overlap(detonationProjectile.getTarget().toXY(), weaponType.getRange())) {
                continue;
            }
            if (!baseItemService.isEnemy(detonationProjectile.getActor(), target)) {
                continue;
            }
            target.onAttacked(weaponType.getDamage(), detonationProjectile.getActor());
        }
    }
}
