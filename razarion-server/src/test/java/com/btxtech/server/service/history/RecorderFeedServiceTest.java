package com.btxtech.server.service.history;

import com.btxtech.server.model.history.GameHistory;
import com.btxtech.server.model.history.GameHistorySource;
import com.btxtech.server.model.history.GameHistoryType;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The recorder's feed: everything in order, from where the recorder left off - and from the start
 * when it asks with numbers of a previous server run.
 */
class RecorderFeedServiceTest {

    private static GameHistory row(GameHistoryType type, Date time) {
        return new GameHistory().type(type).source(GameHistorySource.HUMAN).userId("a").serverTime(time);
    }

    private static List<Long> seqs(RecorderFeedService.Page page) {
        return page.events().stream().map(RecorderFeedService.Event::seq).toList();
    }

    @Test
    void continuesWhereTheCallerLeftOff() {
        RecorderFeedService service = new RecorderFeedService();
        service.onHistory(row(GameHistoryType.BASE_CREATED, new Date()));
        service.onHistory(row(GameHistoryType.LEVEL_UP, new Date()));
        String epoch = service.after(null, 0, 100).epoch();
        service.onHistory(row(GameHistoryType.QUEST_PASSED, new Date()));

        RecorderFeedService.Page page = service.after(epoch, 2, 100);
        assertEquals(List.of(3L), seqs(page));
        assertEquals(3L, page.lastSeq());
        assertEquals(GameHistoryType.QUEST_PASSED, page.events().get(0).history().getType());
    }

    @Test
    void anotherEpochStartsFromTheOldest() {
        RecorderFeedService service = new RecorderFeedService();
        service.onHistory(row(GameHistoryType.BASE_CREATED, new Date()));
        service.onHistory(row(GameHistoryType.LEVEL_UP, new Date()));

        assertEquals(List.of(1L, 2L), seqs(service.after("a previous run", 1, 100)));
        // A number not handed out yet is from a previous run too
        String epoch = service.after(null, 0, 100).epoch();
        assertEquals(List.of(1L, 2L), seqs(service.after(epoch, 99, 100)));
    }

    @Test
    void capsThePageAndForgetsOldRows() {
        RecorderFeedService service = new RecorderFeedService();
        service.onHistory(row(GameHistoryType.LEVEL_UP, new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000L)));
        for (int i = 0; i < 5; i++) {
            service.onHistory(row(GameHistoryType.ITEM_CREATED, new Date()));
        }

        RecorderFeedService.Page page = service.after(null, 0, 3);
        assertEquals(List.of(2L, 3L, 4L), seqs(page));
        assertEquals(6L, page.lastSeq());
    }
}
