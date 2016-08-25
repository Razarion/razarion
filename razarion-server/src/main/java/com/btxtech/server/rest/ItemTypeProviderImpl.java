package com.btxtech.server.rest;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.ItemTypeProvider;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 25.08.2016.
 */
public class ItemTypeProviderImpl implements ItemTypeProvider {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Override
    public BaseItemType createBaseItemType() {
        try {
            return itemTypePersistence.createBaseItemType();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ItemType> read() {
        try {
            return itemTypePersistence.read();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void updateBaseItemType(BaseItemType baseItemType) {
        try {
            itemTypePersistence.update(baseItemType);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteBaseItemType(int id) {
        try {
            itemTypePersistence.deleteBaseItemType(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
