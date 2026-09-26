package com.btxtech.server.system;

import com.btxtech.shared.system.SimpleExecutorService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A timer's thread ends with the timer. PROD 23.09.2026 had 2366 leaked one-thread pools after 15 h.
 */
class ServerSimpleScheduledFutureTest {

    @Test
    void oneShotTimersLeaveNoThreads() throws Exception {
        int runs = 50;
        CountDownLatch done = new CountDownLatch(runs);
        for (int i = 0; i < runs; i++) {
            ServerSimpleScheduledFuture future = new ServerSimpleScheduledFuture();
            future.init(1, false, SimpleExecutorService.Type.BOT_TIMER, done::countDown);
            future.start();
        }
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(0, awaitTimerThreads(SimpleExecutorService.Type.BOT_TIMER));
    }

    @Test
    void cancelledRepeatingTimersLeaveNoThreads() throws Exception {
        AtomicInteger ticks = new AtomicInteger();
        for (int i = 0; i < 50; i++) {
            ServerSimpleScheduledFuture future = new ServerSimpleScheduledFuture();
            future.init(1, true, SimpleExecutorService.Type.BOT_TICKER, ticks::incrementAndGet);
            future.start();
            future.cancel();
        }
        assertEquals(0, awaitTimerThreads(SimpleExecutorService.Type.BOT_TICKER));
    }

    @Test
    void restartAfterCancelStillRuns() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        ServerSimpleScheduledFuture future = new ServerSimpleScheduledFuture();
        future.init(1, false, SimpleExecutorService.Type.GAME_ENGINE, ran::countDown);
        future.cancel();
        future.start();
        assertTrue(ran.await(5, TimeUnit.SECONDS));
    }

    private static long awaitTimerThreads(SimpleExecutorService.Type type) throws InterruptedException {
        long count = Long.MAX_VALUE;
        for (int attempt = 0; attempt < 50 && count > 0; attempt++) {
            Thread.sleep(50);
            count = Thread.getAllStackTraces().keySet().stream()
                    .filter(thread -> thread.isAlive() && thread.getName().startsWith("timer-" + type + "-"))
                    .count();
        }
        return count;
    }
}
