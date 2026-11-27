package com.btxtech.server.service;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.service.engine.StartPositionFinderService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A/B: a new player starts in the landing cell next to a newcomer who is playing right now.
 * <p>
 * Why: the newcomers are seldom close enough to see each other. 2026-11-25..27, 35 new players
 * sent 60 signs over their units and, outside the operator's own visits, one of them was seen by
 * somebody. The spawn path already fills cell after cell, but the bases standing in a cell are
 * mostly of players long gone - a saved player keeps his base.
 * <p>
 * Arm by the userId, half and half: {@code near} takes the cell nearest to the base of an online
 * newcomer (base younger than {@link #NEWCOMER_BASE_MILLIS}, within {@link #MAX_DISTANCE}, with a
 * free spawn point), {@code plain} keeps the usual choice. Both arms record whether there was such a
 * cell (user_activity SPAWN_NEAR, detail {@code near|plain:found|full|alone}), so the two can be
 * compared on the players for whom it made a difference. A player who chose a cell on the loading
 * screen is not touched.
 */
@Service
public class SpawnNearService {
    static final long NEWCOMER_BASE_MILLIS = 15 * 60 * 1000L;
    static final double MAX_DISTANCE = 250;
    private final Logger logger = LoggerFactory.getLogger(SpawnNearService.class);
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final BaseItemService baseItemService;
    private final StartPositionFinderService startPositionFinderService;
    private final UserActivityService userActivityService;

    public SpawnNearService(ClientSystemConnectionService clientSystemConnectionService,
                            BaseItemService baseItemService,
                            StartPositionFinderService startPositionFinderService,
                            UserActivityService userActivityService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.baseItemService = baseItemService;
        this.startPositionFinderService = startPositionFinderService;
        this.userActivityService = userActivityService;
    }

    public static boolean isNearArm(String userId) {
        return userId != null && Math.floorMod(userId.hashCode(), 2) == 0;
    }

    /** The landing cell to put this player in, or null for the usual choice. Records the arm. */
    public Integer chooseCell(String userId, SlavePlanetConfig slavePlanetConfig) {
        try {
            List<DecimalPosition> neighbours = onlineNewcomerBases(userId, System.currentTimeMillis());
            boolean near = isNearArm(userId);
            String outcome;
            Integer cell = null;
            if (neighbours.isEmpty()) {
                outcome = "alone";
            } else {
                cell = startPositionFinderService.cellNear(slavePlanetConfig, neighbours, MAX_DISTANCE);
                outcome = cell != null ? "found" : "full";
            }
            userActivityService.onSpawnNear(userId, (near ? "near:" : "plain:") + outcome);
            if (near && cell != null) {
                logger.info("Spawn near a newcomer: landing cell {} ({} newcomers online)", cell, neighbours.size());
                return cell;
            }
            return null;
        } catch (Exception e) {
            logger.warn("Spawn near failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Where the bases of the other newcomers playing right now stand - one point per base. */
    List<DecimalPosition> onlineNewcomerBases(String ownUserId, long now) {
        List<DecimalPosition> positions = new ArrayList<>();
        for (String userId : clientSystemConnectionService.getOpenConnections().keySet()) {
            if (Objects.equals(userId, ownUserId)) {
                continue;
            }
            PlayerBaseFull base = baseItemService.getPlayerBaseFull4UserId(userId);
            if (base == null || base.getCharacter() != Character.HUMAN || base.isAbandoned()
                    || now - base.getCreatedMillis() > NEWCOMER_BASE_MILLIS) {
                continue;
            }
            for (SyncBaseItem item : base.getItems()) {
                DecimalPosition position = item.getAbstractSyncPhysical().getPosition();
                if (position != null) {
                    positions.add(position);
                    break;
                }
            }
        }
        return positions;
    }
}
