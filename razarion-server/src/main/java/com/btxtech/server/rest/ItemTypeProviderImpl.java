package com.btxtech.server.rest;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.rest.ItemTypeProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 25.08.2016.
 */
@Deprecated
public class ItemTypeProviderImpl implements ItemTypeProvider {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Override
    public BoxItemType createBoxItemType() {
        try {
            return itemTypePersistence.createBoxItemType();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<BoxItemType> readBoxItemTypes() {
        try {
            return itemTypePersistence.readBoxItemTypes();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteBoxItemType(int id) {
        try {
            itemTypePersistence.deleteBoxItemType(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void updateBoxItemType(BoxItemType boxItemType) {
        try {
            itemTypePersistence.updateBoxItemType(boxItemType);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
