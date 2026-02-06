package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionEventService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.BabylonResourceItem;
import com.btxtech.uiservice.renderer.MarkerConfig;
import com.btxtech.uiservice.renderer.ViewField;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
@Singleton
public class ResourceUiService {
    private final Logger logger = Logger.getLogger(ResourceUiService.class.getName());
    private final Map<Integer, SyncResourceItemSimpleDto> resources = new HashMap<>();
    private final Map<Integer, BabylonResourceItem> babylonResourceItems = new HashMap<>();
    private final ItemTypeService itemTypeService;
    private final SelectionService selectionService;
    private final BabylonRendererService babylonRendererService;
    private SyncStaticItemSetPositionMonitor syncStaticItemSetPositionMonitor;
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;
    private BabylonResourceItem selectedBabylonResourceItem;
    private Integer selectedOutOfViewId;
    private BabylonResourceItem hoverBabylonResourceItem;

    @Inject
    public ResourceUiService(BabylonRendererService babylonRendererService,
                             SelectionService selectionService,
                             ItemTypeService itemTypeService,
                             SelectionEventService selectionEventService) {
        this.babylonRendererService = babylonRendererService;
        this.selectionService = selectionService;
        this.itemTypeService = itemTypeService;
        selectionEventService.receiveSelectionEvent(this::onSelectionChanged);
    }

    public void clear() {
        resources.clear();
        syncStaticItemSetPositionMonitor = null;
        selectedBabylonResourceItem = null;
        selectedOutOfViewId = null;
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
            selectionService.resourceItemRemove(resource);
        }
        updateBabylonResourceItems();
    }

    public SyncResourceItemSimpleDto getItem4Id(int resourceItemId) {
        synchronized (resources) {
            SyncResourceItemSimpleDto syncResourceItemSimpleDto = resources.get(resourceItemId);
            if (syncResourceItemSimpleDto != null) {
                return syncResourceItemSimpleDto;
            }
        }
        throw new IllegalArgumentException("No SyncResourceItemSimpleDto for " + resourceItemId);
    }

    public Collection<SyncResourceItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncResourceItemSimpleDto> result = new ArrayList<>();
        synchronized (resources) {
            for (SyncResourceItemSimpleDto resource : resources.values()) {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(resource.getItemTypeId());
                if (rectangle.adjoinsCircleExclusive(resource.getPosition().toXY(), resourceItemType.getRadius())) {
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
                if (resourceSelection.checkInside(resource.getPosition().toXY(), resourceItemType.getRadius())) {
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
            syncStaticItemSetPositionMonitor.setInvisibleSyncItem(null, null);
        }
        DecimalPosition viewFiledCenter = viewFieldAabb.center();
        synchronized (resources) {
            Set<Integer> unused = new HashSet<>(babylonResourceItems.keySet());
            resources.forEach((id, syncResourceItemSimpleDto) -> {
                ResourceItemType resourceItemType = itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId());
                if (viewFieldAabb.adjoinsCircleExclusive(syncResourceItemSimpleDto.getPosition().toXY(), resourceItemType.getRadius())) {
                    BabylonResourceItem visibleResource = babylonResourceItems.get(id);
                    if (visibleResource == null) {
                        visibleResource = babylonRendererService.createBabylonResourceItem(id, resourceItemType);
                        visibleResource.setPosition(syncResourceItemSimpleDto.getPosition());
                        babylonResourceItems.put(id, visibleResource);
                        if (syncStaticItemSetPositionMonitor != null) {
                            syncStaticItemSetPositionMonitor.addVisible(visibleResource);
                        }
                        if (id.equals(selectedOutOfViewId)) {
                            selectedOutOfViewId = null;
                            visibleResource.select(true);
                            selectedBabylonResourceItem = visibleResource;
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
                        if (selectedBabylonResourceItem != null && selectedBabylonResourceItem.getId() == id) {
                            selectedBabylonResourceItem = null;
                            selectedOutOfViewId = id;
                        }
                        visibleResource.dispose();
                        unused.remove(id);
                    }

                    if (syncStaticItemSetPositionMonitor != null) {
                        syncStaticItemSetPositionMonitor.setInvisibleSyncItem(syncResourceItemSimpleDto, viewFiledCenter);
                    }
                }
            });
            unused.forEach(id -> {
                BabylonResourceItem toRemove = babylonResourceItems.remove(id);
                if (id.equals(selectedOutOfViewId)) {
                    selectedOutOfViewId = null;
                }
                if (selectedBabylonResourceItem != null && selectedBabylonResourceItem.getId() == id) {
                    selectedBabylonResourceItem = null;
                }
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
        return new SyncItemState(syncResourceItemSimpleDto.getId(), syncResourceItemSimpleDto.getPosition().toXY(), itemTypeService.getResourceItemType(syncResourceItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncStaticItemSetPositionMonitor createSyncItemSetPositionMonitor(MarkerConfig markerConfig) {
        if (syncStaticItemSetPositionMonitor != null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() syncStaticItemSetPositionMonitor != null");
        }
        if (viewField == null) {
            throw new IllegalStateException("ResourceUiService.createSyncItemSetPositionMonitor() viewField != null");
        }
        syncStaticItemSetPositionMonitor = new SyncStaticItemSetPositionMonitor(babylonRendererService, markerConfig, () -> syncStaticItemSetPositionMonitor = null);
        babylonResourceItems.values().forEach(syncStaticItemSetPositionMonitor::addVisible);
        if (babylonResourceItems.isEmpty()) {
            DecimalPosition viewFieldCenter = viewField.calculateCenter();
            synchronized (resources) {
                for (SyncResourceItemSimpleDto resource : resources.values()) {
                    syncStaticItemSetPositionMonitor.setInvisibleSyncItem(resource, viewFieldCenter);
                }
            }
            syncStaticItemSetPositionMonitor.handleOutOfView(viewFieldCenter);
        }
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

    private void onSelectionChanged(SelectionEvent selectionEvent) {
        selectedOutOfViewId = null;
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

    /**
     * Returns the position of the nearest resource to the given position.
     * This searches ALL resources, not just visible ones.
     * Called from Angular/TypeScript.
     */
    @SuppressWarnings("unused") // Called by Angular
    public Vertex getNearestResourcePosition(double fromX, double fromY) {
        DecimalPosition from = new DecimalPosition(fromX, fromY);
        SyncResourceItemSimpleDto nearest = null;
        double minDistance = Double.MAX_VALUE;

        synchronized (resources) {
            for (SyncResourceItemSimpleDto resource : resources.values()) {
                double distance = resource.getPosition().toXY().getDistance(from);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = resource;
                }
            }
        }

        return nearest != null ? nearest.getPosition() : null;
    }

    // Only for tests
    public Map<Integer, SyncResourceItemSimpleDto> getResources() {
        return resources;
    }

    // Only for tests
    public ViewField getViewField() {
        return viewField;
    }
}
