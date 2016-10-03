package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 24.08.2016.
 */
public abstract class AbstractCrudeEditor<T> implements CrudEditor<T> {
    private Collection<Consumer<List<ObjectNameId>>> observers = new ArrayList<>();
    private Collection<Consumer<ObjectNameId>> callbackSelection = new ArrayList<>();

    protected abstract List<ObjectNameId> setupObjectNameIds();

    @Override
    public void monitor(Consumer<List<ObjectNameId>> observer) {
        observers.add(observer);
        observer.accept(setupObjectNameIds());
    }

    @Override
    public void monitorSelection(Consumer<ObjectNameId> callback) {
        callbackSelection.add(callback);
    }

    @Override
    public void removeMonitor(Consumer<List<ObjectNameId>> observer) {
        observers.remove(observer);
    }

    @Override
    public void removeSelectionMonitor(Consumer<ObjectNameId> callback) {
        callbackSelection.remove(callback);
    }

    protected void fire() {
        List<ObjectNameId> objectNameIds = setupObjectNameIds();
        for (Consumer<List<ObjectNameId>> observer : observers) {
            observer.accept(objectNameIds);
        }
    }

    protected void fireSelection(ObjectNameId selection) {
        for (Consumer<ObjectNameId> observer : callbackSelection) {
            observer.accept(selection);
        }
    }


}
