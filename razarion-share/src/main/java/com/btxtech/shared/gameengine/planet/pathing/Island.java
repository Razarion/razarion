package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Beat
 * on 19.05.2018.
 */
public class Island {
    private Collection<SyncPhysicalMovable> activeMovable = new HashSet<>();
    private Collection<SyncPhysicalMovable> passiveMovable = new HashSet<>();
    private Collection<SyncPhysicalArea> fix = new HashSet<>();

    public void add(Contact contact) {
        activeMovable.add(contact.getItem1());
        contact.getItem1().setCrowded();
        if (contact.getItem2() != null) {
            if (contact.getItem2().hasDestination()) {
                ((SyncPhysicalMovable)contact.getItem2()).setCrowded();
                activeMovable.add((SyncPhysicalMovable) contact.getItem2());
            } else if (contact.getItem2().canMove()) {
                passiveMovable.add((SyncPhysicalMovable) contact.getItem2());
            } else {
                fix.add(contact.getItem2());
            }
        }
    }

    public boolean included(Contact contact) {
        if (activeMovable.contains(contact.getItem1())) {
            return true;
        }
        if (contact.getItem2() != null) {
            if (contact.getItem2().hasDestination()) {
                return activeMovable.contains((SyncPhysicalMovable) contact.getItem2());
            } else if (contact.getItem2().canMove()) {
                return activeMovable.contains((SyncPhysicalMovable) contact.getItem2());
            } else {
                fix.add(contact.getItem2());
            }
        }

        return false;
    }

    public void solve() {
        SingleHolder<DecimalPosition> commonVelocityHolder = new SingleHolder<>(DecimalPosition.NULL);
        SingleHolder<Double> minSpeed = new SingleHolder<>();
        activeMovable.forEach(syncPhysicalMovable -> {
            double speed = syncPhysicalMovable.getVelocity().magnitude();
            commonVelocityHolder.setO(commonVelocityHolder.getO().add(syncPhysicalMovable.getVelocity()));
            if (minSpeed.isEmpty()) {
                minSpeed.setO(speed);
            } else {
                minSpeed.setO(Math.min(speed, minSpeed.getO()));
            }

        });
        DecimalPosition commonVelocity = commonVelocityHolder.getO().normalize(minSpeed.getO());
        activeMovable.forEach(syncPhysicalMovable -> syncPhysicalMovable.setVelocity(commonVelocity));
        passiveMovable.forEach(syncPhysicalMovable -> syncPhysicalMovable.setVelocity(commonVelocity));
    }
}
