package com.btxtech.server.gameengine;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 26.02.2018.
 */
@Singleton
@Deprecated
public class PathingChangesDisruptor implements EventHandler<SingleHolder<Collection<Set<SyncBaseItem>>>> {
    private final static long DE_BOUNCING = 2000;
    private final static long PERIODIC_RUNNER_PERIOD = 500;
    @Resource(name = "DefaultManagedThreadFactory")
    private ManagedThreadFactory managedThreadFactory;
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    private RingBuffer<SingleHolder<Collection<Set<SyncBaseItem>>>> ringBuffer;
    private final Map<Set<SyncBaseItem>, Long> lastSents = new HashMap<>();
    private Map<Set<SyncBaseItem>, Long> waitingSyncBaseItems = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        try {
            scheduleExecutor.scheduleAtFixedRate(this::periodicRunner, PERIODIC_RUNNER_PERIOD, PERIODIC_RUNNER_PERIOD, TimeUnit.MILLISECONDS);
            Disruptor<SingleHolder<Collection<Set<SyncBaseItem>>>> disruptor = new Disruptor<>(SingleHolder::new, 512, managedThreadFactory);
            disruptor.handleEventsWith(this);
            disruptor.start();
            ringBuffer = disruptor.getRingBuffer();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
        }
    }

    public void onPostTick(Collection<Set<SyncBaseItem>> colliding, Set<SyncBaseItem> alreadySentSyncBaseItems) {
        try {
            if (!colliding.isEmpty()) {
                ringBuffer.publishEvent((event, sequence) -> event.setO(colliding));
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void onEvent(SingleHolder<Collection<Set<SyncBaseItem>>> event, long sequence, boolean endOfBatch) {
        try {
            event.getO().forEach(this::handleCollidingSyncItems);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void handleCollidingSyncItems(Set<SyncBaseItem> collidingSyncItems) {
        synchronized (lastSents) {
            Long lastSent = lastSents.get(collidingSyncItems);
            if (lastSent == null || lastSent + DE_BOUNCING < System.currentTimeMillis()) {
                waitingSyncBaseItems.remove(collidingSyncItems);
                sendAndAdd(collidingSyncItems);
            } else {
                waitingSyncBaseItems.put(collidingSyncItems, lastSent);
            }
        }
    }

    private void periodicRunner() {
        try {
            synchronized (lastSents) {
                waitingSyncBaseItems.entrySet().removeIf(entry -> {
                    if (entry.getValue() + DE_BOUNCING < System.currentTimeMillis()) {
                        sendAndAdd(entry.getKey());
                        return true;
                    } else {
                        return false;
                    }
                });
                lastSents.entrySet().removeIf(entry -> entry.getValue() + DE_BOUNCING < System.currentTimeMillis());
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Deprecated
    private void sendAndAdd(Set<SyncBaseItem> collidingSyncItems) {
        collidingSyncItems.forEach(syncItem -> clientGameConnectionService.sendSyncBaseItem(syncItem));
        synchronized (lastSents) {
            lastSents.put(collidingSyncItems, System.currentTimeMillis());
        }
    }
}
