package com.btxtech.server.service.engine;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runs what the game engine reports from inside a tick - a quest passed, a box picked - but has to
 * write to the databases: one thread, in the order the events came, so a player's level up, next quest
 * and crystals land in sequence.
 * <p>
 * The tick waited for all of it. A passed quest read and wrote the user some ten times in MariaDB and
 * saved user_activity to Mongo Atlas, which sits in europe-west1 while the server runs in us-central1:
 * a tenth to half a second in which nothing moved for anyone - under the 1 s that [TickStall] reports,
 * so the logs never showed it.
 */
@Service
public class EngineEventExecutor {
    private static final long SHUTDOWN_WAIT_SECONDS = 10;
    private final Logger logger = LoggerFactory.getLogger(EngineEventExecutor.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "engine-events");
        thread.setDaemon(true);
        return thread;
    });

    public void execute(String what, Runnable runnable) {
        executor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                // One failed event must not stop the ones behind it.
                logger.warn("Engine event failed: {}", what, t);
            }
        });
    }

    /** What is queued is a player's progress: let it be written before the server goes down. */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Engine events still queued after {} s at shutdown - dropped", SHUTDOWN_WAIT_SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
