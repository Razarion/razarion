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

    protected abstract List<ObjectNameId> setupObjectNameIds();

    @Override
    public void monitor(Consumer<List<ObjectNameId>> observer) {
        observers.add(observer);
        observer.accept(setupObjectNameIds());
    }

    @Override
    public void removeMonitor(Consumer<List<ObjectNameId>> observer) {
        observers.remove(observer);
    }

    protected void fire() {
        List<ObjectNameId> objectNameIds = setupObjectNameIds();
        for (Consumer<List<ObjectNameId>> observer : observers) {
            observer.accept(objectNameIds);
        }
    }
}
