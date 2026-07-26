package com.btxtech.server.service.tracking;

import com.btxtech.shared.dto.TipStallJson;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A stall and its resolution arrive as two separate requests. Getting them back together is the
 * whole value of the record: a stall alone says a tip stopped guiding, the pair says the player
 * found their way anyway.
 */
class TipStallServiceTest {
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final TipStallService tipStallService = new TipStallService(mongoTemplate);

    @Test
    void stallIsStoredWithUserAndServerTime() {
        TipStallJson stall = new TipStallJson()
                .stallUuid("stall-1")
                .questId(358)
                .tipTaskName("START_BUILD_PLACER")
                .reason("BUTTON_DISABLED");

        tipStallService.onTipStall(stall, "user-7");

        ArgumentCaptor<TipStallJson> saved = ArgumentCaptor.forClass(TipStallJson.class);
        verify(mongoTemplate).save(saved.capture(), eq(TipStallService.TIP_STALL_COLLECTION));
        assertEquals("user-7", saved.getValue().getUserId());
        // Without a server time the record falls outside every date range the tracking view asks for.
        assertNotNull(saved.getValue().getServerTime());
    }

    @Test
    void resolutionUpdatesTheStallItBelongsTo() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(TipStallService.TIP_STALL_COLLECTION)))
                .thenReturn(updateResult);

        tipStallService.onTipStall(new TipStallJson()
                .stallUuid("stall-1")
                .resolved(true)
                .resolvedMillis(42000), "user-7");

        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(TipStallService.TIP_STALL_COLLECTION));
        // A second document would count as another stall and double the number in the statistics.
        verify(mongoTemplate, never()).save(any(), eq(TipStallService.TIP_STALL_COLLECTION));
    }

    @Test
    void resolutionWithoutItsStallIsKeptRatherThanDropped() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(TipStallService.TIP_STALL_COLLECTION)))
                .thenReturn(updateResult);

        tipStallService.onTipStall(new TipStallJson()
                .stallUuid("lost-stall")
                .resolved(true)
                .resolvedMillis(42000), "user-7");

        // Dropping it would leave the pair looking like a dead end the player never got out of.
        verify(mongoTemplate).save(any(TipStallJson.class), eq(TipStallService.TIP_STALL_COLLECTION));
    }

    @Test
    void aFailingDatabaseDoesNotReachThePlayer() {
        when(mongoTemplate.save(any(), eq(TipStallService.TIP_STALL_COLLECTION)))
                .thenThrow(new RuntimeException("mongo down"));

        // No exception: the call comes from the running game, and losing telemetry is not worth an error.
        tipStallService.onTipStall(new TipStallJson().stallUuid("stall-1"), "user-7");
    }
}
