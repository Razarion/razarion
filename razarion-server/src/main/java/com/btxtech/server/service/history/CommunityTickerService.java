package com.btxtech.server.service.history;

import com.btxtech.server.model.history.GameHistory;
import com.btxtech.server.model.history.GameHistorySource;
import com.btxtech.server.model.history.GameHistoryType;
import com.btxtech.shared.dto.CommunityTickerKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * What the players did lately, for the ticker over the game field: a few lines that show the planet
 * is played by others, with one to three of them on at the same time (PROD, 10/2026).
 * <p>
 * Fed by {@link HistoryService} as it writes, held in memory: game_history has no index on
 * serverTime, and a ticker that every client polls must not scan the collection. After a restart
 * the last six hours are read once.
 */
@Service
public class CommunityTickerService {
    /** Unloading the builder on the island beyond the noob island - the same id local and on PROD. */
    static final int LEFT_NOOB_ISLAND_QUEST_ID = 392;
    private static final long KEEP_MILLIS = 6 * 60 * 60 * 1000L;
    private static final int MAX_EVENTS = 300;
    // As far back as a new player is shown on the first poll
    private static final long WARM_UP_MILLIS = KEEP_MILLIS;
    /*
     * Everything a player builds and destroys is told while there are few players (10/2026), but a
     * factory turning out Vipers or a group of them clearing a bot base would be all the ticker ever
     * says. The same thing again from the same player only after these pauses.
     */
    private static final long BOT_BASE_PER_PLAYER_MILLIS = 3 * 60 * 1000L;
    private static final long BUILT_SAME_TYPE_PER_PLAYER_MILLIS = 2 * 60 * 1000L;
    private static final long DESTROYED_PER_PLAYER_MILLIS = 60 * 1000L;
    private static final Set<String> FIRST_UNIT_TYPES = Set.of("Hydra", "Transporter", "Badger");
    private static final int MAX_FIRST_UNITS_REMEMBERED = 20_000;
    private final Logger logger = LoggerFactory.getLogger(CommunityTickerService.class);
    private final MongoTemplate mongoTemplate;
    private final ArrayDeque<Event> events = new ArrayDeque<>();
    /** When a throttled line was last taken, per kind, player and - for building - item type. */
    private final Map<String, Long> lastTaken = new HashMap<>();
    private final Set<String> firstUnitsSeen = new LinkedHashSet<>();
    private long nextSeq = 1;

    public record Event(long seq, long timeMillis, String userId, CommunityTickerKind kind, Integer level, String what) {
    }

    public CommunityTickerService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        CompletableFuture.runAsync(this::warmUp);
    }

    /** Called for every history row as it is written; cheap for everything the ticker ignores. */
    public void onHistory(GameHistory gameHistory) {
        try {
            Date serverTime = gameHistory.getServerTime();
            Event event = toEvent(gameHistory, serverTime != null ? serverTime.getTime() : System.currentTimeMillis());
            if (event != null) {
                synchronized (events) {
                    events.addLast(event);
                    trim(event.timeMillis());
                }
            }
        } catch (Exception e) {
            logger.warn("Community ticker could not take a history entry: {}", e.getMessage());
        }
    }

    /**
     * Everything after {@code afterSeq}, oldest first, at most {@code max} of the newest. A number
     * the server has not handed out yet comes from a client of the previous run: it gets the latest.
     */
    public List<Event> recent(long afterSeq, int max) {
        synchronized (events) {
            if (afterSeq >= nextSeq) {
                afterSeq = 0;
            }
            trim(System.currentTimeMillis());
            long after = afterSeq;
            List<Event> result = new ArrayList<>();
            events.descendingIterator().forEachRemaining(event -> {
                if (event.seq() > after && result.size() < max) {
                    result.add(0, event);
                }
            });
            return result;
        }
    }

    private Event toEvent(GameHistory history, long time) {
        if (history.getSource() != GameHistorySource.HUMAN || history.getUserId() == null) {
            return null;
        }
        GameHistoryType type = history.getType();
        if (type == null) {
            return null;
        }
        String userId = history.getUserId();
        switch (type) {
            case BASE_CREATED:
                return create(time, userId, CommunityTickerKind.LANDED, null, null);
            case ANONYMOUS_USER_RETURNED:
            case USER_LOGGED_IN:
                return create(time, userId, CommunityTickerKind.RETURNED, null, null);
            case LEVEL_UP:
                if (history.getLevelNumber() == null) {
                    return null;
                }
                return create(time, userId, CommunityTickerKind.LEVEL_UP, history.getLevelNumber(), null);
            case QUEST_PASSED:
                if (history.getQuestId() == null || history.getQuestId() != LEFT_NOOB_ISLAND_QUEST_ID) {
                    return null;
                }
                return create(time, userId, CommunityTickerKind.LEFT_NOOB_ISLAND, null, null);
            case ITEM_DESTROYED:
                // A player's own item sold or removed is HUMAN too, with the player as the victim
                if (userId.equals(history.getTargetUserId()) || history.getItemTypeName() == null) {
                    return null;
                }
                // A Spire is the goal of quest 403, the one destruction always told
                if (!history.getItemTypeName().contains("Spire")
                        && !takeThrottled(CommunityTickerKind.DESTROYED + "|" + userId, time, DESTROYED_PER_PLAYER_MILLIS)) {
                    return null;
                }
                return create(time, userId, CommunityTickerKind.DESTROYED, null, history.getItemTypeName());
            case BASE_DEFEATED:
                if (history.getTargetUserId() != null || history.getTargetBaseName() == null
                        || history.getTargetBaseName().isEmpty()) {
                    return null;
                }
                if (!takeThrottled(CommunityTickerKind.DEFEATED_BOT_BASE + "|" + userId, time, BOT_BASE_PER_PLAYER_MILLIS)) {
                    return null;
                }
                return create(time, userId, CommunityTickerKind.DEFEATED_BOT_BASE, null, history.getTargetBaseName());
            case ITEM_CREATED:
                String itemTypeName = history.getItemTypeName();
                if (itemTypeName == null) {
                    return null;
                }
                String builtKey = CommunityTickerKind.BUILT + "|" + userId + "|" + itemTypeName;
                if (FIRST_UNIT_TYPES.contains(itemTypeName)) {
                    synchronized (events) {
                        boolean first = firstUnitsSeen.add(userId + "|" + itemTypeName);
                        if (firstUnitsSeen.size() > MAX_FIRST_UNITS_REMEMBERED) {
                            firstUnitsSeen.remove(firstUnitsSeen.iterator().next());
                        }
                        if (first) {
                            lastTaken.put(builtKey, time);
                            return create(time, userId, CommunityTickerKind.FIRST_UNIT, null, itemTypeName);
                        }
                    }
                }
                if (!takeThrottled(builtKey, time, BUILT_SAME_TYPE_PER_PLAYER_MILLIS)) {
                    return null;
                }
                return create(time, userId, CommunityTickerKind.BUILT, null, itemTypeName);
            default:
                return null;
        }
    }

    /** True and noted if nothing under this key was taken within {@code pauseMillis}. */
    private boolean takeThrottled(String key, long time, long pauseMillis) {
        synchronized (events) {
            Long last = lastTaken.get(key);
            if (last != null && time - last < pauseMillis) {
                return false;
            }
            lastTaken.put(key, time);
            return true;
        }
    }

    private Event create(long time, String userId, CommunityTickerKind kind, Integer level, String what) {
        synchronized (events) {
            return new Event(nextSeq++, time, userId, kind, level, what);
        }
    }

    private void trim(long now) {
        while (!events.isEmpty() && (events.size() > MAX_EVENTS || now - events.peekFirst().timeMillis() > KEEP_MILLIS)) {
            events.removeFirst();
        }
        // The longest pause; an entry older than that throttles nothing any more
        lastTaken.values().removeIf(last -> now - last > BOT_BASE_PER_PLAYER_MILLIS);
    }

    /**
     * The last six hours from game_history, once after a start, so the ticker is not empty after a
     * deploy. A scan without an index on serverTime - acceptable once per start, never per request.
     * Before the first live event, so the order stays by time.
     */
    private void warmUp() {
        try {
            Query query = new Query(Criteria.where("serverTime").gte(new Date(System.currentTimeMillis() - WARM_UP_MILLIS))
                    .and("source").is(GameHistorySource.HUMAN)
                    .and("type").in(GameHistoryType.BASE_CREATED, GameHistoryType.ANONYMOUS_USER_RETURNED,
                            GameHistoryType.USER_LOGGED_IN, GameHistoryType.LEVEL_UP, GameHistoryType.QUEST_PASSED,
                            GameHistoryType.ITEM_DESTROYED, GameHistoryType.BASE_DEFEATED, GameHistoryType.ITEM_CREATED))
                    .with(Sort.by(Sort.Direction.ASC, "serverTime"));
            List<GameHistory> rows = mongoTemplate.find(query, GameHistory.class, HistoryService.GAME_HISTORY);
            List<Event> warm = new ArrayList<>();
            for (GameHistory row : rows) {
                Event event = toEvent(row, row.getServerTime() != null ? row.getServerTime().getTime() : 0);
                if (event != null) {
                    warm.add(event);
                }
            }
            synchronized (events) {
                // Live events may have arrived meanwhile; the warm ones are older and go in front,
                // and everything is numbered again in that order - seconds after the start, before
                // any client has asked.
                List<Event> all = new ArrayList<>(warm);
                all.addAll(events);
                events.clear();
                nextSeq = 1;
                for (Event event : all) {
                    events.addLast(new Event(nextSeq++, event.timeMillis(), event.userId(), event.kind(), event.level(), event.what()));
                }
                trim(System.currentTimeMillis());
            }
            logger.info("Community ticker warmed up with {} events from {} history rows", warm.size(), rows.size());
        } catch (Exception e) {
            logger.warn("Community ticker warm-up failed: {}", e.getMessage());
        }
    }
}
