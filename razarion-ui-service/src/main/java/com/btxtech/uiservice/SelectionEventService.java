package com.btxtech.uiservice;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

@Singleton
public class SelectionEventService {
    private final Collection<Consumer<SelectionEvent>> selectionEventConsumers = new ArrayList<>();

    @Inject
    public SelectionEventService() {
    }

    public void receiveSelectionEvent(Consumer<SelectionEvent> selectionEventConsumer) {
        selectionEventConsumers.add(selectionEventConsumer);
    }

    public void fire(SelectionEvent selectionEvent) {
        selectionEventConsumers.forEach(consumer -> consumer.accept(selectionEvent));
    }
}
