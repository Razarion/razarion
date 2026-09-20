package com.btxtech.server.rest.director;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;

import java.util.List;
import java.util.Objects;

/**
 * Where a base is, and how far across it lies.
 *
 * Both numbers are deliberately robust rather than exact, because one unit can be anywhere: a
 * transporter crossing the map belongs to the base and says nothing about where the base is. On
 * production a base of 15 units had a mean-and-maximum spread of 796 while the median base measured
 * 23, and a camera framed on that filmed the planet from orbit. The median position and the 80th
 * percentile of the distances describe the part of a base that is actually somewhere.
 *
 * Shared by the two halves of director mode because they ask the same question for opposite
 * reasons: the camera half to point at a base, the staging half to keep a staged battle away from
 * one.
 *
 * @param centre median position of the items, or null for a base with nothing in it
 * @param radius 80th percentile of the distances from that centre; 0 when there is no centre
 * @param itemCount how many items the position was taken from
 */
public record BaseGeometry(DecimalPosition centre, double radius, int itemCount) {

    public static BaseGeometry of(PlayerBase base) {
        List<DecimalPosition> positions = base instanceof PlayerBaseFull full
                ? full.getItems().stream()
                .map(item -> item.getAbstractSyncPhysical().getPosition())
                .filter(Objects::nonNull)
                .toList()
                : List.of();
        if (positions.isEmpty()) {
            return new BaseGeometry(null, 0, 0);
        }
        double x = median(positions.stream().mapToDouble(DecimalPosition::getX).sorted().toArray());
        double y = median(positions.stream().mapToDouble(DecimalPosition::getY).sorted().toArray());
        double[] distances = positions.stream()
                .mapToDouble(p -> Math.hypot(p.getX() - x, p.getY() - y))
                .sorted().toArray();
        return new BaseGeometry(new DecimalPosition(x, y), percentile(distances, 0.8), positions.size());
    }

    /**
     * Distance from `position` to the near edge of this base, i.e. to its centre less its spread,
     * and {@link Double#MAX_VALUE} for a base that is nowhere. Never negative: a position inside
     * the base reads as 0 rather than as being on the other side of it.
     */
    public double distanceFrom(DecimalPosition position) {
        if (centre == null) {
            return Double.MAX_VALUE;
        }
        return Math.max(0, Math.hypot(centre.getX() - position.getX(), centre.getY() - position.getY()) - radius);
    }

    /** @param sorted ascending; empty gives 0. */
    private static double median(double[] sorted) {
        return percentile(sorted, 0.5);
    }

    /** @param sorted ascending; empty gives 0. Nearest-rank, which needs no interpolation. */
    private static double percentile(double[] sorted, double p) {
        if (sorted.length == 0) {
            return 0;
        }
        int index = (int) Math.round(p * (sorted.length - 1));
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }
}
