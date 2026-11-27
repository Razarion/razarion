package com.btxtech.server.service;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.rest.CallSign;
import com.btxtech.server.service.history.CommunityTickerService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.CommunityTickerKind;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A greeting from one player to another, wherever the two are: "👋" to somebody who just landed,
 * "GG" to somebody who reached a level or left the noob island - one tap on the line of the
 * community ticker - and one tap back.
 * <p>
 * Why: the signs over units (EmoteService) reach only who has the unit in view, and the newcomers
 * are seldom that close. 2026-11-25..27, 35 new players sent 60 signs; outside of the operator's own
 * visits one was ever seen by anybody. A greeting goes to the person instead of to a place.
 * <p>
 * Only to somebody with the game open at this moment, never to oneself, and a player greets the
 * same player once in {@link #PAIR_PAUSE_MILLIS}. The greeting waits in the receiver's inbox, which
 * the presence poll empties (PresenceController) - no new packet on the game connection.
 * <p>
 * A player who has left the noob island and greets a newcomer still on it gets
 * {@link #HELPER_BONUS} Razarion when the newcomer greets back: once per newcomer, at most
 * {@link #HELPER_BONUSES_PER_DAY} a day, both kept in Mongo across restarts. It pays for welcoming, and only for welcoming that a
 * person answered.
 */
@Service
public class CheerService {
    public static final Set<String> CHEERS = Set.of("WAVE", "GG");
    /** The levels of the noob island; quest 392 (leaving it) belongs to level 8 on PROD. */
    public static final int NEWCOMER_MAX_LEVEL = 8;
    public static final int HELPER_BONUS = 100;
    static final int HELPER_BONUSES_PER_DAY = 10;
    static final long PAIR_PAUSE_MILLIS = 5 * 60 * 1000L;
    /** A greeting not fetched in this time is not news any more. */
    static final long KEEP_MILLIS = 3 * 60 * 1000L;
    static final int MAX_INBOX = 5;
    static final long MIN_GAP_MILLIS = 2000;
    static final int MAX_PER_MINUTE = 8;
    private final CommunityTickerService communityTickerService;
    private final UserService userService;
    private final UserActivityService userActivityService;
    private final ServerGameEngineControl serverGameEngineControl;
    private final CheerBonusStore cheerBonusStore;
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<String, Deque<Cheer>> inboxes = new ConcurrentHashMap<>();
    /** Sent greetings by id, kept so the receiver can answer one. */
    private final Map<Long, Cheer> sent = new ConcurrentHashMap<>();
    private final Map<String, Long> lastPair = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> sentByUser = new ConcurrentHashMap<>();

    public enum Result {
        SENT, UNKNOWN, NOT_ONLINE, YOURSELF, ALREADY, TOO_MANY, NOT_IN_GAME
    }

    /**
     * One greeting as the receiver gets it. {@code bonus}: Razarion paid to the receiver for
     * having welcomed the sender, who answered - only on a reply.
     */
    public record Cheer(long id, long timeMillis, String fromUserId, String fromName, String toUserId, String emote, boolean reply, int bonus) {
    }

    public record Delivered(long id, String from, String emote, boolean reply, int bonus) {
    }

    public CheerService(CommunityTickerService communityTickerService,
                        UserService userService,
                        UserActivityService userActivityService,
                        ServerGameEngineControl serverGameEngineControl,
                        CheerBonusStore cheerBonusStore) {
        this.communityTickerService = communityTickerService;
        this.userService = userService;
        this.userActivityService = userActivityService;
        this.serverGameEngineControl = serverGameEngineControl;
        this.cheerBonusStore = cheerBonusStore;
    }

    /** The greeting a ticker line offers, or null: only lines about somebody arriving or getting on. */
    public static String cheerFor(CommunityTickerKind kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case LANDED, RETURNED -> "WAVE";
            case LEVEL_UP, LEFT_NOOB_ISLAND, DEFEATED_BOT_BASE, FIRST_UNIT -> "GG";
            default -> null;
        };
    }

    /** Greets the player a ticker line is about. */
    public Result greet(long tickerSeq) {
        CommunityTickerService.Event event = communityTickerService.find(tickerSeq);
        String emote = event != null ? cheerFor(event.kind()) : null;
        if (emote == null) {
            return Result.UNKNOWN;
        }
        return send(event.userId(), emote, false);
    }

    /** Greets back whoever sent greeting {@code cheerId} to the asking player. */
    public Result answer(long cheerId) {
        Cheer cheer = sent.get(cheerId);
        String userId = userService.getUserIdFromContextOrNull();
        if (cheer == null || cheer.reply() || !Objects.equals(cheer.toUserId(), userId)) {
            return Result.UNKNOWN;
        }
        return send(cheer.fromUserId(), cheer.emote(), true);
    }

    private Result send(String toUserId, String emote, boolean reply) {
        String fromUserId = userService.getUserIdFromContextOrNull();
        if (fromUserId == null) {
            return Result.NOT_IN_GAME;
        }
        if (fromUserId.equals(toUserId)) {
            return Result.YOURSELF;
        }
        if (!userService.hasOpenSystemConnection(toUserId)) {
            return Result.NOT_ONLINE;
        }
        long now = System.currentTimeMillis();
        String pair = fromUserId + ">" + toUserId + (reply ? ":reply" : "");
        Long last = lastPair.get(pair);
        if (last != null && now - last < PAIR_PAUSE_MILLIS) {
            return Result.ALREADY;
        }
        if (!allowed(fromUserId, now)) {
            return Result.TOO_MANY;
        }
        lastPair.put(pair, now);
        lastPair.values().removeIf(at -> now - at > PAIR_PAUSE_MILLIS);

        Map<String, String> names = userService.getUserNames(Set.of(fromUserId));
        int bonus = reply ? helperBonus(toUserId, fromUserId) : 0;
        Cheer cheer = new Cheer(nextId.getAndIncrement(), now, fromUserId,
                names.getOrDefault(fromUserId, CallSign.of(fromUserId)), toUserId, emote, reply, bonus);
        sent.put(cheer.id(), cheer);
        sent.values().removeIf(other -> now - other.timeMillis() > KEEP_MILLIS * 3);
        Deque<Cheer> inbox = inboxes.computeIfAbsent(toUserId, k -> new ArrayDeque<>());
        synchronized (inbox) {
            inbox.addLast(cheer);
            while (inbox.size() > MAX_INBOX) {
                inbox.pollFirst();
            }
        }
        if (bonus > 0) {
            serverGameEngineControl.addRazarion(toUserId, bonus);
        }
        userActivityService.onCheerSent(fromUserId, emote + (reply ? ":REPLY" : "") + (bonus > 0 ? ":BONUS" : ""));
        return Result.SENT;
    }

    /**
     * The bonus for {@code helperUserId} now that {@code newcomerUserId} answered their greeting, or 0:
     * the helper has left the noob island, the newcomer has not, the pair was not paid before and
     * the helper has bonuses left today. Notes the payment in Mongo
     * (CheerBonusStore), so a restart pays no pair twice.
     */
    int helperBonus(String helperUserId, String newcomerUserId) {
        Map<String, Integer> levels = userService.getUserLevelNumbers(Set.of(helperUserId, newcomerUserId));
        Integer helperLevel = levels.get(helperUserId);
        Integer newcomerLevel = levels.get(newcomerUserId);
        if (!isHelperAndNewcomer(helperLevel, newcomerLevel)) {
            return 0;
        }
        return cheerBonusStore.claim(helperUserId, newcomerUserId, HELPER_BONUSES_PER_DAY) ? HELPER_BONUS : 0;
    }

    /** Whether a greeting from a player of this level to one of that level may earn a bonus. */
    public static boolean isHelperAndNewcomer(Integer helperLevel, Integer newcomerLevel) {
        return helperLevel != null && newcomerLevel != null
                && helperLevel > NEWCOMER_MAX_LEVEL && newcomerLevel <= NEWCOMER_MAX_LEVEL;
    }

    /** The greetings waiting for this player, oldest first; they are handed out once. */
    public List<Delivered> take(String userId) {
        if (userId == null) {
            return List.of();
        }
        Deque<Cheer> inbox = inboxes.remove(userId);
        if (inbox == null) {
            return List.of();
        }
        long now = System.currentTimeMillis();
        List<Delivered> result = new ArrayList<>();
        synchronized (inbox) {
            for (Cheer cheer : inbox) {
                if (now - cheer.timeMillis() <= KEEP_MILLIS) {
                    result.add(new Delivered(cheer.id(), cheer.fromName(), cheer.emote(), cheer.reply(), cheer.bonus()));
                }
            }
        }
        inboxes.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return entry.getValue().stream().allMatch(cheer -> now - cheer.timeMillis() > KEEP_MILLIS);
            }
        });
        return result;
    }

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
            return true;
        }
    }
}
