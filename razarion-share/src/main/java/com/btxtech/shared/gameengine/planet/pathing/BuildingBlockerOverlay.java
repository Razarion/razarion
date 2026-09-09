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
 * <p><b>Endpoint clearance is a disc around the endpoint, not amnesty for a whole building.</b> The
 * first version of this class skipped any building whose centre was within
 * {@code radius + unitRadius + ENDPOINT_CLEARANCE} of the start or the destination, on the grounds
 * that a unit must be able to leave the building it is docked at. That is the right goal and the
 * wrong instrument, and it made the anti-stuck machinery useless in exactly the case it was built
 * for:
 *
 * <p>{@code PathingService.replanStuckItems} recomputes the path from the unit's <i>current</i>
 * position - which, for a wedged unit, is by definition inside the skip radius of the building it is
 * wedged against. So the replan got the same overlay, ran the same deterministic A*, and produced
 * the same path. Three times, then the unit stopped for good. A Factory (r 2.55) and a Harvester
 * (r 1.0) give a skip radius of 4.55 m, and a unit touching that factory stands at 3.55 m.
 *
 * <p>Freeing a disc around the endpoint instead achieves the actual goal - the unit can always step
 * off where it stands and arrive where it is going - while A* still sees the rest of the building
 * and routes around it, on the first path and on every replan.
 *
 * <p>One whole-building exemption survives, and only one: when the destination lies inside a
 * building's footprint, that building <i>is</i> the goal (attack, build, dock). Blocking it would
 * make the goal unreachable, A* would fall back to its best-fit node, and the unit would stop
 * wherever that landed - possibly outside its own weapon or build range.
 *
 * <p>On the start side there is no whole-building exemption at all any more. A unit that is inside
 * a footprint rather than beside one simply gets a larger disc — see {@code startClearance} — which
 * covers the same need without ever hiding the far side of the building from A*.
 *
 * <p>Pure / deterministic: the overlay is computed once when the path is created and the resulting
 * path travels inside the command over the network, so this never needs to agree across clients.
 */
public final class BuildingBlockerOverlay {

    /** Clearance (meters) added to the unit radius around start and destination, where buildings
     * are left to ORCA instead of being blocked in A*. */
    public static final double ENDPOINT_CLEARANCE = 1.0;

    private BuildingBlockerOverlay() {
    }

    public static Set<Index> compute(Collection<Blocker> buildings, DecimalPosition start, DecimalPosition destination, double unitRadius) {
        Set<Index> blocked = new HashSet<>();
        for (Blocker building : buildings) {
            // Inflate by the unit radius (Minkowski sum) so the unit body — not just its center —
            // clears the building. ORCA cleans up the residual sub-node overlap.
            double footprint = building.radius + unitRadius;
            if (building.position.getDistance(destination) <= footprint) {
                // The destination is inside this building: it is the goal, not an obstacle.
                continue;
            }
            rasterize(building.position, footprint).forEach(blocked::add);
        }
        // Endpoint clearance. Applied after the fact so that a building near an endpoint is blocked
        // everywhere except right at that endpoint, instead of vanishing from the overlay entirely.
        double clearance = unitRadius + ENDPOINT_CLEARANCE;
        rasterize(start, startClearance(buildings, start, unitRadius, clearance)).forEach(blocked::remove);
        rasterize(destination, clearance).forEach(blocked::remove);
        return blocked;
    }

    /**
     * The clearance disc a unit needs at its own position in order to be able to leave it.
     *
     * <p>Normally {@code unitRadius + ENDPOINT_CLEARANCE}. It grows only for a unit that is *inside*
     * a footprint rather than beside one — a building raised on top of it, or a spawn that landed
     * badly. Such a unit has to cross its own penetration depth before it is clear of the wall, and
     * a fixed disc would not reach that far, so A* would find no way out at all.
     *
     * <p>Grown continuously rather than by a threshold on purpose. A rule of the form "exempt the
     * whole building if the unit is more than X inside it" has a cliff exactly where the interesting
     * case lives: a unit wedged against a wall sits at penetration zero, one that ORCA squeezed a
     * few centimetres in sits just past it, and they would get opposite treatment for a difference
     * nobody can see. Here a deeper unit simply gets a slightly bigger disc, and a unit that is
     * merely touching gets the ordinary one.
     */
    private static double startClearance(Collection<Blocker> buildings, DecimalPosition start, double unitRadius, double base) {
        double clearance = base;
        for (Blocker building : buildings) {
            double penetration = (building.radius + unitRadius) - building.position.getDistance(start);
            if (penetration > 0) {
                clearance = Math.max(clearance, penetration + base);
            }
        }
        return clearance;
    }

    private static Collection<Index> rasterize(DecimalPosition centre, double radius) {
        return GeometricUtil.rasterizeCircle(new Circle2D(centre, radius), (int) TerrainUtil.NODE_SIZE);
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
