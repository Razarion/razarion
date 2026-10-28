package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.uiservice.renderer.BabylonBoxItem;
import com.btxtech.uiservice.renderer.BabylonRendererService;
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
public class BoxUiService {
    private final Logger logger = Logger.getLogger(BoxUiService.class.getName());
    private final Map<Integer, SyncBoxItemSimpleDto> boxes = new HashMap<>();
    private final Map<Integer, BabylonBoxItem> babylonBoxItems = new HashMap<>();
    private final ItemTypeService itemTypeService;
    private final BabylonRendererService babylonRendererService;
    private SyncStaticItemSetPositionMonitor syncStaticItemSetPositionMonitor;
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;
    @Inject
    public BoxUiService(BabylonRendererService babylonRendererService,
                        ItemTypeService itemTypeService) {
        this.babylonRendererService = babylonRendererService;
        this.itemTypeService = itemTypeService;
    }

    public void clear() {
        boxes.clear();
        syncStaticItemSetPositionMonitor = null;
    }

    public void addBox(SyncBoxItemSimpleDto syncBoxItem) {
        synchronized (boxes) {
            SyncBoxItemSimpleDto previous = boxes.put(syncBoxItem.getId(), syncBoxItem);
            if (previous != null) {
                if (previous.getPosition().equals(syncBoxItem.getPosition())) {
                    logger.warning("Box already exists: " + syncBoxItem);
                } else {
                    // Sent again with its real height: it was placed while the far part of the height
                    // map was still loading (GameEngineWorker#onHeightMapComplete).
                    BabylonBoxItem visible = babylonBoxItems.get(syncBoxItem.getId());
                    if (visible != null) {
                        visible.setPosition(syncBoxItem.getPosition());
                    }
                }
            }
        }
        updateBabylonBoxItems();
    }

    public void removeBox(int id) {
        SyncBoxItemSimpleDto box;
        synchronized (boxes) {
            box = boxes.remove(id);
            if (box == null) {
                throw new IllegalStateException("No box for id: " + id);
            }
        }
        updateBabylonBoxItems();
    }


    public SyncBoxItemSimpleDto getItem4Id(int boxItemId) {
        synchronized (boxes) {
            SyncBoxItemSimpleDto syncBoxItemSimpleDto = boxes.get(boxItemId);
            if (syncBoxItemSimpleDto != null) {
                return syncBoxItemSimpleDto;
            }
        }
        throw new IllegalArgumentException("No SyncBoxItemSimpleDto for " + boxItemId);
    }

    public Collection<SyncBoxItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncBoxItemSimpleDto> result = new ArrayList<>();
        synchronized (boxes) {
            for (SyncBoxItemSimpleDto box : boxes.values()) {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(box.getItemTypeId());
                if (rectangle.adjoinsCircleExclusive(box.getPosition().toXY(), boxItemType.getRadius())) {
                    result.add(box);
                }
            }
        }
        return result;
    }

    public SyncItemMonitor monitorSyncBoxItem(SyncBoxItemSimpleDto boxItemSimpleDto) {
        // No monitoring is done, since boxes do not move
        return new SyncItemState(boxItemSimpleDto.getId(), boxItemSimpleDto.getPosition().toXY(), itemTypeService.getBoxItemType(boxItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncStaticItemSetPositionMonitor createSyncItemSetPositionMonitor(MarkerConfig markerConfig) {
        if (syncStaticItemSetPositionMonitor != null) {
            throw new IllegalStateException("BoxUiService.createSyncItemSetPositionMonitor() syncStaticItemSetPositionMonitor != null");
        }
        if (viewField == null) {
            throw new IllegalStateException("BoxUiService.createSyncItemSetPositionMonitor() viewField != null");
        }

        syncStaticItemSetPositionMonitor = new SyncStaticItemSetPositionMonitor(babylonRendererService, markerConfig, () -> syncStaticItemSetPositionMonitor = null);
        babylonBoxItems.values().forEach(syncStaticItemSetPositionMonitor::addVisible);
        if (babylonBoxItems.isEmpty()) {
            DecimalPosition viewFieldCenter = viewField.calculateCenter();
            synchronized (boxes) {
                for (SyncBoxItemSimpleDto box : boxes.values()) {
                    syncStaticItemSetPositionMonitor.setInvisibleSyncItem(box, viewFieldCenter);
                }
            }
            syncStaticItemSetPositionMonitor.handleOutOfView(viewFieldCenter);
        }
        return syncStaticItemSetPositionMonitor;
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        updateBabylonBoxItems();
    }

    private void updateBabylonBoxItems() {
        if (viewFieldAabb == null) {
            return;
        }
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.setInvisibleSyncItem(null, null);
        }
        DecimalPosition viewFiledCenter = viewFieldAabb.center();
        synchronized (boxes) {
            Set<Integer> unused = new HashSet<>(babylonBoxItems.keySet());
            boxes.forEach((id, syncBoxItemSimpleDto) -> {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(syncBoxItemSimpleDto.getItemTypeId());
                if (viewFieldAabb.adjoinsCircleExclusive(syncBoxItemSimpleDto.getPosition().toXY(), boxItemType.getRadius())) {
                    BabylonBoxItem visibleBox = babylonBoxItems.get(id);
                    if (visibleBox == null) {
                        visibleBox = babylonRendererService.createBabylonBoxItem(id, boxItemType);
                        visibleBox.setPosition(syncBoxItemSimpleDto.getPosition());
                        babylonBoxItems.put(id, visibleBox);
                        if (syncStaticItemSetPositionMonitor != null) {
                            syncStaticItemSetPositionMonitor.addVisible(visibleBox);
                        }
                    } else {
                        unused.remove(id);
                    }
                } else {
                    BabylonBoxItem visibleBox = babylonBoxItems.remove(id);
                    if (visibleBox != null) {
                        if (syncStaticItemSetPositionMonitor != null) {
                            syncStaticItemSetPositionMonitor.removeVisible(visibleBox);
                        }
                        visibleBox.removeFromView();
                        unused.remove(id);
                    }

                    if (syncStaticItemSetPositionMonitor != null) {
                        syncStaticItemSetPositionMonitor.setInvisibleSyncItem(syncBoxItemSimpleDto, viewFiledCenter);
                    }
                }
            });
            unused.forEach(id -> {
                BabylonBoxItem toRemove = babylonBoxItems.remove(id);
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

    /**
     * Returns the position of the nearest box to the given position.
     * This searches ALL boxes, not just visible ones - the box tip points at one out of view.
     * Called from Angular/TypeScript.
     */
    @SuppressWarnings("unused") // Called by Angular
    public Vertex getNearestBoxPosition(double fromX, double fromY) {
        DecimalPosition from = new DecimalPosition(fromX, fromY);
        SyncBoxItemSimpleDto nearest = null;
        double minDistance = Double.MAX_VALUE;

        synchronized (boxes) {
            for (SyncBoxItemSimpleDto box : boxes.values()) {
                double distance = box.getPosition().toXY().getDistance(from);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = box;
                }
            }
        }

        return nearest != null ? nearest.getPosition() : null;
    }

    // Only for tests
    public Map<Integer, SyncBoxItemSimpleDto> getBoxes() {
        return boxes;
    }
}
