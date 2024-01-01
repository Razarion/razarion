package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.renderer.BabylonBoxItem;
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
public class BoxUiService {
    private final Logger logger = Logger.getLogger(BoxUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private BabylonRendererService babylonRendererService;
    private final Map<Integer, SyncBoxItemSimpleDto> boxes = new HashMap<>();
    private SyncStaticItemSetPositionMonitor syncStaticItemSetPositionMonitor;
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;
    private final Map<Integer, BabylonBoxItem> babylonBoxItems = new HashMap<>();
    private BabylonBoxItem selectedBabylonBoxItem;
    private BabylonBoxItem hoverBabylonBoxItem;

    public void clear() {
        boxes.clear();
        syncStaticItemSetPositionMonitor = null;
    }

    public void addBox(SyncBoxItemSimpleDto syncBoxItem) {
        synchronized (boxes) {
            if (boxes.put(syncBoxItem.getId(), syncBoxItem) != null) {
                logger.warning("Box already exists: " + syncBoxItem);
            }
        }
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.add(syncBoxItem);
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
            selectionHandler.boxItemRemove(box);
        }
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.remove(box);
        }
        updateBabylonBoxItems();
    }

    public SyncBoxItemSimpleDto findItemAtPosition(DecimalPosition decimalPosition) {
        synchronized (boxes) {
            for (SyncBoxItemSimpleDto box : boxes.values()) {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(box.getItemTypeId());
                if (box.getPosition2d().getDistance(decimalPosition) <= boxItemType.getRadius()) {
                    return box;
                }
            }
        }
        return null;
    }

    public Collection<SyncBoxItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncBoxItemSimpleDto> result = new ArrayList<>();
        synchronized (boxes) {
            for (SyncBoxItemSimpleDto box : boxes.values()) {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(box.getItemTypeId());
                if (rectangle.adjoinsCircleExclusive(box.getPosition2d(), boxItemType.getRadius())) {
                    result.add(box);
                }
            }
        }
        return result;
    }

    private SyncBoxItemSimpleDto findFirstBoxItem(int boxItemTypeId) {
        synchronized (boxes) {
            for (SyncBoxItemSimpleDto box : boxes.values()) {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(box.getItemTypeId());
                if (boxItemType.getId() == boxItemTypeId) {
                    return box;
                }
            }
        }
        return null;
    }

    public SyncBoxItemSimpleDto getSyncBoxItem(int id) {
        synchronized (boxes) {
            SyncBoxItemSimpleDto box = boxes.get(id);
            if (box == null) {
                throw new IllegalArgumentException("No box for id: " + id);
            }
            return box;
        }
    }

    public SyncItemMonitor monitorFirstBoxItem(int boxItemTypeId) {
        SyncBoxItemSimpleDto boxItemSimpleDto = findFirstBoxItem(boxItemTypeId);
        if (boxItemSimpleDto != null) {
            return monitorSyncBoxItem(boxItemSimpleDto);
        } else {
            return null;
        }
    }

    public SyncItemMonitor monitorSyncBoxItem(SyncBoxItemSimpleDto boxItemSimpleDto) {
        // No monitoring is done, since boxes do not move
        return new SyncItemState(boxItemSimpleDto.getId(), boxItemSimpleDto.getPosition2d(), boxItemSimpleDto.getPosition3d(), itemTypeService.getBoxItemType(boxItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }

    public SyncStaticItemSetPositionMonitor createSyncItemSetPositionMonitor() {
        if (syncStaticItemSetPositionMonitor != null) {
            throw new IllegalStateException("BoxUiService.createSyncItemSetPositionMonitor() syncStaticItemSetPositionMonitor != null");
        }
        syncStaticItemSetPositionMonitor = new SyncStaticItemSetPositionMonitor(boxes.values(), viewField, () -> syncStaticItemSetPositionMonitor = null);
        return syncStaticItemSetPositionMonitor;
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        updateBabylonBoxItems();
        if (syncStaticItemSetPositionMonitor != null) {
            syncStaticItemSetPositionMonitor.onViewChanged(viewField);
        }
    }

    public SyncBoxItemSimpleDto getSyncBoxItemSimpleDto4IdPlayback(int syncBoxId) {
        return boxes.get(syncBoxId);
    }

    private void updateBabylonBoxItems() {
        if (viewFieldAabb == null) {
            return;
        }
        synchronized (boxes) {
            Set<Integer> unused = new HashSet<>(babylonBoxItems.keySet());
            boxes.forEach((id, syncBoxItemSimpleDto) -> {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(syncBoxItemSimpleDto.getItemTypeId());
                if (viewFieldAabb.adjoinsCircleExclusive(syncBoxItemSimpleDto.getPosition2d(), boxItemType.getRadius())) {
                    BabylonBoxItem visibleBox = babylonBoxItems.get(id);
                    if (visibleBox == null) {
                        visibleBox = babylonRendererService.createBabylonBoxItem(id, boxItemType);
                        visibleBox.setPosition(syncBoxItemSimpleDto.getPosition3d());
                        visibleBox.updatePosition();
                        babylonBoxItems.put(id, visibleBox);
                    } else {
                        unused.remove(id);
                    }
                } else {
                    BabylonBoxItem visibleBox = babylonBoxItems.remove(id);
                    if (visibleBox != null) {
                        visibleBox.dispose();
                        unused.remove(id);
                    }
                }
            });
            unused.forEach(id -> babylonBoxItems.remove(id).dispose());
        }
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectedBabylonBoxItem != null) {
            selectedBabylonBoxItem.select(false);
            selectedBabylonBoxItem = null;
        }
        if (selectionEvent.getType() == SelectionEvent.Type.OTHER && selectionEvent.getSelectedOther() instanceof SyncBoxItemSimpleDto) {
            selectedBabylonBoxItem = babylonBoxItems.get(selectionEvent.getSelectedOther().getId());
            if (selectedBabylonBoxItem != null) {
                selectedBabylonBoxItem.select(true);
            }
        }
    }

    public void onHover(SyncBoxItemSimpleDto syncItem) {
        if (hoverBabylonBoxItem == null && syncItem != null) {
            hoverBabylonBoxItem = babylonBoxItems.get(syncItem.getId());
            if (hoverBabylonBoxItem != null) {
                hoverBabylonBoxItem.hover(true);
            }
        } else if (hoverBabylonBoxItem != null && syncItem == null) {
            hoverBabylonBoxItem.hover(false);
            hoverBabylonBoxItem = null;
        } else if (hoverBabylonBoxItem != null && hoverBabylonBoxItem.getId() != syncItem.getId()) {
            hoverBabylonBoxItem.hover(false);
            hoverBabylonBoxItem = babylonBoxItems.get(syncItem.getId());
            if (hoverBabylonBoxItem != null) {
                hoverBabylonBoxItem.hover(true);
            }
        }
    }

}
