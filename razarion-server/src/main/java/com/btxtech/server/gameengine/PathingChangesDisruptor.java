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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * on 26.02.2018.
 */
@Singleton
public class PathingChangesDisruptor implements EventHandler<SingleHolder<Set<SyncBaseItem>>> {
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
    private RingBuffer<SingleHolder<Set<SyncBaseItem>>> ringBuffer;
    private final Map<SyncBaseItem, Long> lastSents = new HashMap<>();
    private Map<SyncBaseItem, Long> waitingSyncBaseItems = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        try {
            scheduleExecutor.scheduleAtFixedRate(this::periodicRunner, PERIODIC_RUNNER_PERIOD, PERIODIC_RUNNER_PERIOD, TimeUnit.MILLISECONDS);
            Disruptor<SingleHolder<Set<SyncBaseItem>>> disruptor = new Disruptor<>(SingleHolder::new, 512, managedThreadFactory);
            disruptor.handleEventsWith(this);
            disruptor.start();
            ringBuffer = disruptor.getRingBuffer();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
        }
    }

    public void onChanges(Set<SyncBaseItem> syncBaseItems) {
        try {
            if (syncBaseItems.isEmpty()) {
                return;
            }
            ringBuffer.publishEvent((event, sequence) -> event.setO(syncBaseItems));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void onEvent(SingleHolder<Set<SyncBaseItem>> event, long sequence, boolean endOfBatch) {
        try {
            for (SyncBaseItem syncBaseItem : event.getO()) {
                synchronized (lastSents) {
                    Long lastSent = lastSents.get(syncBaseItem);
                    if (lastSent == null) {
                        sendAndAdd(syncBaseItem);
                    } else {
                        if (lastSent + DE_BOUNCING < System.currentTimeMillis()) {
                            sendAndAdd(syncBaseItem);
                        } else {
                            waitingSyncBaseItems.put(syncBaseItem, System.currentTimeMillis());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
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

    private void sendAndAdd(SyncBaseItem syncBaseItem) {
        synchronized (lastSents) {
            clientGameConnectionService.sendSyncBaseItem(syncBaseItem);
            lastSents.put(syncBaseItem, System.currentTimeMillis());
        }
    }
}
