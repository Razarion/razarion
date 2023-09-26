package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.renderer.BabylonResourceItem;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.ViewField;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.01.2017.
 */
@ApplicationScoped
public class ResourceUiService {
    private final Logger logger = Logger.getLogger(ResourceUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private BabylonRendererService threeJsRendererService;
    private final Map<Integer, SyncResourceItemSimpleDto> resources = new HashMap<>();
    private SyncStaticItemSetPositionMonitor syncStaticItemSetPositionMonitor;
    private final Map<Integer, BabylonResourceItem> babylonResourceItem = new HashMap<>();
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;
    private BabylonResourceItem selectedBabylonBaseItem;
    private BabylonResourceItem hoverBabylonResourceItem;

    public void clear() {
        resources.clear();
        syncStaticItemSetPositionMonitor = null;
    }

    public void addResource(SyncResourceItemSimpleDto syncResourceItem) {
        synchronized (resources) {
            if (resources.put(syncResourceItem.getId(), syncResourceItem) != null) {
                logger.warning("Resource already exists: " + syncResourceItem);
            }
        }
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.add(syncResourceItem);
        }
        updateBabylonResourceItems();
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
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.remove(resource);
        }
        updateBabylonResourceItems();
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

    private void updateBabylonResourceItems() {
        if (viewFieldAabb == null) {
            return;
        }
        synchronized (resources) {
            Set<Integer> unused = new HashSet<>(babylonResourceItem.keySet());
            resources.forEach((id, syncResourceItemSimpleDto) -> {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId());
                if (viewFieldAabb.adjoinsCircleExclusive(syncResourceItemSimpleDto.getPosition2d(), resourceItemType.getRadius())) {
                    BabylonResourceItem visibleResource = babylonResourceItem.get(id);
                    if (visibleResource == null) {
                        visibleResource = threeJsRendererService.createBabylonResourceItem(id, resourceItemType);
                        visibleResource.setPosition(syncResourceItemSimpleDto.getPosition3d());
                        visibleResource.updatePosition();
                        babylonResourceItem.put(id, visibleResource);
                    } else {
                        unused.remove(id);
                    }
                } else {
                    BabylonResourceItem visibleResource = babylonResourceItem.remove(id);
                    if (visibleResource != null) {
                        visibleResource.dispose();
                        unused.remove(id);
                    }
                }
            });
            unused.forEach(id -> babylonResourceItem.remove(id).dispose());
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
        return new SyncItemState(syncResourceItemSimpleDto.getId(), syncResourceItemSimpleDto.getPosition2d(), syncResourceItemSimpleDto.getPosition3d(), itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncStaticItemSetPositionMonitor createSyncItemSetPositionMonitor() {
        if (syncStaticItemSetPositionMonitor != null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() syncStaticItemSetPositionMonitor != null");
        }
        if (viewField == null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() viewField != null");
        }
        syncStaticItemSetPositionMonitor = new SyncStaticItemSetPositionMonitor(resources.values(), viewField, () -> syncStaticItemSetPositionMonitor = null);
        return syncStaticItemSetPositionMonitor;
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        updateBabylonResourceItems();
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.onViewChanged(viewField);
        }
    }

    public SyncResourceItemSimpleDto getSyncResourceItemSimpleDto4IdPlayback(int resourceItemId) {
        return resources.get(resourceItemId);
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectedBabylonBaseItem != null) {
            selectedBabylonBaseItem.select(false);
            selectedBabylonBaseItem = null;
        }
        if (selectionEvent.getType() == SelectionEvent.Type.OTHER && selectionEvent.getSelectedOther() instanceof SyncResourceItemSimpleDto) {
            selectedBabylonBaseItem = babylonResourceItem.get(selectionEvent.getSelectedOther().getId());
            if (selectedBabylonBaseItem != null) {
                selectedBabylonBaseItem.select(true);
            }
        }
    }

    public void onHover(SyncResourceItemSimpleDto syncItem) {
        if (hoverBabylonResourceItem == null && syncItem != null) {
            hoverBabylonResourceItem = babylonResourceItem.get(syncItem.getId());
            if (hoverBabylonResourceItem != null) {
                hoverBabylonResourceItem.hover(true);
            }
        } else if (hoverBabylonResourceItem != null && syncItem == null) {
            hoverBabylonResourceItem.hover(false);
            hoverBabylonResourceItem = null;
        } else if (hoverBabylonResourceItem != null && hoverBabylonResourceItem.getId() != syncItem.getId()) {
            hoverBabylonResourceItem.hover(false);
            hoverBabylonResourceItem = babylonResourceItem.get(syncItem.getId());
            if (hoverBabylonResourceItem != null) {
                hoverBabylonResourceItem.hover(true);
            }
        }
    }
}
