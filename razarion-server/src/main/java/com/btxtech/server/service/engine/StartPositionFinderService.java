package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Where a player without a base is put down: the camera flies there, and the base placer opens on
 * the same spot.
 */
@Service
public class StartPositionFinderService {
    private final Logger logger = LoggerFactory.getLogger(StartPositionFinderService.class);
    private final SyncItemContainerServiceImpl syncItemContainerService;

    public StartPositionFinderService(SyncItemContainerServiceImpl syncItemContainerService) {
        this.syncItemContainerService = syncItemContainerService;
    }

    /**
     * The emptiest of the configured spawn points, picked at random among those that tie.
     * <p>
     * It used to walk the path and take the first point with no more than {@code positionMaxItems}
     * base items within {@code positionRadius}. Both halves of that hurt. Taking the <em>first</em>
     * meant everybody started at the same end of the list: over seven days on PROD, 24% of the 164
     * new players were sent to the very same coordinate and 38% to one of the first two, while
     * fifteen of the thirty-three points were never used at all. And <em>acceptable</em> is not
     * empty - two buildings may already stand within the radius, which is how the placer came to
     * open on top of things: 48% of the rejected start placements said "blocked by another item".
     * <p>
     * So the whole path is counted and the least crowded point wins, with the tie broken at random
     * so that several players arriving within the same few seconds - before any of them has
     * actually placed a base and changed the counts - do not all get sent to the same spot.
     * <p>
     * {@code positionMaxItems} stays, now as the give-up condition rather than the choice: if even
     * the emptiest point is more crowded than that, this returns null and the client falls back to
     * a random point in the start region. That fallback checks nothing at all
     * ({@code GeometricUtil.findFreeRandomPosition} passes a null predicate), so it is worth
     * avoiding - which is the other reason the threshold is not tightened here.
     * <p>
     * Costs a full pass where the old one could stop early: thirty-three radius queries instead of
     * a median of seven. The old line reported that pass at 0 ms.
     */
    public DecimalPosition findFreePosition(SlavePlanetConfig slavePlanetConfig) {
        if (slavePlanetConfig.getPositionRadius() == null || slavePlanetConfig.getPositionMaxItems() == null) {
            throw new RuntimeException("Position radius or maxItems not set");
        }

        long startTime = System.currentTimeMillis();
        List<DecimalPosition> emptiest = new ArrayList<>();
        int lowestCount = Integer.MAX_VALUE;
        for (DecimalPosition position : slavePlanetConfig.getPositionPath()) {
            int count = countBaseItems(position, slavePlanetConfig.getPositionRadius());
            if (count < lowestCount) {
                lowestCount = count;
                emptiest.clear();
            }
            if (count == lowestCount) {
                emptiest.add(position);
            }
        }

        if (emptiest.isEmpty() || lowestCount > slavePlanetConfig.getPositionMaxItems()) {
            logger.warn("Free position not found, emptiest holds {} items, duration {}",
                    emptiest.isEmpty() ? -1 : lowestCount, System.currentTimeMillis() - startTime);
            return null;
        }
        DecimalPosition positionFound = emptiest.get(ThreadLocalRandom.current().nextInt(emptiest.size()));
        // Same opening as before on purpose: the analysis that produced this change reads these
        // lines out of Cloud Logging, and a renamed prefix would silently end that series.
        logger.info("Free position found at {} items {} candidates {} of {} duration {}",
                positionFound, lowestCount, emptiest.size(), slavePlanetConfig.getPositionPath().size(),
                System.currentTimeMillis() - startTime);
        return positionFound;
    }

    /**
     * Base items standing within the radius. Only SyncBaseItem counts - resources and terrain
     * objects block a placement too, but they do not move, so a spawn point that sits on one is a
     * fixed property of the path rather than a reason to send this player elsewhere.
     */
    private int countBaseItems(DecimalPosition position, double radius) {
        int[] count = {0};
        syncItemContainerService.iterateCellRadiusItem(position, radius, syncItem -> {
            if (syncItem instanceof SyncBaseItem) {
                count[0]++;
            }
        });
        return count[0];
    }
}
