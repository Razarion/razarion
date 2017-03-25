package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 24.08.2016.
 */
public abstract class AbstractCrudeEditor<T> implements CrudEditor<T>, CrudEditor.ChangeListener<T> {
    private Collection<LoadedListener> loadListeners = new ArrayList<>();
    private Collection<SelectionListener> selectionListeners = new ArrayList<>();
    private Collection<ChangeListener<T>> changeListeners = new ArrayList<>();

    protected abstract List<ObjectNameId> setupObjectNameIds();

    @Override
    public void monitor(LoadedListener loadedListener) {
        loadListeners.add(loadedListener);
        loadedListener.onLoaded(setupObjectNameIds());
    }

    @Override
    public void removeMonitor(LoadedListener loadedListener) {
        loadListeners.remove(loadedListener);
    }

    @Override
    public void monitorSelection(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    @Override
    public void removeSelectionMonitor(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    @Override
    public void addChangeListener(ChangeListener<T> changeListener) {
        changeListeners.add(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener<T> changeListener) {
        changeListeners.remove(changeListener);
    }

    /**
     * Fire for reate, delete and name change "structure-change". Everything which influences the parent combo-box.
     */
    protected void fire() {
        List<ObjectNameId> objectNameIds = setupObjectNameIds();
        for (LoadedListener loadedListener : loadListeners) {
            loadedListener.onLoaded(objectNameIds);
        }
    }

    /**
     * Fire if parent combo-box selection changed. Most fired by a creation to select the newly created
     */
    protected void fireSelection(ObjectNameId selection) {
        for (SelectionListener selectionListener : selectionListeners) {
            selectionListener.onSelect(selection);
        }
    }

    /**
     * Fire if values have changed
     */
    protected void fireChange(T t) {
        for (ChangeListener<T> changeListener : changeListeners) {
            changeListener.onChange(t);
        }
        onChange(t);
    }

    /**
     * Fire if values have changed
     */
    protected void fireChange(Collection<T> changes) {
        changes.forEach(this::fireChange);
    }

    @Override
    public void onChange(T t) {
        // Override in subclasses if needed
    }
}
