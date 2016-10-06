package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;

import java.util.List;

/**
 * Created by Beat
 * 23.08.2016.
 */
public interface CrudEditor<T> {
    interface LoadedListener {
        void onLoaded(List<ObjectNameId> objectNameIds);
    }

    interface SelectionListener {
        void onSelect(ObjectNameId objectNameId);
    }

    interface ChangeListener<T> {
        void onChange(T t);
    }

    void create();

    void delete(T t);

    void save(T t);

    void reload();

    T getInstance(ObjectNameId id);

    void monitor(LoadedListener loadedListener);

    void removeMonitor(LoadedListener loadedListener);

    void monitorSelection(SelectionListener selectionListener);

    void removeSelectionMonitor(SelectionListener selectionListener);

    void addChangeListener(ChangeListener<T> changeListener);

    void removeChangeListener(ChangeListener<T> changeListener);
}
