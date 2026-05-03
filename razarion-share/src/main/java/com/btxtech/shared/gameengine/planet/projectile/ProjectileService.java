package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponKind;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 15.07.2016.
 */
@Singleton
public class ProjectileService {
    private final Logger logger = Logger.getLogger(ProjectileService.class.getName());
    private final BaseItemService baseItemService;
    private final GameLogicService gameLogicService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final Collection<Projectile> projectiles = new ArrayList<>();

    @Inject
    public ProjectileService(SyncItemContainerServiceImpl syncItemContainerService,
                             GameLogicService gameLogicService,
                             BaseItemService baseItemService) {
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
        DecimalPosition targetPos = target.getAbstractSyncPhysical().getPosition();

        if (weaponType.getWeaponKind() == WeaponKind.LIGHTNING) {
            // Instant hit: damage applied this tick. Both events fire so the client
            // can render bolt + impact in sync. Visual duration lives on the client.
            gameLogicService.onProjectileFired(actor, target.getId(), targetPos);
            gameLogicService.onProjectileDetonation(actor, targetPos);
            applyDamage(actor, targetPos);
            return;
        }
        Projectile projectile = new Projectile(actor, targetPos);
        synchronized (projectiles) {
            projectiles.add(projectile);
        }

        gameLogicService.onProjectileFired(actor, target.getId(), targetPos);
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
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    private void projectileDetonation(Projectile detonationProjectile) {
        gameLogicService.onProjectileDetonation(detonationProjectile.getActor(), detonationProjectile.getTarget());
        applyDamage(detonationProjectile.getActor(), detonationProjectile.getTarget());
    }

    private void applyDamage(SyncBaseItem actor, DecimalPosition targetPos) {
        WeaponType weaponType = actor.getSyncWeapon().getWeaponType();
        Collection<SyncBaseItem> possibleTargets = syncItemContainerService.findBaseItemInRect(Rectangle2D.generateRectangleFromMiddlePoint(targetPos, weaponType.getRange(), weaponType.getRange()));
        for (SyncBaseItem target : possibleTargets) {
            if (!target.getAbstractSyncPhysical().overlap(targetPos, weaponType.getDetonationRadius())) {
                continue;
            }
            if (!baseItemService.isEnemy(actor, target)) {
                continue;
            }
            target.onAttacked(weaponType.getDamage(), actor);
        }
    }
}
