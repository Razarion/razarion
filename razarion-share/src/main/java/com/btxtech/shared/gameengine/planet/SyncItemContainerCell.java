package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.planet.model.SyncItem;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 17.03.2018.
 */
public class SyncItemContainerCell {
    private Collection<SyncItem> items = new ArrayList<>();

    public void add(SyncItem syncItem) {
        items.add(syncItem);
    }

    public void remove(SyncItem syncItem) {
        items.remove(syncItem);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Collection<SyncItem> get() {
        return items;
    }
}
