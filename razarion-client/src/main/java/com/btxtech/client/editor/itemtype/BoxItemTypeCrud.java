package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.rest.ItemTypeProvider;
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
public class BoxItemTypeCrud extends AbstractCrudeEditor<BoxItemType> {
    private Logger logger = Logger.getLogger(BoxItemTypeCrud.class.getName());
    @Inject
    private Caller<ItemTypeProvider> provider;
    @Inject
    private ItemTypeService itemTypeService;

    @Override
    public void create() {
        provider.call((RemoteCallback<BoxItemType>) boxItemType -> {
            itemTypeService.overrideBoxItemType(boxItemType);
            fire();
            fireSelection(boxItemType.createObjectNameId());
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BoxItemTypeCrud.createBoxItemType failed: " + message, throwable);
            return false;
        }).createBoxItemType();
    }

    @Override
    public void delete(BoxItemType boxItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteBoxItemType(boxItemType);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BoxItemTypeCrud.deleteBoxItemType failed: " + message, throwable);
            return false;
        }).deleteBoxItemType(boxItemType.getId());
    }

    @Override
    public void save(BoxItemType boxItemType) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "BoxItemTypeCrud.updateBoxItemType failed: " + message, throwable);
            return false;
        }).updateBoxItemType(boxItemType);
    }

    @Override
    public void reload() {
        provider.call((RemoteCallback<List<BoxItemType>>) boxItemTypes -> {
            itemTypeService.setBoxItemTypes(boxItemTypes);
            fire();
            fireChange(boxItemTypes);
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "BoxItemTypeCrud.readBoxItemType failed: " + message, throwable);
            return false;
        }).readBoxItemTypes();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<BoxItemType> callback) {
        callback.accept(itemTypeService.getBoxItemType(objectNameId.getId()));
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return itemTypeService.getBoxItemTypes().stream().map(BoxItemType::createObjectNameId).collect(Collectors.toList());
    }
}
