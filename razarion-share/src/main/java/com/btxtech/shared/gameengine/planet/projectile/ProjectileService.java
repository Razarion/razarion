package com.btxtech.shared.gameengine.planet.projectile;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 15.07.2016.
 */
@ApplicationScoped
public class ProjectileService {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private final MapList<BaseItemType, Projectile> projectiles = new MapList<>();

    public void onPlanetActivation(@Observes PlanetActivationEvent ignore) {
        synchronized (projectiles) {
            projectiles.clear();
        }
    }

    public void fireProjectile(long timeStamp, SyncBaseItem actor, SyncBaseItem target) {
        WeaponType weaponType = actor.getSyncWeapon().getWeaponType();

        Vertex muzzle = actor.getSyncWeapon().createTurretModelMatrices().getModel().multiply(weaponType.getTurretType().getMuzzlePosition(), 1.0);
        if (weaponType.getProjectileSpeed() == null) {
            // projectileDetonation(projectileGroup);
            throw new UnsupportedOperationException();
        }
        Projectile projectile = new Projectile(timeStamp, actor, muzzle, target.getSyncPhysicalArea().getPosition3d());
        synchronized (projectiles) {
            projectiles.put(actor.getBaseItemType(), projectile);
        }

        gameLogicService.onProjectileFired(actor, muzzle, target.getSyncPhysicalArea().getPosition3d().sub(muzzle));
    }

    public void tick(long timeStamp) {
        Collection<Projectile> detonationProjectiles = new ArrayList<>();
        synchronized (projectiles) {
            projectiles.getAll().stream().filter(projectile -> projectile.isTargetReached(timeStamp)).forEach(projectile -> {
                detonationProjectiles.add(projectile);
                projectiles.remove(projectile.getActor().getBaseItemType(), projectile);
            });
        }
        detonationProjectiles.forEach(projectile -> projectileDetonation(projectile, timeStamp));
    }

    public List<ModelMatrices> getProjectiles(BaseItemType baseItemType, long timeStamp) {
        synchronized (projectiles) {
            return projectiles.getSave(baseItemType).stream().map(projectile -> projectile.getInterpolatedModelMatrices(timeStamp)).collect(Collectors.toList());
        }
    }

    private void projectileDetonation(Projectile detonationProjectile, long timeStamp) {
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
            target.onAttacked(weaponType.getDamage(), detonationProjectile.getActor(), timeStamp);
        }
    }
}
