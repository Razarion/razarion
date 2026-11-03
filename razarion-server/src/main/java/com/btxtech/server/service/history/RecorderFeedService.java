package com.btxtech.server.service.history;

import com.btxtech.server.model.history.GameHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Every history row as it is written, held in memory for the game recorder (razarion-recorder).
 * <p>
 * The recorder sees the world over the game connection like any player, but what only the owner is
 * told - quests, levels, XP, box contents - never reaches it there. It reads that here instead,
 * polling with the last sequence number it got. game_history has no index on serverTime, so this
 * is a buffer beside the writer rather than a query, the same as the community ticker.
 * <p>
 * Nothing is read back after a restart: the epoch changes, and the recorder knows its numbers no
 * longer mean anything.
 */
@Service
public class RecorderFeedService {
    private static final long KEEP_MILLIS = 2 * 60 * 60 * 1000L;
    // Two hours of the firehose are a few thousand rows (10/2026); the cap is for a battle storm.
    private static final int MAX_EVENTS = 20_000;
    private final Logger logger = LoggerFactory.getLogger(RecorderFeedService.class);
    private final ArrayDeque<Event> events = new ArrayDeque<>();
    private final String epoch = UUID.randomUUID().toString();
    private long nextSeq = 1;

    public record Event(long seq, GameHistory history) {
    }

    public record Page(String epoch, long lastSeq, List<Event> events) {
    }

    /** Called for every history row as it is written. */
    public void onHistory(GameHistory gameHistory) {
        try {
            synchronized (events) {
                events.addLast(new Event(nextSeq++, gameHistory));
                trim();
            }
        } catch (Exception e) {
            logger.warn("Recorder feed could not take a history entry: {}", e.getMessage());
        }
    }

    /**
     * Everything after {@code afterSeq}, oldest first, at most {@code max}. A caller with another
     * epoch, or a number not handed out yet, starts from the oldest row held.
     */
    public Page after(String callerEpoch, long afterSeq, int max) {
        synchronized (events) {
            trim();
            long after = epoch.equals(callerEpoch) && afterSeq < nextSeq ? afterSeq : 0;
            List<Event> result = new ArrayList<>();
            for (Event event : events) {
                if (event.seq() > after) {
                    result.add(event);
                    if (result.size() >= max) {
                        break;
                    }
                }
            }
            return new Page(epoch, nextSeq - 1, result);
        }
    }

    private void trim() {
        long cutoff = System.currentTimeMillis() - KEEP_MILLIS;
        while (!events.isEmpty() && (events.size() > MAX_EVENTS || isOlder(events.peekFirst(), cutoff))) {
            events.pollFirst();
        }
    }

    private static boolean isOlder(Event event, long cutoff) {
        return event.history().getServerTime() != null && event.history().getServerTime().getTime() < cutoff;
    }
}
