package com.btxtech.server.persistence.item;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.shared.system.ExceptionHandler;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 01.01.2018.
 */
@Singleton
public class ItemTrackerDisruptor {
    @Resource(name = "DefaultManagedThreadFactory")
    private ManagedThreadFactory managedThreadFactory;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private MongoDbService mongoDbService;
    private RingBuffer<ItemTracking> ringBuffer;

    @PostConstruct
    public void postConstruct() {
        try {
            Disruptor<ItemTracking> disruptor = new Disruptor<>(ItemTracking::new, 512, managedThreadFactory);
            disruptor.handleEventsWith(new ItemTrackingEventHandler());
            disruptor.start();
            ringBuffer = disruptor.getRingBuffer();
            ringBuffer.publishEvent((event, sequence, date) -> {
                event.clean();
                event.setTimeStamp(date);
                event.setType(ItemTracking.Type.SERVER_START);
            }, new Date());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
        }
    }

    public void publishEvent(Consumer<RingBuffer<ItemTracking>> ringBufferConsumer) {
        try {
            ringBufferConsumer.accept(getRingBuffer());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private RingBuffer<ItemTracking> getRingBuffer() {
        if (ringBuffer == null) {
            throw new IllegalStateException("ringBuffer == null");
        }
        return ringBuffer;
    }

    private class ItemTrackingEventHandler implements EventHandler<ItemTracking> {

        @Override
        public void onEvent(ItemTracking event, long sequence, boolean endOfBatch) {
            try {
                mongoDbService.storeObject(event, ItemTracking.class, MongoDbService.CollectionName.SERVER_ITEM_TRACKING);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }
}
