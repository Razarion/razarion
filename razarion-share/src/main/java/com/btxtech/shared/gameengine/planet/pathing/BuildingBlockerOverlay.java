package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds the set of A* grid nodes that are blocked by buildings (immovable items).
 *
 * <p>Buildings are deliberately NOT baked into the cached {@link PassabilityGrid} (which is terrain
 * only and shared/bucketed). Historically buildings were resolved purely by ORCA at runtime, which
 * left units stuck when A* routed a path straight through a building. This overlay gives A* awareness
 * of buildings so the path routes around them; ORCA then only handles fine local avoidance.
 *
 * <p>Buildings within a docking clearance of the start or destination are intentionally NOT blocked:
 * those endpoints are where a unit legitimately approaches a building (build / attack / harvest /
 * exit-factory), and ORCA handles the close-quarters avoidance there.
 *
 * <p>Pure / deterministic: the overlay is computed once when the path is created and the resulting
 * path travels inside the command over the network, so this never needs to agree across clients.
 */
public final class BuildingBlockerOverlay {

    /** Clearance (meters), added to building + unit radius, around start and destination where
     * buildings are left to ORCA instead of being blocked in A*. */
    public static final double ENDPOINT_CLEARANCE = 1.0;

    private BuildingBlockerOverlay() {
    }

    public static Set<Index> compute(Collection<Blocker> buildings, DecimalPosition start, DecimalPosition destination, double unitRadius) {
        Set<Index> blocked = new HashSet<>();
        for (Blocker building : buildings) {
            double skipDistance = building.radius + unitRadius + ENDPOINT_CLEARANCE;
            if (building.position.getDistance(start) <= skipDistance
                    || building.position.getDistance(destination) <= skipDistance) {
                // Endpoint docking zone — leave it to ORCA so the unit can still reach/exit here.
                continue;
            }
            // Inflate by the unit radius (Minkowski sum) so the unit body — not just its center —
            // clears the building. ORCA cleans up the residual sub-node overlap.
            GeometricUtil.rasterizeCircle(new Circle2D(building.position, building.radius + unitRadius), (int) TerrainUtil.NODE_SIZE)
                    .forEach(blocked::add);
        }
        return blocked;
    }

    /** Minimal footprint descriptor of a building: its centre and bounding radius. */
    public static final class Blocker {
        public final DecimalPosition position;
        public final double radius;

        public Blocker(DecimalPosition position, double radius) {
            this.position = position;
            this.radius = radius;
        }
    }
}
