package com.btxtech.shared.gameengine.planet;


import com.btxtech.shared.gameengine.datatypes.syncobject.SyncItem;

/**
 * User: beat
 * Date: 07.10.2011
 * Time: 14:54:10
 */
public interface ItemIteratorHandler<T> {
    /**
     * Is called for every SyncItem
     *
     * @param syncItem syncItem
     * @return null if the iteration shall continue T if the iteration shall stop
     */
    T handleItem(SyncItem syncItem);
}
