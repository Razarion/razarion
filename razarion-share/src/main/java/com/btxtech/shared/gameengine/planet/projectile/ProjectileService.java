package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ProjectileService {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Collection<Projectile> projectiles = new ArrayList<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent ignore) {
        synchronized (projectiles) {
            projectiles.clear();
        }
    }

    public void fireProjectile(SyncBaseItem actor, SyncBaseItem target) {
        WeaponType weaponType = actor.getSyncWeapon().getWeaponType();

        Vertex muzzle = actor.getSyncWeapon().createTurretMatrix().multiply(weaponType.getTurretType().getMuzzlePosition(), 1.0);
        if (weaponType.getProjectileSpeed() == null) {
            // projectileDetonation(projectileGroup);
            throw new UnsupportedOperationException();
        }
        Projectile projectile = new Projectile(actor, muzzle, target.getSyncPhysicalArea().getPosition3d());
        synchronized (projectiles) {
            projectiles.add(projectile);
        }

        gameLogicService.onProjectileFired(actor, muzzle, target.getSyncPhysicalArea().getPosition3d());
    }

    public void tick() {
        try {
            synchronized (projectiles) {
                for (Iterator<Projectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
                    Projectile projectile = iterator.next();
                    if (!projectile.tick()) {
                        iterator.remove();
                        projectileDetonation(projectile);
                    }
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void projectileDetonation(Projectile detonationProjectile) {
        WeaponType weaponType = detonationProjectile.getActor().getSyncWeapon().getWeaponType();
        gameLogicService.onProjectileDetonation(detonationProjectile.getActor(), detonationProjectile.getTarget());
        Collection<SyncBaseItem> possibleTargets = syncItemContainerService.findBaseItemInRect(Rectangle2D.generateRectangleFromMiddlePoint(detonationProjectile.getTarget().toXY(), weaponType.getRange(), weaponType.getRange()));
        for (SyncBaseItem target : possibleTargets) {
            if (!target.getSyncPhysicalArea().overlap(detonationProjectile.getTarget().toXY(), weaponType.getDetonationRadius())) {
                continue;
            }
            if (!baseItemService.isEnemy(detonationProjectile.getActor(), target)) {
                continue;
            }
            target.onAttacked(weaponType.getDamage(), detonationProjectile.getActor());
        }
    }

    public Collection<Projectile> getProjectiles() {
        synchronized (projectiles) {
            return Collections.unmodifiableCollection(projectiles);
        }
    }
}
