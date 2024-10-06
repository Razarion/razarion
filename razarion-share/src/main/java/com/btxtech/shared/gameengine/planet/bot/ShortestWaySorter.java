package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

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

    static class AttackerTargets<T extends SyncItem> {
        private BotSyncBaseItem attacker;
        private TargetDistanceCmp targetDistanceCmp = new TargetDistanceCmp();
        private List<TargetDistance<T>> targetDistance = new ArrayList<>();

        private AttackerTargets(BotSyncBaseItem attacker, Collection<T> targets, BiPredicate<BotSyncBaseItem, T> checker) {
            this.attacker = attacker;
            // Target has move to a container
            targets.stream().filter(target -> checker.test(attacker, target)).forEach(target -> {
                try {
                    targetDistance.add(new TargetDistance<>(attacker, target));
                } catch (TargetHasNoPositionException e) {
                    // Target has move to a container
                }
            });
            Collections.sort(targetDistance, targetDistanceCmp);
        }

        private double getSmallestDistance() {
            return targetDistance.get(0).distance;
        }

        private TargetDistance<T> removeFirstTarget() {
            if (targetDistance.isEmpty()) {
                return null;
            } else {
                return targetDistance.remove(0);
            }
        }

        private void removeTarget(T target) {
            for (Iterator<TargetDistance<T>> iterator = targetDistance.iterator(); iterator.hasNext(); ) {
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

    static private class TargetDistance<T extends SyncItem> {
        private double distance;
        private T target;

        public TargetDistance(BotSyncBaseItem attacker, T target) throws TargetHasNoPositionException {
            this.target = target;
            distance = attacker.getSyncBaseItem().getAbstractSyncPhysical().getDistance(target);
        }
    }

    public static <T extends SyncItem> Map<BotSyncBaseItem, T> setupAttackerTarget(Collection<BotSyncBaseItem> attackers, Collection<T> targets, BiPredicate<BotSyncBaseItem, T> checker) {
        AttackerTargetsCmp attackerTargetsCmp = new AttackerTargetsCmp();

        List<AttackerTargets<T>> attackerTargetsList = new ArrayList<>();
        for (BotSyncBaseItem attacker : attackers) {
            AttackerTargets<T> attackerTargets = new AttackerTargets<>(attacker, targets, checker);
            if (!attackerTargets.isEmpty()) {
                attackerTargetsList.add(attackerTargets);
            }
        }

        Map<BotSyncBaseItem, T> result = new HashMap<>();
        while (!attackerTargetsList.isEmpty()) {
            Collections.sort(attackerTargetsList, attackerTargetsCmp);
            AttackerTargets<T> attackerTargets = attackerTargetsList.remove(0);
            TargetDistance<T> targetDistance = attackerTargets.removeFirstTarget();
            if (targetDistance != null) {
                result.put(attackerTargets.attacker, targetDistance.target);
                for (Iterator<AttackerTargets<T>> iterator = attackerTargetsList.iterator(); iterator.hasNext(); ) {
                    AttackerTargets<T> attackerTargetsToClean = iterator.next();
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

