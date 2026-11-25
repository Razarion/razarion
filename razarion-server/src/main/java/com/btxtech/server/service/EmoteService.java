package com.btxtech.server.service;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.rest.CallSign;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.EmotePacket;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.datatypes.DecimalPosition;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A sign over one of the player's own units for everybody who has it in view - see
 * {@link EmotePacket}. The server checks that the unit is the player's, keeps a player from
 * flooding the others, sends it to every open game page and records it.
 */
@Service
public class EmoteService {
    public static final Set<String> EMOTES = Set.of("WAVE", "THUMBS_UP", "LAUGH", "GG", "QUESTION");
    static final long MIN_GAP_MILLIS = 1500;
    static final int MAX_PER_MINUTE = 12;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final UserService userService;
    private final UserActivityService userActivityService;
    private final Map<String, Deque<Long>> sentByUser = new ConcurrentHashMap<>();

    public enum Result {
        SENT, UNKNOWN_EMOTE, NOT_YOUR_UNIT, TOO_MANY, NOT_IN_GAME
    }

    public EmoteService(SyncItemContainerServiceImpl syncItemContainerService,
                        ClientSystemConnectionService clientSystemConnectionService,
                        UserService userService,
                        UserActivityService userActivityService) {
        this.syncItemContainerService = syncItemContainerService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.userService = userService;
        this.userActivityService = userActivityService;
    }

    public Result send(String emote, int syncItemId, boolean reply) {
        if (emote == null || !EMOTES.contains(emote)) {
            return Result.UNKNOWN_EMOTE;
        }
        String userId = userService.getUserIdFromContextOrNull();
        if (userId == null) {
            return Result.NOT_IN_GAME;
        }
        SyncItem syncItem = syncItemContainerService.getSyncItem(syncItemId);
        if (!(syncItem instanceof SyncBaseItem syncBaseItem)) {
            return Result.NOT_YOUR_UNIT;
        }
        PlayerBase base = syncBaseItem.getBase();
        if (base == null || !Objects.equals(base.getUserId(), userId)) {
            return Result.NOT_YOUR_UNIT;
        }
        if (!allowed(userId, System.currentTimeMillis())) {
            return Result.TOO_MANY;
        }
        DecimalPosition position = syncBaseItem.getAbstractSyncPhysical().getPosition();
        EmotePacket packet = new EmotePacket()
                .setEmote(emote)
                .setSyncItemId(syncItemId)
                .setBaseId(base.getBaseId())
                .setName(base.getName() != null ? base.getName() : CallSign.of(userId))
                .setX(position != null ? position.getX() : 0)
                .setY(position != null ? position.getY() : 0)
                .setReply(reply);
        clientSystemConnectionService.sendEmote(packet);
        userActivityService.onEmoteSent(userId, emote + (reply ? ":REPLY" : ""));
        return Result.SENT;
    }

    /** At most one every {@link #MIN_GAP_MILLIS} and {@link #MAX_PER_MINUTE} a minute, per player. */
    boolean allowed(String userId, long now) {
        Deque<Long> times = sentByUser.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && times.peekFirst() < now - 60_000) {
                times.pollFirst();
            }
            if (!times.isEmpty() && now - times.peekLast() < MIN_GAP_MILLIS) {
                return false;
            }
            if (times.size() >= MAX_PER_MINUTE) {
                return false;
            }
            times.addLast(now);
        }
        sentByUser.values().removeIf(d -> {
            synchronized (d) {
                return d.isEmpty();
            }
        });
        return true;
    }
}
