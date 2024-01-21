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
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.BabylonResourceItem;
import com.btxtech.uiservice.renderer.MarkerConfig;
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
    private BabylonRendererService babylonRendererService;
    private final Map<Integer, SyncResourceItemSimpleDto> resources = new HashMap<>();
    private SyncStaticItemSetPositionMonitor syncStaticItemSetPositionMonitor;
    private final Map<Integer, BabylonResourceItem> babylonResourceItems = new HashMap<>();
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;
    private BabylonResourceItem selectedBabylonResourceItem;
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
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.setInvisible(null,null);
        }
        DecimalPosition viewFiledCenter = viewFieldAabb.center();
        synchronized (resources) {
            Set<Integer> unused = new HashSet<>(babylonResourceItems.keySet());
            resources.forEach((id, syncResourceItemSimpleDto) -> {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId());
                if (viewFieldAabb.adjoinsCircleExclusive(syncResourceItemSimpleDto.getPosition2d(), resourceItemType.getRadius())) {
                    BabylonResourceItem visibleResource = babylonResourceItems.get(id);
                    if (visibleResource == null) {
                        visibleResource = babylonRendererService.createBabylonResourceItem(id, resourceItemType);
                        visibleResource.setPosition(syncResourceItemSimpleDto.getPosition3d());
                        visibleResource.updatePosition();
                        babylonResourceItems.put(id, visibleResource);
                        if (syncStaticItemSetPositionMonitor != null) {
                            syncStaticItemSetPositionMonitor.addVisible(visibleResource);
                        }
                    } else {
                        unused.remove(id);
                    }
                } else {
                    BabylonResourceItem visibleResource = babylonResourceItems.remove(id);
                    if (visibleResource != null) {
                        if (syncStaticItemSetPositionMonitor != null) {
                            syncStaticItemSetPositionMonitor.removeVisible(visibleResource);
                        }
                        visibleResource.dispose();
                        unused.remove(id);
                    }

                    if (syncStaticItemSetPositionMonitor != null) {
                        syncStaticItemSetPositionMonitor.setInvisible(syncResourceItemSimpleDto, viewFiledCenter);
                    }
                }
            });
            unused.forEach(id -> {
                BabylonResourceItem toRemove = babylonResourceItems.remove(id);
                if (syncStaticItemSetPositionMonitor != null) {
                    syncStaticItemSetPositionMonitor.removeVisible(toRemove);
                }
                toRemove.dispose();
            });
            if (syncStaticItemSetPositionMonitor != null) {
                syncStaticItemSetPositionMonitor.handleOutOfView(viewFiledCenter);
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
        return new SyncItemState(syncResourceItemSimpleDto.getId(), syncResourceItemSimpleDto.getPosition2d(), syncResourceItemSimpleDto.getPosition3d(), itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncStaticItemSetPositionMonitor createSyncItemSetPositionMonitor(MarkerConfig markerConfig) {
        if (syncStaticItemSetPositionMonitor != null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() syncStaticItemSetPositionMonitor != null");
        }
        if (viewField == null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() viewField != null");
        }
        syncStaticItemSetPositionMonitor = new SyncStaticItemSetPositionMonitor(babylonRendererService, markerConfig,() -> syncStaticItemSetPositionMonitor = null);
        return syncStaticItemSetPositionMonitor;
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        updateBabylonResourceItems();
    }

    public SyncResourceItemSimpleDto getSyncResourceItemSimpleDto4IdPlayback(int resourceItemId) {
        return resources.get(resourceItemId);
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectedBabylonResourceItem != null) {
            selectedBabylonResourceItem.select(false);
            selectedBabylonResourceItem = null;
        }
        if (selectionEvent.getType() == SelectionEvent.Type.OTHER && selectionEvent.getSelectedOther() instanceof SyncResourceItemSimpleDto) {
            selectedBabylonResourceItem = babylonResourceItems.get(selectionEvent.getSelectedOther().getId());
            if (selectedBabylonResourceItem != null) {
                selectedBabylonResourceItem.select(true);
            }
        }
    }

    public void onHover(SyncResourceItemSimpleDto syncItem) {
        if (hoverBabylonResourceItem == null && syncItem != null) {
            hoverBabylonResourceItem = babylonResourceItems.get(syncItem.getId());
            if (hoverBabylonResourceItem != null) {
                hoverBabylonResourceItem.hover(true);
            }
        } else if (hoverBabylonResourceItem != null && syncItem == null) {
            hoverBabylonResourceItem.hover(false);
            hoverBabylonResourceItem = null;
        } else if (hoverBabylonResourceItem != null && hoverBabylonResourceItem.getId() != syncItem.getId()) {
            hoverBabylonResourceItem.hover(false);
            hoverBabylonResourceItem = babylonResourceItems.get(syncItem.getId());
            if (hoverBabylonResourceItem != null) {
                hoverBabylonResourceItem.hover(true);
            }
        }
    }
}
