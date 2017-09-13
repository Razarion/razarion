package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.01.2017.
 */
@ApplicationScoped
public class ResourceUiService implements ViewService.ViewFieldListener {
    private Logger logger = Logger.getLogger(ResourceUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private ViewService viewService;
    private final Map<Integer, SyncResourceItemSimpleDto> resources = new HashMap<>();
    private final MapList<ResourceItemType, ModelMatrices> resourceModelMatrices = new MapList<>();
    private SyncResourceItemSetPositionMonitor syncResourceItemSetPositionMonitor;

    @PostConstruct
    public void init() {
        viewService.addViewFieldListeners(this);
    }

    public void clear() {
        resources.clear();
        resourceModelMatrices.clear();
        syncResourceItemSetPositionMonitor = null;
    }

    public void addResource(SyncResourceItemSimpleDto syncResourceItem) {
        synchronized (resources) {
            if (resources.put(syncResourceItem.getId(), syncResourceItem) != null) {
                logger.warning("Resource already exists: " + syncResourceItem);
            }
        }
        if (syncResourceItemSetPositionMonitor != null) {
            syncResourceItemSetPositionMonitor.add(syncResourceItem);
        }
        setupModelMatrices();
    }

    public void removeResource(int id) {
        SyncResourceItemSimpleDto resource;
        synchronized (resources) {
            resource = resources.remove(id);
            if (resource == null) {
                throw new IllegalStateException("No resource for id: " + id);
            }
            selectionHandler.resourceItemRemove(resource);
        }
        if (syncResourceItemSetPositionMonitor != null) {
            syncResourceItemSetPositionMonitor.remove(resource);
        }
        setupModelMatrices();
    }

    public SyncResourceItemSimpleDto findItemAtPosition(DecimalPosition decimalPosition) {
        synchronized (resources) {
            for (SyncResourceItemSimpleDto resource : resources.values()) {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resource.getItemTypeId());
                if (resource.getPosition2d().getDistance(decimalPosition) <= resourceItemType.getRadius()) {
                    return resource;
                }
            }
        }
        return null;
    }

    public Collection<SyncResourceItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncResourceItemSimpleDto> result = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItemSimpleDto resource : resources.values()) {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resource.getItemTypeId());
                if (rectangle.adjoinsCircleExclusive(resource.getPosition2d(), resourceItemType.getRadius())) {
                    result.add(resource);
                }
            }
        }
        return result;
    }

    private Collection<SyncResourceItemSimpleDto> findResourceItemWithPlace(int resourceTypeId, PlaceConfig resourceSelection) {
        Collection<SyncResourceItemSimpleDto> result = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItemSimpleDto resource : resources.values()) {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resource.getItemTypeId());
                if (resourceItemType.getId() != resourceTypeId) {
                    continue;
                }
                if (resourceSelection.checkInside(resource.getPosition2d(), resourceItemType.getRadius())) {
                    result.add(resource);
                }
            }
        }
        return result;
    }

    public List<ModelMatrices> provideModelMatrices(ResourceItemType resourceItemType) {
        return resourceModelMatrices.get(resourceItemType);
    }

    private void setupModelMatrices() {
        synchronized (resourceModelMatrices) {
            resourceModelMatrices.clear();
            Rectangle2D aabb = viewService.getCurrentAabb();
            if(aabb == null) {
                return;
            }
            for (SyncResourceItemSimpleDto resourceItem : resources.values()) {
                if (aabb.contains(resourceItem.getPosition2d())) {
                    ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resourceItem.getItemTypeId());
                    resourceModelMatrices.put(resourceItemType, new ModelMatrices(resourceItem.getModel(), nativeMatrixFactory));
                }
            }
        }
    }

    public SyncItemMonitor monitorResourceItemWithPlace(int toCollectFormId, PlaceConfig resourceSelection) {
        Collection<SyncResourceItemSimpleDto> syncResourceItems = findResourceItemWithPlace(toCollectFormId, resourceSelection);
        if (syncResourceItems.isEmpty()) {
            return null;
        } else {
            return monitorSyncResourceItem(CollectionUtils.getFirst(syncResourceItems));
        }
    }

    public SyncItemMonitor monitorSyncResourceItem(SyncResourceItemSimpleDto syncResourceItemSimpleDto) {
        // No monitoring is done, since resources do not move
        return new SyncItemState(syncResourceItemSimpleDto, null, itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncResourceItemSetPositionMonitor createSyncItemSetPositionMonitor() {
        if (syncResourceItemSetPositionMonitor != null) {
            throw new IllegalStateException("BaseItemUiService.createSyncItemSetPositionMonitor() syncResourceItemSetPositionMonitor != null");
        }
        syncResourceItemSetPositionMonitor = new SyncResourceItemSetPositionMonitor(resources.values(), viewService.getCurrentViewField(), () -> syncResourceItemSetPositionMonitor = null);
        return syncResourceItemSetPositionMonitor;
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        setupModelMatrices();
        if (syncResourceItemSetPositionMonitor != null) {
            syncResourceItemSetPositionMonitor.onViewChanged(viewField);
        }
    }
}
