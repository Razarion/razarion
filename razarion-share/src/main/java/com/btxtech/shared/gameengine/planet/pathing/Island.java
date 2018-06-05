package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Beat
 * on 19.05.2018.
 */
@Deprecated
public class Island {
    private Collection<SyncPhysicalMovable> activeMovable = new HashSet<>();
    private Collection<Integer> activeMovableIds = new ArrayList<>();
    private Collection<SyncPhysicalMovable> passiveMovable = new HashSet<>();
    private Collection<Integer> passiveMovableIds = new HashSet<>();

    public void add(Contact contact) {
        DebugHelperStatic.add2printOnTick("\nContact: " + contact.getItem1().getSyncItem().getId() + ":" + contact.getItem2().getSyncItem().getId());
        if (contact.getItem1() != null) {
            addActive(contact.getItem1());
        }
        if (contact.getItem2() != null) {
            addActive(contact.getItem2());
        }
    }

    public void addActive(SyncPhysicalArea syncPhysicalArea) {
        if (syncPhysicalArea.canMove()) {
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
            syncPhysicalMovable.setCrowded();
            if (syncPhysicalMovable.hasDestination()) {
                activeMovable.add(syncPhysicalMovable);
                activeMovableIds.add(syncPhysicalMovable.getSyncItem().getId());
            } else {
                passiveMovable.add(syncPhysicalMovable);
                passiveMovableIds.add(syncPhysicalMovable.getSyncItem().getId());
            }
        }
    }

    public boolean included(Contact contact) {
        if (contact.getItem1() != null && included(contact.getItem1())) {
            return true;
        }
        if (contact.getItem2() != null && included(contact.getItem2())) {
            return true;
        }

        return false;
    }

    public boolean included(SyncPhysicalArea syncPhysicalArea) {
        return activeMovableIds.contains(syncPhysicalArea.getSyncItem().getId()) || passiveMovableIds.contains(syncPhysicalArea.getSyncItem().getId());
    }

    public void solve() {
        Collection<VelocityObstacleSolver> velocityObstacleSolvers = new ArrayList<>();
        activeMovable.forEach(syncPhysicalMovable -> {
            VelocityObstacleSolver velocityObstacleSolver = new VelocityObstacleSolver(syncPhysicalMovable);
            activeMovable.forEach(other -> {
                if (syncPhysicalMovable == other) {
                    return;
                }
                velocityObstacleSolver.analyzeAndAdd(other);
            });
            velocityObstacleSolver.solve();
            velocityObstacleSolvers.add(velocityObstacleSolver);
        });

        velocityObstacleSolvers.forEach(VelocityObstacleSolver::implementVelocity);
        //  TODO check if whole island is solved. If not solved rerun solve
    }
}
