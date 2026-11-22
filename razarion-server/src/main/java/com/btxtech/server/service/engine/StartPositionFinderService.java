package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Where a player without a base is put down: the camera flies there, and the base placer opens on
 * the same spot.
 */
@Service
public class StartPositionFinderService {
    /** How many of the emptiest points, counted from the start of the path, a player may land on. */
    static final int FILL_WINDOW = 4;
    /** Points of one landing cell are 21 m apart, cells some 100 m - see #landingCells. */
    static final double CELL_GAP = 40;
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
     * So the whole path is counted and the least crowded point wins. Ties go to one of the first
     * {@link #FILL_WINDOW} in path order, at random: the configured path lists the island cells one
     * after the other, so the cells fill up in turn and a new player starts next to the others,
     * while several players arriving within the same few seconds - before any of them has placed a
     * base and changed the counts - are still not all sent to the same spot.
     * <p>
     * {@code positionMaxItems} only decides what is logged now. If even the emptiest point is more
     * crowded than that, it is still returned: null used to send the client to a random point in
     * the start region, and that fallback checks nothing at all
     * ({@code GeometricUtil.findFreeRandomPosition} passes a null predicate).
     * <p>
     * Costs a full pass where the old one could stop early: thirty-three radius queries instead of
     * a median of seven. The old line reported that pass at 0 ms.
     */
    public DecimalPosition findFreePosition(SlavePlanetConfig slavePlanetConfig) {
        return findFreePosition(slavePlanetConfig, null);
    }

    /**
     * The same, limited to one landing cell when the player chose one on the loading screen - see
     * LandingController. A cell the path does not have is ignored. The player chose it, so the
     * emptiest point of that cell is returned however crowded it is.
     */
    public DecimalPosition findFreePosition(SlavePlanetConfig slavePlanetConfig, Integer landingCell) {
        if (slavePlanetConfig.getPositionRadius() == null || slavePlanetConfig.getPositionMaxItems() == null) {
            throw new RuntimeException("Position radius or maxItems not set");
        }

        long startTime = System.currentTimeMillis();
        List<DecimalPosition> path = slavePlanetConfig.getPositionPath();
        List<LandingCell> cells = landingCells(path);
        if (landingCell != null && landingCell >= 0 && landingCell < cells.size()) {
            path = cells.get(landingCell).slots();
        } else {
            landingCell = null;
        }
        List<DecimalPosition> emptiest = new ArrayList<>();
        int lowestCount = Integer.MAX_VALUE;
        for (DecimalPosition position : path) {
            int count = countBaseItems(position, slavePlanetConfig.getPositionRadius());
            if (count < lowestCount) {
                lowestCount = count;
                emptiest.clear();
            }
            if (count == lowestCount) {
                emptiest.add(position);
            }
        }

        if (emptiest.isEmpty()) {
            logger.warn("Free position not found, no spawn points, duration {}", System.currentTimeMillis() - startTime);
            return null;
        }
        // The path is in fill order, cell after cell, so the earliest of the emptiest points keep a
        // new player next to the others. The window keeps the random spread for players who arrive
        // within the same few seconds.
        DecimalPosition positionFound = emptiest.get(ThreadLocalRandom.current().nextInt(Math.min(FILL_WINDOW, emptiest.size())));
        if (landingCell != null) {
            logger.info("Free position found at {} items {} in chosen landing cell {} duration {}",
                    positionFound, lowestCount, landingCell, System.currentTimeMillis() - startTime);
            return positionFound;
        }
        if (lowestCount > slavePlanetConfig.getPositionMaxItems()) {
            // Still the emptiest spawn point: the client's own fallback is a random point in the
            // start region that checks nothing at all, which is worse than a crowded one.
            logger.warn("Free position not found, emptiest holds {} items, using {} anyway, duration {}",
                    lowestCount, positionFound, System.currentTimeMillis() - startTime);
            return positionFound;
        }
        // Same opening as before on purpose: the analysis that produced this change reads these
        // lines out of Cloud Logging, and a renamed prefix would silently end that series.
        logger.info("Free position found at {} items {} candidates {} of {} duration {}",
                positionFound, lowestCount, emptiest.size(), slavePlanetConfig.getPositionPath().size(),
                System.currentTimeMillis() - startTime);
        return positionFound;
    }

    /**
     * What the loading screen offers: the landing cells of the spawn path, how many players have a
     * base in each, and the cell the server would send this player to if he chose nothing (the
     * cell of the first emptiest point - findFreePosition picks at random among the first few of
     * those, which is nearly always the same cell).
     */
    public LandingOverview landingOverview(SlavePlanetConfig slavePlanetConfig) {
        List<LandingCell> cells = landingCells(slavePlanetConfig.getPositionPath());
        double radius = slavePlanetConfig.getPositionRadius() != null ? slavePlanetConfig.getPositionRadius() : 12;
        List<LandingCellInfo> infos = new ArrayList<>();
        int recommended = 0;
        int lowest = Integer.MAX_VALUE;
        for (LandingCell cell : cells) {
            int cellLowest = Integer.MAX_VALUE;
            for (DecimalPosition slot : cell.slots()) {
                int count = countBaseItems(slot, radius);
                cellLowest = Math.min(cellLowest, count);
                if (count < lowest) {
                    lowest = count;
                    recommended = cell.index();
                }
            }
            infos.add(new LandingCellInfo(cell.index(), cell.center().getX(), cell.center().getY(),
                    cell.slots().size(), countHumanBases(cell, radius), cellLowest));
        }
        return new LandingOverview(infos, recommended);
    }

    /**
     * The spawn path cut into its cells: consecutive points closer than CELL_GAP belong together.
     * The configured path lists the points of one cell after the other, 21 m apart, with some 100 m
     * between cells (PROD 22.11.2026: eight cells of five).
     */
    static List<LandingCell> landingCells(List<DecimalPosition> path) {
        List<LandingCell> cells = new ArrayList<>();
        List<DecimalPosition> current = new ArrayList<>();
        for (DecimalPosition position : path) {
            if (!current.isEmpty() && current.get(current.size() - 1).getDistance(position) > CELL_GAP) {
                cells.add(LandingCell.of(cells.size(), current));
                current = new ArrayList<>();
            }
            current.add(position);
        }
        if (!current.isEmpty()) {
            cells.add(LandingCell.of(cells.size(), current));
        }
        return cells;
    }

    /** Players, not items: one base with twenty units is still one neighbour. Bots do not count. */
    private int countHumanBases(LandingCell cell, double radius) {
        double reach = radius;
        for (DecimalPosition slot : cell.slots()) {
            reach = Math.max(reach, slot.getDistance(cell.center()) + radius);
        }
        Set<Integer> bases = new HashSet<>();
        syncItemContainerService.iterateCellRadiusItem(cell.center(), reach, syncItem -> {
            if (syncItem instanceof SyncBaseItem syncBaseItem && syncBaseItem.getBase() != null
                    && syncBaseItem.getBase().getCharacter() == Character.HUMAN) {
                bases.add(syncBaseItem.getBase().getBaseId());
            }
        });
        return bases.size();
    }

    public record LandingCell(int index, DecimalPosition center, List<DecimalPosition> slots) {
        static LandingCell of(int index, List<DecimalPosition> slots) {
            double x = 0;
            double y = 0;
            for (DecimalPosition slot : slots) {
                x += slot.getX();
                y += slot.getY();
            }
            return new LandingCell(index, new DecimalPosition(x / slots.size(), y / slots.size()), slots);
        }
    }

    public record LandingCellInfo(int index, double x, double y, int slots, int players, int emptiestItems) {
    }

    public record LandingOverview(List<LandingCellInfo> cells, int recommended) {
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
