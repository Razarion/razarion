package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Where a player without a base is put down.
 * <p>
 * The service used to take the first spawn point that was merely acceptable, which on PROD sent 24%
 * of new players to one coordinate and 38% to one of two - and "acceptable" allowed two buildings
 * inside the radius, so the base placer regularly opened on top of something.
 */
class StartPositionFinderServiceTest {
    private static final double RADIUS = 20;

    /**
     * A world where each spawn point has a fixed number of base items around it. The real
     * {@code iterateCellRadiusItem} walks the planet; here the answer is simply looked up, because
     * what is under test is which point gets chosen, not how items are counted.
     */
    private StartPositionFinderService serviceWith(Map<DecimalPosition, Integer> itemsAt) {
        SyncItemContainerServiceImpl container = mock(SyncItemContainerServiceImpl.class);
        doAnswer(invocation -> {
            DecimalPosition centre = invocation.getArgument(0);
            Consumer<SyncItem> callback = invocation.getArgument(2);
            for (int i = 0; i < itemsAt.getOrDefault(centre, 0); i++) {
                callback.accept(mock(SyncBaseItem.class));
            }
            return null;
        }).when(container).iterateCellRadiusItem(any(), anyDouble(), any());
        return new StartPositionFinderService(container);
    }

    private SlavePlanetConfig config(int maxItems, DecimalPosition... path) {
        return new SlavePlanetConfig()
                .positionRadius(RADIUS)
                .positionMaxItems(maxItems)
                .positionPath(List.of(path));
    }

    private static final DecimalPosition CROWDED = new DecimalPosition(178, 25);
    private static final DecimalPosition BUSY = new DecimalPosition(168, 25);
    private static final DecimalPosition EMPTY = new DecimalPosition(158, 25);
    private static final DecimalPosition ALSO_EMPTY = new DecimalPosition(148, 25);

    @Test
    void picksTheEmptiestPointRatherThanTheFirstAcceptableOne() {
        // The first point holds two items, which the old threshold of two allowed - and it won,
        // every time, for everybody. The third is empty and is the one a new player wants.
        Map<DecimalPosition, Integer> world = new HashMap<>();
        world.put(CROWDED, 2);
        world.put(BUSY, 1);
        world.put(EMPTY, 0);

        DecimalPosition found = serviceWith(world)
                .findFreePosition(config(2, CROWDED, BUSY, EMPTY));

        assertEquals(EMPTY, found);
    }

    @Test
    void spreadsPlayersAcrossPointsThatAreEquallyEmpty() {
        // Several players arriving in the same few seconds are counted against the same world:
        // none of them has placed a base yet. Without the random tie-break they would all be sent
        // to whichever empty point comes first in the path, which is the pile-up this replaces.
        Map<DecimalPosition, Integer> world = new HashMap<>();
        world.put(CROWDED, 2);
        world.put(EMPTY, 0);
        world.put(ALSO_EMPTY, 0);
        StartPositionFinderService service = serviceWith(world);

        Set<DecimalPosition> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(service.findFreePosition(config(2, CROWDED, EMPTY, ALSO_EMPTY)));
        }

        assertEquals(Set.of(EMPTY, ALSO_EMPTY), seen);
    }

    @Test
    void givesUpWhenEvenTheEmptiestPointIsTooCrowded() {
        // Null sends the client to a random point in the start region, which checks nothing. That
        // is worse than a crowded spawn point, so the threshold has to stay a last resort and not
        // become the choice.
        Map<DecimalPosition, Integer> world = new HashMap<>();
        world.put(CROWDED, 5);
        world.put(BUSY, 3);

        assertNull(serviceWith(world).findFreePosition(config(2, CROWDED, BUSY)));
    }

    @Test
    void countsOnlyBaseItems() {
        // A resource or a terrain object blocks a placement too, but it does not move: a spawn
        // point standing on one is a property of the path, not a reason to send this player away.
        SyncItemContainerServiceImpl container = mock(SyncItemContainerServiceImpl.class);
        doAnswer(invocation -> {
            Consumer<SyncItem> callback = invocation.getArgument(2);
            callback.accept(mock(SyncItem.class));
            callback.accept(mock(SyncItem.class));
            callback.accept(mock(SyncItem.class));
            return null;
        }).when(container).iterateCellRadiusItem(any(), anyDouble(), any());

        DecimalPosition found = new StartPositionFinderService(container)
                .findFreePosition(config(0, CROWDED));

        assertEquals(CROWDED, found);
    }

    @Test
    void readsEveryPointOfThePathBeforeChoosing() {
        // The old version stopped at the first acceptable point, so the tail of the path was never
        // looked at - fifteen of the thirty-three were never used at all. The emptiest spot is
        // frequently at the end, and finding it means reading all of them.
        List<DecimalPosition> path = new ArrayList<>();
        Map<DecimalPosition, Integer> world = new HashMap<>();
        for (int i = 0; i < 33; i++) {
            DecimalPosition position = new DecimalPosition(178 - i * 10, 25);
            path.add(position);
            world.put(position, i == 32 ? 0 : 2);
        }

        DecimalPosition found = serviceWith(world)
                .findFreePosition(config(2, path.toArray(new DecimalPosition[0])));

        assertEquals(path.get(32), found);
        assertTrue(world.get(found) == 0);
    }
}
