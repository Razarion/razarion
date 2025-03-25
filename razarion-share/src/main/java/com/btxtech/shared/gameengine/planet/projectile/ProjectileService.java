package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ProjectileService {

    private final BaseItemService baseItemService;

    private final GameLogicService gameLogicService;

    private final SyncItemContainerServiceImpl syncItemContainerService;

    private final ExceptionHandler exceptionHandler;
    private final Collection<Projectile> projectiles = new ArrayList<>();

    @Inject
    public ProjectileService(ExceptionHandler exceptionHandler, SyncItemContainerServiceImpl syncItemContainerService, GameLogicService gameLogicService, BaseItemService baseItemService) {
        this.exceptionHandler = exceptionHandler;
        this.syncItemContainerService = syncItemContainerService;
        this.gameLogicService = gameLogicService;
        this.baseItemService = baseItemService;
    }

    public void onPlanetActivation(PlanetActivationEvent ignore) {
        synchronized (projectiles) {
            projectiles.clear();
        }
    }

    public void fireProjectile(SyncBaseItem actor, SyncBaseItem target) {
        WeaponType weaponType = actor.getSyncWeapon().getWeaponType();

        if (weaponType.getProjectileSpeed() == null) {
            // projectileDetonation(projectileGroup);
            throw new UnsupportedOperationException();
        }
        Projectile projectile = new Projectile(actor, target.getAbstractSyncPhysical().getPosition());
        synchronized (projectiles) {
            projectiles.add(projectile);
        }

        gameLogicService.onProjectileFired(actor, target.getAbstractSyncPhysical().getPosition());
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
        Collection<SyncBaseItem> possibleTargets = syncItemContainerService.findBaseItemInRect(Rectangle2D.generateRectangleFromMiddlePoint(detonationProjectile.getTarget(), weaponType.getRange(), weaponType.getRange()));
        for (SyncBaseItem target : possibleTargets) {
            if (!target.getAbstractSyncPhysical().overlap(detonationProjectile.getTarget(), weaponType.getDetonationRadius())) {
                continue;
            }
            if (!baseItemService.isEnemy(detonationProjectile.getActor(), target)) {
                continue;
            }
            target.onAttacked(weaponType.getDamage(), detonationProjectile.getActor());
        }
    }
}
