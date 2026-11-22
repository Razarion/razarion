package com.btxtech.shared.gameengine.planet.connection;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * The game connection opened ahead of the start (GameEngineWorker#preconnect) keeps what the
 * server sends until the engine starts, and then applies it in order.
 */
public class AbstractServerGameConnectionHoldTest {
    private static final String SYNC_1 = "INITIAL_SLAVE_SYNC_INFO#{\"tickCount\":1}";
    private static final String SYNC_2 = "INITIAL_SLAVE_SYNC_INFO#{\"tickCount\":9}";
    private static final String TICK_2 = "TICK_INFO#{\"tickCount\":2}";
    private static final String TICK_3 = "TICK_INFO#{\"tickCount\":3}";
    private static final String TICK_10 = "TICK_INFO#{\"tickCount\":10}";

    @Test
    public void withoutHoldingEveryMessageIsAppliedAtOnce() {
        RecordingConnection connection = new RecordingConnection();
        connection.onMessageText(SYNC_1);
        connection.onMessageText(TICK_2);
        Assert.assertEquals(List.of(SYNC_1, TICK_2), connection.applied);
        Assert.assertFalse(connection.isHoldingMessages());
    }

    @Test
    public void heldMessagesAreAppliedInOrderOnRelease() {
        RecordingConnection connection = new RecordingConnection();
        connection.holdMessages();
        connection.onMessageText(SYNC_1);
        connection.onMessageText(TICK_2);
        connection.onMessageText(TICK_3);
        Assert.assertTrue(connection.applied.isEmpty());
        Assert.assertTrue(connection.isHoldingMessages());

        Assert.assertTrue(connection.releaseHeldMessages());
        Assert.assertEquals(List.of(SYNC_1, TICK_2, TICK_3), connection.applied);
        Assert.assertFalse(connection.isHoldingMessages());

        connection.onMessageText(TICK_10);
        Assert.assertEquals(List.of(SYNC_1, TICK_2, TICK_3, TICK_10), connection.applied);
    }

    @Test
    public void aNewWorldDropsWhatWasHeldBeforeIt() {
        RecordingConnection connection = new RecordingConnection();
        connection.holdMessages();
        connection.onMessageText(SYNC_1);
        connection.onMessageText(TICK_2);
        // reconnect while holding: the server sends the whole world again
        connection.onMessageText(SYNC_2);
        connection.onMessageText(TICK_10);

        Assert.assertTrue(connection.releaseHeldMessages());
        Assert.assertEquals(List.of(SYNC_2, TICK_10), connection.applied);
    }

    @Test
    public void overflowAppliesNothingAndAsksForANewConnection() {
        RecordingConnection connection = new RecordingConnection();
        connection.holdMessages();
        connection.onMessageText(SYNC_1);
        for (int i = 0; i < AbstractServerGameConnection.MAX_HELD_MESSAGES + 5; i++) {
            connection.onMessageText(TICK_2);
        }

        Assert.assertFalse(connection.releaseHeldMessages());
        Assert.assertTrue(connection.applied.isEmpty());
        Assert.assertFalse(connection.isHoldingMessages());
    }

    @Test
    public void releaseWithoutHoldingIsHarmless() {
        RecordingConnection connection = new RecordingConnection();
        Assert.assertTrue(connection.releaseHeldMessages());
        Assert.assertTrue(connection.applied.isEmpty());
    }

    @Test
    public void aFailingHeldMessageDoesNotStopTheRest() {
        RecordingConnection connection = new RecordingConnection();
        connection.failOn = TICK_2;
        connection.holdMessages();
        connection.onMessageText(SYNC_1);
        connection.onMessageText(TICK_2);
        connection.onMessageText(TICK_3);

        Assert.assertTrue(connection.releaseHeldMessages());
        Assert.assertEquals(List.of(SYNC_1, TICK_3), connection.applied);
    }

    private static class RecordingConnection extends AbstractServerGameConnection {
        final List<String> applied = new ArrayList<>();
        String failOn;

        RecordingConnection() {
            super(null, null, null, null, null);
        }

        @Override
        public void handleMessage(String text) {
            if (text.equals(failOn)) {
                throw new IllegalStateException("broken message");
            }
            applied.add(text);
        }

        @Override
        protected void sendToServer(String text) {
        }

        @Override
        protected String toJson(Object param) {
            return null;
        }

        @Override
        protected Object fromJson(String jsonString, GameConnectionPacket packet) {
            return null;
        }

        @Override
        public void init(String bearerToken) {
        }

        @Override
        public void close() {
        }
    }
}
