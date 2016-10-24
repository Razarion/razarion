package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.rest.ItemTypeProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 24.08.2016.
 */
@ApplicationScoped
public class BaseItemTypeCrud extends AbstractCrudeEditor<BaseItemType> {
    private Logger logger = Logger.getLogger(BaseItemTypeCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<ItemTypeProvider> provider;
    @Inject
    private ItemTypeService itemTypeService;

    @Override
    public void create() {
        provider.call(new RemoteCallback<BaseItemType>() {
            @Override
            public void callback(BaseItemType baseItemType) {
                itemTypeService.overrideBaseItemType(baseItemType);
                fire();
                fireSelection(baseItemType.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BaseItemTypeCrud.createBaseItemType failed: " + message, throwable);
            return false;
        }).createBaseItemType();
    }

    @Override
    public void delete(BaseItemType baseItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteBaseItemType(baseItemType);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BaseItemTypeCrud.deleteBaseItemType failed: " + message, throwable);
            return false;
        }).deleteBaseItemType(baseItemType.getId());
    }

    @Override
    public void save(BaseItemType baseItemType) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "BaseItemTypeCrud.updateBaseItemType failed: " + message, throwable);
            return false;
        }).updateBaseItemType(baseItemType);
    }

    @Override
    public void reload() {
        provider.call(new RemoteCallback<List<BaseItemType>>() {
            @Override
            public void callback(List<BaseItemType> baseItemTypes) {
                itemTypeService.setBaseItemTypes(baseItemTypes);
                fire();
                fireChange(baseItemTypes);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BaseItemTypeCrud.readBaseItemType failed: " + message, throwable);
            return false;
        }).readBaseItemType();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<BaseItemType> callback) {
        callback.accept(itemTypeService.getBaseItemType(objectNameId.getId()));
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return itemTypeService.getBaseItemTypes().stream().map(BaseItemType::createObjectNameId).collect(Collectors.toList());
    }
}
