package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: beat
 * Date: 11.11.2011
 * Time: 18:33:46
 */
public class ShortestWaySorter {
    private static class AttackerTargetsCmp implements Comparator<AttackerTargets> {
        @Override
        public int compare(AttackerTargets o1, AttackerTargets o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.getSmallestDistance() < o2.getSmallestDistance()) {
                return -1;
            } else if (o1.getSmallestDistance() > o2.getSmallestDistance()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static class TargetDistanceCmp implements Comparator<TargetDistance> {
        @Override
        public int compare(TargetDistance o1, TargetDistance o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.distance < o2.distance) {
                return -1;
            } else if (o1.distance > o2.distance) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    static class AttackerTargets {
        private BotSyncBaseItem attacker;
        private TargetDistanceCmp targetDistanceCmp = new TargetDistanceCmp();
        private List<TargetDistance> targetDistance = new ArrayList<TargetDistance>();

        private AttackerTargets(BotSyncBaseItem attacker, Collection<SyncBaseItem> targets) {
            this.attacker = attacker;
            for (SyncBaseItem target : targets) {
                if (attacker.isAbleToAttack(target.getBaseItemType())) {
                    try {
                        targetDistance.add(new TargetDistance(attacker, target));
                    } catch (TargetHasNoPositionException e) {
                        // Target has move to a container
                    }
                }
            }
            Collections.sort(targetDistance, targetDistanceCmp);
        }

        private double getSmallestDistance() {
            return targetDistance.get(0).distance;
        }

        private TargetDistance removeFirstTarget() {
            if (targetDistance.isEmpty()) {
                return null;
            } else {
                return targetDistance.remove(0);
            }
        }

        private void removeTarget(SyncBaseItem target) {
            for (Iterator<TargetDistance> iterator = targetDistance.iterator(); iterator.hasNext(); ) {
                TargetDistance distance = iterator.next();
                if (distance.target.equals(target)) {
                    iterator.remove();
                    return;
                }
            }
        }

        private boolean isEmpty() {
            return targetDistance.isEmpty();
        }
    }

    static private class TargetDistance {
        private double distance;
        private SyncBaseItem target;

        public TargetDistance(BotSyncBaseItem attacker, SyncBaseItem target) throws TargetHasNoPositionException {
            this.target = target;
            distance = attacker.getDistanceTo(target);
        }
    }

    public static Map<BotSyncBaseItem, SyncBaseItem> setupAttackerTarget(Collection<BotSyncBaseItem> attackers, Collection<SyncBaseItem> targets) {
        AttackerTargetsCmp attackerTargetsCmp = new AttackerTargetsCmp();

        List<AttackerTargets> attackerTargetsList = new ArrayList<AttackerTargets>();
        for (BotSyncBaseItem attacker : attackers) {
            AttackerTargets attackerTargets = new AttackerTargets(attacker, targets);
            if (!attackerTargets.isEmpty()) {
                attackerTargetsList.add(attackerTargets);
            }
        }

        Map<BotSyncBaseItem, SyncBaseItem> result = new HashMap<BotSyncBaseItem, SyncBaseItem>();
        while (!attackerTargetsList.isEmpty()) {
            Collections.sort(attackerTargetsList, attackerTargetsCmp);
            AttackerTargets attackerTargets = attackerTargetsList.remove(0);
            TargetDistance targetDistance = attackerTargets.removeFirstTarget();
            if (targetDistance != null) {
                result.put(attackerTargets.attacker, targetDistance.target);
                for (Iterator<AttackerTargets> iterator = attackerTargetsList.iterator(); iterator.hasNext(); ) {
                    AttackerTargets attackerTargetsToClean = iterator.next();
                    attackerTargetsToClean.removeTarget(targetDistance.target);
                    if (attackerTargetsToClean.isEmpty()) {
                        iterator.remove();
                    }
                }
            }
        }
        return result;
    }
}

