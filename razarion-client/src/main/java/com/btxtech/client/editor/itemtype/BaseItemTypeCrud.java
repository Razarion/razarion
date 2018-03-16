package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.ItemTypeProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 24.08.2016.
 */
@ApplicationScoped
public class BaseItemTypeCrud extends AbstractCrudeEditor<BaseItemType> {
    // private Logger logger = Logger.getLogger(BaseItemTypeCrud.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ItemTypeProvider> provider;
    @Inject
    private ItemTypeService itemTypeService;

    @Override
    public void create() {
        provider.call((RemoteCallback<BaseItemType>) baseItemType -> {
            itemTypeService.overrideBaseItemType(baseItemType);
            fire();
            fireSelection(baseItemType.createObjectNameId());
        }, exceptionHandler.restErrorHandler("BaseItemTypeCrud.createBaseItemType failed: ")).createBaseItemType();
    }

    @Override
    public void delete(BaseItemType baseItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteBaseItemType(baseItemType);
            fire();
        }, exceptionHandler.restErrorHandler("BaseItemTypeCrud.deleteBaseItemType failed: ")).deleteBaseItemType(baseItemType.getId());
    }

    @Override
    public void save(BaseItemType baseItemType) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("BaseItemTypeCrud.updateBaseItemType failed: ")).updateBaseItemType(baseItemType);
    }

    @Override
    public void reload() {
        provider.call((RemoteCallback<List<BaseItemType>>) baseItemTypes -> {
            itemTypeService.setBaseItemTypes(baseItemTypes);
            fire();
            fireChange(baseItemTypes);
        }, exceptionHandler.restErrorHandler("BaseItemTypeCrud.readBaseItemTypes failed: ")).readBaseItemTypes();
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
