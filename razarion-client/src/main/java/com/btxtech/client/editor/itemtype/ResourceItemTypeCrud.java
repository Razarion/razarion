package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
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
public class ResourceItemTypeCrud extends AbstractCrudeEditor<ResourceItemType> {
    // private Logger logger = Logger.getLogger(ResourceItemTypeCrud.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<ItemTypeProvider> provider;
    @Inject
    private ItemTypeService itemTypeService;

    @Override
    public void create() {
        provider.call(new RemoteCallback<ResourceItemType>() {
            @Override
            public void callback(ResourceItemType resourceItemType) {
                itemTypeService.overrideResourceItemType(resourceItemType);
                fire();
                fireSelection(resourceItemType.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("ResourceItemTypeCrud.createResourceItemType failed: ")).createResourceItemType();
    }

    @Override
    public void delete(ResourceItemType resourceItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteResourceItemType(resourceItemType);
            fire();
        }, exceptionHandler.restErrorHandler("ResourceItemTypeCrud.deleteResourceItemType failed: ")).deleteResourceItemType(resourceItemType.getId());
    }

    @Override
    public void save(ResourceItemType resourceItemType) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("ResourceItemTypeCrud.updateResourceItemType failed: ")).updateResourceItemType(resourceItemType);
    }

    @Override
    public void reload() {
        provider.call(new RemoteCallback<List<ResourceItemType>>() {
            @Override
            public void callback(List<ResourceItemType> resourceItemTypes) {
                itemTypeService.setResourceItemTypes(resourceItemTypes);
                fire();
                fireChange(resourceItemTypes);
            }
        }, exceptionHandler.restErrorHandler("ResourceItemTypeCrud.readResourceItemTypes failed: ")).readResourceItemTypes();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<ResourceItemType> callback) {
        callback.accept(itemTypeService.getResourceItemType(objectNameId.getId()));
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return itemTypeService.getResourceItemTypes().stream().map(ResourceItemType::createObjectNameId).collect(Collectors.toList());
    }
}
