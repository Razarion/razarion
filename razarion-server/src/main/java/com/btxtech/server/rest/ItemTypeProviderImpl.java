package com.btxtech.server.rest;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.rest.ItemTypeProvider;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
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
    public List<BaseItemType> readBaseItemTypes() {
        try {
            return itemTypePersistence.readBaseItemTypes();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void updateBaseItemType(BaseItemType baseItemType) {
        try {
            itemTypePersistence.updateBaseItemType(baseItemType);
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

    @Override
    public ResourceItemType createResourceItemType() {
        try {
            return itemTypePersistence.createResourceItemType();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ResourceItemType> readResourceItemTypes() {
        try {
            return itemTypePersistence.readResourceItemTypes();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void updateResourceItemType(ResourceItemType resourceItemType) {
        try {
            itemTypePersistence.updateResourceItemType(resourceItemType);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteResourceItemType(int id) {
        try {
            itemTypePersistence.deleteResourceItemType(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

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
