package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 23.08.2016.
 */
public interface CrudEditor<T> {
    void monitor(Consumer<List<ObjectNameId>> consumer);

    void removeMonitor(Consumer<List<ObjectNameId>> consumer);

    void create();

    void delete(T t);

    void save(T t);

    void reload();

    T getInstance(ObjectNameId id);

    void monitorSelection(Consumer<ObjectNameId> callback);

    void removeSelectionMonitor(Consumer<ObjectNameId> callback);
}
