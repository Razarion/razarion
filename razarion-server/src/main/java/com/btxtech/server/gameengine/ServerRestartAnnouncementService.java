package com.btxtech.server.gameengine;

import com.btxtech.shared.datatypes.LifecyclePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Tells the players that the server (the whole JVM, not just the planet) is about to go down.
 * <p>
 * The old GWT version had an admin page where a restart message was typed by hand. That does not
 * work with an automated deployment, so the announcement is triggered from two places instead:
 * <ul>
 *     <li>{@code deploy.ps1} calls the announce REST endpoint before {@code kubectl rollout restart}
 *     and then waits — that is the planned, several-minutes-ahead warning.</li>
 *     <li>{@link ContextClosedEvent} fires the last-second announcement for every other shutdown
 *     (manual rollout, node drain, pod eviction). It arrives only seconds before the socket dies,
 *     but it lets the client say "server is restarting" instead of "connection lost".</li>
 * </ul>
 * The pending announcement is kept so clients connecting during the countdown still see it.
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServerRestartAnnouncementService implements ApplicationListener<ContextClosedEvent> {
    /**
     * Announcement sent when the server is going down without a planned announcement. The client
     * has no time to act on it; it only makes the reconnect overlay say the right thing.
     */
    private static final int SHUTDOWN_HOOK_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(ServerRestartAnnouncementService.class);
    private final ClientSystemConnectionService clientSystemConnectionService;
    /**
     * Wall clock time the server is expected to go down, or null if no restart is announced.
     */
    private volatile Long restartAtMillis;

    public ServerRestartAnnouncementService(ClientSystemConnectionService clientSystemConnectionService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
    }

    /**
     * Broadcasts the countdown to every connected player.
     *
     * @param inSeconds seconds until the server goes down
     */
    public void announceRestart(int inSeconds) {
        if (inSeconds < 0) {
            throw new IllegalArgumentException("inSeconds must not be negative: " + inSeconds);
        }
        restartAtMillis = System.currentTimeMillis() + inSeconds * 1000L;
        logger.info("Server restart announced in {}s", inSeconds);
        clientSystemConnectionService.sendLifecyclePacket(packet(inSeconds));
    }

    /**
     * Drops a pending announcement, e.g. when a deployment was aborted after the announcement went
     * out. Sending 0 seconds makes the client hide the countdown again.
     */
    public void cancelRestart() {
        if (restartAtMillis == null) {
            return;
        }
        restartAtMillis = null;
        logger.info("Server restart announcement cancelled");
        clientSystemConnectionService.sendLifecyclePacket(packet(null));
    }

    /**
     * Sends the still-running countdown to a client that connected after the announcement.
     */
    public void onSystemConnectionOpened(String userId) {
        Integer remaining = remainingSeconds();
        if (remaining != null) {
            clientSystemConnectionService.sendLifecyclePacket(userId, packet(remaining));
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // Runs before the beans are destroyed, so the web socket sessions are still open.
        try {
            if (remainingSeconds() == null) {
                logger.info("Shutting down without a pending announcement - warning clients now");
                clientSystemConnectionService.sendLifecyclePacket(packet(SHUTDOWN_HOOK_SECONDS));
            } else {
                logger.info("Shutting down as announced");
            }
        } catch (Throwable t) {
            logger.warn("Could not announce shutdown: {}", t.getMessage(), t);
        }
    }

    private Integer remainingSeconds() {
        Long restartAt = restartAtMillis;
        if (restartAt == null) {
            return null;
        }
        long remaining = restartAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return null;
        }
        return (int) (remaining / 1000L);
    }

    private LifecyclePacket packet(Integer inSeconds) {
        return new LifecyclePacket()
                .setType(LifecyclePacket.Type.SERVER_RESTART_ANNOUNCEMENT)
                .setRestartInSeconds(inSeconds);
    }
}
