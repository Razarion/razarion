package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.ItemTypeProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 24.08.2016.
 */
@ApplicationScoped
public class ResourceItemTypeCrud extends AbstractCrudeEditor<ResourceItemType> {
    private Logger logger = Logger.getLogger(ResourceItemTypeCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
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
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ResourceItemTypeCrud.createResourceItemType failed: " + message, throwable);
            return false;
        }).createResourceItemType();
    }

    @Override
    public void delete(ResourceItemType resourceItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteResourceItemType(resourceItemType);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ResourceItemTypeCrud.deleteResourceItemType failed: " + message, throwable);
            return false;
        }).deleteResourceItemType(resourceItemType.getId());
    }

    @Override
    public void save(ResourceItemType resourceItemType) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "ResourceItemTypeCrud.updateResourceItemType failed: " + message, throwable);
            return false;
        }).updateResourceItemType(resourceItemType);
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
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ResourceItemTypeCrud.readResourceItemType failed: " + message, throwable);
            return false;
        }).readResourceItemType();
    }

    @Override
    public ResourceItemType getInstance(ObjectNameId objectNameId) {
        return itemTypeService.getResourceItemType(objectNameId.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return itemTypeService.getResourceItemTypes().stream().map(ResourceItemType::createObjectNameId).collect(Collectors.toList());
    }
}
