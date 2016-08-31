package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.PlanetService;

/**
 * User: beat
 * Date: 18.10.13
 * Time: 08:08
 */
public class ActiveProjectile {
    private DecimalPosition decimalPosition;
    private DecimalPosition projectileTarget;
    private WeaponType weaponType;
    private int muzzleNr;
    private ActiveProjectileGroup activeProjectileGroup;
    private long lastTick;

    public ActiveProjectile(ActiveProjectileGroup activeProjectileGroup, SyncBaseItem syncBaseItem, DecimalPosition projectileTarget, int angleIndex, WeaponType weaponType, int muzzleNr) {
        this.activeProjectileGroup = activeProjectileGroup;
        this.projectileTarget = projectileTarget;
        this.weaponType = weaponType;
        this.muzzleNr = muzzleNr;
        decimalPosition = syncBaseItem.getSyncItemArea().getPosition().add(weaponType.getMuzzleFlashPosition(muzzleNr, angleIndex));
    }

    public void tick() {
        decimalPosition = decimalPosition.getPointWithDistance(PlanetService.TICK_FACTOR * (double) weaponType.getProjectileSpeed(), projectileTarget, false);
        lastTick = System.currentTimeMillis();
    }

    public boolean isTargetReached() {
        return decimalPosition.equals(projectileTarget);
    }

    public Index getPosition() {
        return decimalPosition.getPosition();
    }

    public Index getInterpolatedPosition(long timeStamp) {
        if (lastTick == 0) {
            return getPosition();
        } else {
            double factor = (double) (timeStamp - lastTick) / 1000.0;
            return decimalPosition.getPointWithDistance(factor * (double) weaponType.getProjectileSpeed(), projectileTarget, false).getPosition();
        }
    }

    public int getMuzzleNr() {
        return muzzleNr;
    }

    public boolean isAlive() {
        return activeProjectileGroup.isAlive();
    }

    @Override
    public String toString() {
        return "ActiveProjectile{" +
                "decimalPosition=" + decimalPosition +
                ", muzzleNr=" + muzzleNr +
                '}';
    }

    public long getLastTick() {
        return lastTick;
    }
}
