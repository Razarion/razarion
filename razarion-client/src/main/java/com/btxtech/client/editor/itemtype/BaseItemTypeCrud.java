package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.ItemTypeProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
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
                itemTypeService.override(baseItemType);
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ItemTypeProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).createBaseItemType();
    }

    @Override
    public void delete(BaseItemType baseItemType) {
        provider.call(ignore -> {
            itemTypeService.deleteBaseItemType(baseItemType);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ItemTypeProvider.deleteBaseItemType failed: " + message, throwable);
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
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TerrainElementEditorProvider.createTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).read();
    }

    @Override
    public BaseItemType getInstance(ObjectNameId objectNameId) {
        return (BaseItemType) itemTypeService.getItemType(objectNameId.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return itemTypeService.getItemTypes(BaseItemType.class).stream().map(BaseItemType::createObjectNameId).collect(Collectors.toList());
    }
}
