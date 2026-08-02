package com.btxtech.server.service.history;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.history.GameHistory;
import com.btxtech.server.model.history.GameHistorySource;
import com.btxtech.server.model.history.GameHistoryType;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.GameHistoryEntry;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-user (and bot) game-history event log. Ported from the old controltheland {@code HistoryService},
 * but persisted into the MongoDB {@code game_history} collection instead of a SQL table, matching the
 * other tracking services in {@code com.btxtech.server.service.tracking}. Bot activity is logged too, so
 * this doubles as a debug log for the game engine.
 * <p>
 * Writes are asynchronous: many events originate on the game-engine tick thread (base/item/box
 * lifecycle), which must never block on a Mongo round-trip. Each writer extracts its primitives on the
 * caller thread (inside the caller's transaction, so no JPA entity or live sync object is ever touched
 * off-thread), stamps the event time, and hands a detached {@link GameHistory} POJO to a single-thread
 * executor with a BOUNDED queue: under a busy planet the item/box firehose can outrun Mongo, so once the
 * queue is full further entries are dropped (logged) rather than growing unbounded and OOM-ing the server.
 * <p>
 * Every write swallows all exceptions on purpose: history logging must never break game logic.
 */
@Service
public class HistoryService {
    public static final String GAME_HISTORY = "game_history";
    /** Cap the per-user history returned to the admin backend so a long-lived user stays cheap to load. */
    private static final int MAX_ENTRIES = 100;
    /** Bounded write queue: history must never OOM the server under the tick-thread item/box firehose. */
    private static final int QUEUE_CAPACITY = 50_000;
    /** How long shutdown waits for the write queue to drain before it gives up on the rest. */
    private static final int SHUTDOWN_DRAIN_SECONDS = 5;
    /** Retention for the high-volume engine/bot debug firehose (base/item/box lifecycle). */
    private static final Duration SHORT_RETENTION = Duration.ofDays(7);
    /** Retention for meaningful per-user interactions (login, level, quest, economy, unlock). */
    private static final Duration LONG_RETENTION = Duration.ofDays(365);
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private final AtomicLong droppedCount = new AtomicLong();
    private final ExecutorService writeExecutor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            runnable -> {
                Thread thread = new Thread(runnable, "game-history-writer");
                thread.setDaemon(true);
                return thread;
            },
            (runnable, executor) -> {
                long dropped = droppedCount.incrementAndGet();
                if (dropped % 1000 == 1) {
                    logger.warn("Game history write queue full ({}); dropping entries. Total dropped: {}", QUEUE_CAPACITY, dropped);
                }
            });

    public HistoryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Per-document TTL: a single TTL index over expireAt with expireAfterSeconds=0 deletes each row
     * exactly when its own expireAt passes. Retention is chosen per event type in {@link #retention},
     * so the engine/bot firehose is pruned quickly while user interactions linger. Rows with a null
     * expireAt are never deleted by the index.
     */
    @PostConstruct
    public void ensureTtlIndex() {
        try {
            mongoTemplate.indexOps(GAME_HISTORY).ensureIndex(new Index().on("expireAt", Sort.Direction.ASC).expire(Duration.ZERO));
            // The only read of this collection is loadGameHistoryEntries(): the two arms of its $or,
            // each already in the order it asks for. Without them every lookup scanned the whole
            // collection and sorted the result in memory to return a handful of rows.
            // Two single-field-plus-sort indexes rather than one compound: an $or is planned as a
            // union of its arms, so each arm needs its own.
            mongoTemplate.indexOps(GAME_HISTORY).ensureIndex(new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("serverTime", Sort.Direction.DESC));
            mongoTemplate.indexOps(GAME_HISTORY).ensureIndex(new Index()
                    .on("targetUserId", Sort.Direction.ASC)
                    .on("serverTime", Sort.Direction.DESC));
        } catch (Exception e) {
            logger.warn("Could not ensure game_history indexes: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- account / progression

    public void onUserLoggedIn(UserEntity userEntity, String httpSessionId) {
        save(new GameHistory()
                .type(GameHistoryType.USER_LOGGED_IN)
                .source(GameHistorySource.HUMAN)
                .userId(userEntity.getUserId())
                .httpSessionId(httpSessionId));
    }

    public void onLevelUp(String userId, LevelEntity newLevel) {
        save(new GameHistory()
                .type(GameHistoryType.LEVEL_UP)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .levelNumber(newLevel != null ? newLevel.getNumber() : null));
    }

    public void onQuest(String userId, QuestConfig questConfig, GameHistoryType type) {
        save(new GameHistory()
                .type(type)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .questId(questConfig != null ? questConfig.getId() : null));
    }

    public void onQuestPassed(String userId, int questId, int levelNumber) {
        save(new GameHistory()
                .type(GameHistoryType.QUEST_PASSED)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .questId(questId)
                .levelNumber(levelNumber));
    }

    // ---------------------------------------------------------------- base lifecycle (human + bot)

    public void onBaseCreated(PlayerBaseFull playerBase) {
        GameHistory gameHistory = new GameHistory().type(GameHistoryType.BASE_CREATED);
        applyActorBase(gameHistory, playerBase);
        save(gameHistory);
    }

    /**
     * @param playerBase the eliminated/removed base (victim)
     * @param actor      the base that destroyed it, or null when removed/surrendered (non-combat)
     */
    public void onBaseDefeated(PlayerBase playerBase, PlayerBase actor) {
        GameHistory gameHistory = new GameHistory().type(GameHistoryType.BASE_DEFEATED);
        applyTargetBase(gameHistory, playerBase);
        if (actor != null) {
            applyActorBase(gameHistory, actor);
        } else if (playerBase != null) {
            gameHistory.source(sourceOf(playerBase));
        }
        save(gameHistory);
    }

    // ---------------------------------------------------------------- item lifecycle (human + bot)

    /** A finished/created item (factory unit, placed building, no-spawn unit). Never a build-progress tick. */
    public void onItemCreated(SyncBaseItem syncBaseItem) {
        GameHistory gameHistory = new GameHistory().type(GameHistoryType.ITEM_CREATED);
        if (syncBaseItem != null) {
            applyActorBase(gameHistory, syncBaseItem.getBase());
            applyItem(gameHistory, syncBaseItem);
        }
        save(gameHistory);
    }

    /**
     * @param syncBaseItem the destroyed/removed item (its base is the victim)
     * @param actor        the killing item, or null on non-combat removal
     */
    public void onItemDestroyed(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        GameHistory gameHistory = new GameHistory().type(GameHistoryType.ITEM_DESTROYED);
        if (syncBaseItem != null) {
            applyItem(gameHistory, syncBaseItem);
            applyTargetBase(gameHistory, syncBaseItem.getBase());
        }
        if (actor != null && actor.getBase() != null) {
            applyActorBase(gameHistory, actor.getBase());
        } else if (syncBaseItem != null && syncBaseItem.getBase() != null) {
            gameHistory.source(sourceOf(syncBaseItem.getBase()));
        }
        save(gameHistory);
    }

    // ---------------------------------------------------------------- box lifecycle

    public void onBoxDropped(SyncBoxItem syncBoxItem) {
        save(box(new GameHistory().type(GameHistoryType.BOX_DROPPED).source(GameHistorySource.BOT), syncBoxItem));
    }

    public void onBoxDeleted(SyncBoxItem syncBoxItem) {
        save(box(new GameHistory().type(GameHistoryType.BOX_DELETED).source(GameHistorySource.BOT), syncBoxItem));
    }

    public void onBoxExpired(SyncBoxItem syncBoxItem) {
        save(box(new GameHistory().type(GameHistoryType.BOX_EXPIRED).source(GameHistorySource.BOT), syncBoxItem));
    }

    /** Aggregate pick event; the individual contents are logged separately via onXxxFromBox. */
    public void onBoxPicked(String userId, BoxContent boxContent) {
        save(new GameHistory()
                .type(GameHistoryType.BOX_PICKED)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .crystals(boxContent != null ? boxContent.getCrystals() : null)
                .inventoryItemCount(boxContent != null && boxContent.getInventoryItems() != null ? boxContent.getInventoryItems().size() : null)
                .inventoryArtifactCount(boxContent != null && boxContent.getInventoryArtifacts() != null ? boxContent.getInventoryArtifacts().size() : null));
    }

    public void onCrystalsFromBox(String userId, int crystals) {
        save(new GameHistory()
                .type(GameHistoryType.CRYSTALS_FROM_BOX)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .deltaCrystals(crystals));
    }

    public void onInventoryItemFromBox(String userId, int inventoryId) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ITEM_FROM_BOX)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryId(inventoryId));
    }

    public void onInventoryArtifactFromBox(String userId, int inventoryArtifactId) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ARTIFACT_FROM_BOX)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryArtifactId(inventoryArtifactId));
    }

    // ---------------------------------------------------------------- inventory / economy

    public void onInventoryItemUsed(String userId, int inventoryId) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ITEM_USED)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryId(inventoryId));
    }

    public void onInventoryItemBought(String userId, int inventoryId, int crystalCost) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ITEM_BOUGHT)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryId(inventoryId)
                .deltaCrystals(crystalCost));
    }

    public void onInventoryArtifactBought(String userId, int inventoryArtifactId, int crystalCost) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ARTIFACT_BOUGHT)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryArtifactId(inventoryArtifactId)
                .deltaCrystals(crystalCost));
    }

    public void onInventoryItemAssembled(String userId, int inventoryId) {
        save(new GameHistory()
                .type(GameHistoryType.INVENTORY_ITEM_ASSEMBLED)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .inventoryId(inventoryId));
    }

    public void onLevelUnlockEntityUsedViaCrystals(String userId, int levelUnlockEntityId) {
        save(new GameHistory()
                .type(GameHistoryType.LEVEL_UNLOCK_USED_VIA_CRYSTALS)
                .source(GameHistorySource.HUMAN)
                .userId(userId)
                .levelUnlockEntityId(levelUnlockEntityId));
    }

    // ---------------------------------------------------------------- bot enragement (debug)

    /** @param actor the player base whose kills enraged the bot */
    public void onBotEnrageUp(String botName, BotEnragementStateConfig botEnragementStateConfig, PlayerBase actor) {
        GameHistory gameHistory = new GameHistory()
                .type(GameHistoryType.BOT_ENRAGE_UP)
                .source(GameHistorySource.BOT)
                .botName(botName)
                .botState(botEnragementStateConfig != null ? botEnragementStateConfig.getName() : null);
        // The player that angered the bot is the target, so it shows up in that user's history.
        applyTargetBase(gameHistory, actor);
        save(gameHistory);
    }

    public void onBotEnrageNormal(String botName, BotEnragementStateConfig botEnragementStateConfig) {
        save(new GameHistory()
                .type(GameHistoryType.BOT_ENRAGE_NORMAL)
                .source(GameHistorySource.BOT)
                .botName(botName)
                .botState(botEnragementStateConfig != null ? botEnragementStateConfig.getName() : null));
    }

    // ---------------------------------------------------------------- denormalization helpers

    private void applyActorBase(GameHistory gameHistory, PlayerBase base) {
        if (base == null) {
            return;
        }
        gameHistory.userId(base.getUserId())
                .baseId(base.getBaseId())
                .baseName(base.getName())
                .botId(base.getBotId())
                .source(sourceOf(base));
    }

    private void applyTargetBase(GameHistory gameHistory, PlayerBase base) {
        if (base == null) {
            return;
        }
        gameHistory.targetUserId(base.getUserId())
                .targetBaseId(base.getBaseId())
                .targetBaseName(base.getName());
    }

    private void applyItem(GameHistory gameHistory, SyncBaseItem syncBaseItem) {
        if (syncBaseItem == null) {
            return;
        }
        gameHistory.itemId(syncBaseItem.getId());
        if (syncBaseItem.getBaseItemType() != null) {
            gameHistory.itemTypeId(syncBaseItem.getBaseItemType().getId())
                    .itemTypeName(syncBaseItem.getBaseItemType().getInternalName());
        }
        applyPosition(gameHistory, syncBaseItem);
    }

    private GameHistory box(GameHistory gameHistory, SyncBoxItem syncBoxItem) {
        if (syncBoxItem != null) {
            gameHistory.boxId(syncBoxItem.getId());
            if (syncBoxItem.getBoxItemType() != null) {
                gameHistory.boxItemTypeId(syncBoxItem.getBoxItemType().getId());
            }
            applyPosition(gameHistory, syncBoxItem);
        }
        return gameHistory;
    }

    private void applyPosition(GameHistory gameHistory, SyncItem syncItem) {
        try {
            DecimalPosition position = syncItem.getAbstractSyncPhysical().getPosition();
            if (position != null) {
                gameHistory.x(position.getX()).y(position.getY());
            }
        } catch (Exception e) {
            // position is best-effort; never let it break history logging
        }
    }

    private static GameHistorySource sourceOf(PlayerBase base) {
        return base.getCharacter() != null && base.getCharacter().isBot() ? GameHistorySource.BOT : GameHistorySource.HUMAN;
    }

    // ---------------------------------------------------------------- persistence

    /**
     * Stamp the event time on the caller thread (so ordering/timing reflect when the event happened,
     * not when the queue drains) and offload the Mongo write. Never blocks or throws into the caller.
     */
    private void save(GameHistory gameHistory) {
        Date now = new Date();
        gameHistory.serverTime(now);
        Duration retention = retention(gameHistory.getType());
        gameHistory.expireAt(retention != null ? new Date(now.getTime() + retention.toMillis()) : null);
        try {
            writeExecutor.execute(() -> {
                try {
                    mongoTemplate.save(gameHistory, GAME_HISTORY);
                } catch (Exception e) {
                    logger.warn("Game history write failed: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            // never let history break game logic
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * How long a given event type is kept. The high-volume engine/bot debug events expire quickly;
     * everything a real user did is kept long. Adjust {@link #SHORT_RETENTION}/{@link #LONG_RETENTION}
     * (or return null here for "never expire") to tune.
     */
    private static Duration retention(GameHistoryType type) {
        if (type == null) {
            return LONG_RETENTION;
        }
        return switch (type) {
            case BASE_CREATED, BASE_DEFEATED, ITEM_CREATED, ITEM_DESTROYED,
                 BOX_DROPPED, BOX_EXPIRED, BOX_DELETED, BOT_ENRAGE_UP, BOT_ENRAGE_NORMAL -> SHORT_RETENTION;
            default -> LONG_RETENTION;
        };
    }

    /**
     * Let the queue run dry while the driver is still there. shutdown() alone only stops new
     * submissions and returns at once, so the writer kept working while Spring closed the Mongo
     * client under it - every entry still queued then failed with "state should be: server session
     * pool is open", and a normal restart ended in a salvo of stack traces that reads like a crash.
     * <p>
     * Bean destruction runs dependents before their dependencies, so this method is called while
     * the client is open. The wait is bounded: a full queue against a remote Mongo would take
     * longer than any shutdown grace period, and a preempted pod has about 30 seconds in total.
     * Whatever is left after that is history nobody will miss.
     */
    @PreDestroy
    public void shutdown() {
        writeExecutor.shutdown();
        try {
            if (!writeExecutor.awaitTermination(SHUTDOWN_DRAIN_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Game history writer did not drain within {} s; dropping the rest.",
                        SHUTDOWN_DRAIN_SECONDS);
                writeExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeExecutor.shutdownNow();
        }
    }

    // ---------------------------------------------------------------- read / render

    /**
     * Newest-first, rendered history for one user (events where the user is actor OR target), capped at
     * {@link #MAX_ENTRIES}. Never throws: returns an empty list on any error so the caller stays robust.
     */
    public List<GameHistoryEntry> loadGameHistoryEntries(String userId) {
        try {
            Query query = new Query();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("userId").is(userId),
                    Criteria.where("targetUserId").is(userId)));
            query.with(Sort.by(Sort.Direction.DESC, "serverTime"));
            query.limit(MAX_ENTRIES);
            return mongoTemplate.find(query, GameHistory.class, GAME_HISTORY)
                    .stream()
                    .map(gameHistory -> new GameHistoryEntry()
                            .setDate(gameHistory.getServerTime())
                            .setDescription(render(gameHistory)))
                    .toList();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String render(GameHistory h) {
        GameHistoryType type = h.getType();
        if (type == null) {
            return "Unknown event";
        }
        return switch (type) {
            case USER_LOGGED_IN -> "Logged in / registered";
            case LEVEL_UP -> "Reached level " + h.getLevelNumber();
            case QUEST_ACTIVATED -> "Quest activated: " + h.getQuestId();
            case QUEST_DEACTIVATED -> "Quest deactivated: " + h.getQuestId();
            case QUEST_PASSED -> "Quest passed: " + h.getQuestId() + (h.getLevelNumber() != null ? " (level " + h.getLevelNumber() + ")" : "");
            case BASE_CREATED -> "Base created: " + h.getBaseName() + botSuffix(h) + pos(h);
            case BASE_DEFEATED -> "Base defeated: " + h.getTargetBaseName()
                    + (h.getBaseName() != null ? " destroyed by " + h.getBaseName() : " (removed/surrendered)") + pos(h);
            case ITEM_CREATED -> "Item created: " + itemDesc(h) + " for " + h.getBaseName() + botSuffix(h) + pos(h);
            case ITEM_DESTROYED -> "Item destroyed: " + itemDesc(h) + " of " + h.getTargetBaseName()
                    + (h.getBaseName() != null ? " by " + h.getBaseName() : " (removed)") + pos(h);
            case BOX_DROPPED -> "Box dropped (type " + h.getBoxItemTypeId() + ")" + pos(h);
            case BOX_EXPIRED -> "Box expired (type " + h.getBoxItemTypeId() + ")" + pos(h);
            case BOX_DELETED -> "Box removed (type " + h.getBoxItemTypeId() + ")" + pos(h);
            case BOX_PICKED -> "Box picked (" + nz(h.getCrystals()) + " crystals, " + nz(h.getInventoryItemCount())
                    + " items, " + nz(h.getInventoryArtifactCount()) + " artifacts)";
            case CRYSTALS_FROM_BOX -> "Found " + nz(h.getDeltaCrystals()) + " crystals in box";
            case INVENTORY_ITEM_FROM_BOX -> "Found inventory item " + h.getInventoryId() + " in box";
            case INVENTORY_ARTIFACT_FROM_BOX -> "Found inventory artifact " + h.getInventoryArtifactId() + " in box";
            case INVENTORY_ITEM_USED -> "Used inventory item: " + h.getInventoryId();
            case INVENTORY_ITEM_BOUGHT -> "Bought inventory item " + h.getInventoryId() + " for " + nz(h.getDeltaCrystals()) + " crystals";
            case INVENTORY_ARTIFACT_BOUGHT -> "Bought inventory artifact " + h.getInventoryArtifactId() + " for " + nz(h.getDeltaCrystals()) + " crystals";
            case INVENTORY_ITEM_ASSEMBLED -> "Assembled inventory item " + h.getInventoryId();
            case LEVEL_UNLOCK_USED_VIA_CRYSTALS -> "Unlocked via crystals: entity " + h.getLevelUnlockEntityId();
            case BOT_ENRAGE_UP -> "Bot enraged up: " + h.getBotName() + " -> " + h.getBotState()
                    + (h.getTargetBaseName() != null ? " (by " + h.getTargetBaseName() + ")" : "");
            case BOT_ENRAGE_NORMAL -> "Bot calmed down: " + h.getBotName() + " -> " + h.getBotState();
        };
    }

    private static String itemDesc(GameHistory h) {
        return h.getItemTypeName() != null
                ? h.getItemTypeName() + " (type " + h.getItemTypeId() + ")"
                : "type " + h.getItemTypeId();
    }

    private static String botSuffix(GameHistory h) {
        return h.getSource() == GameHistorySource.BOT ? " [BOT]" : "";
    }

    private static String pos(GameHistory h) {
        return h.getX() != null && h.getY() != null
                ? " @(" + Math.round(h.getX()) + "," + Math.round(h.getY()) + ")"
                : "";
    }

    private static int nz(Integer value) {
        return value != null ? value : 0;
    }
}
