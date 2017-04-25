package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

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
public class BoxUiService {
    private Logger logger = Logger.getLogger(BoxUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private final Map<Integer, SyncBoxItemSimpleDto> boxes = new HashMap<>();
    private final MapList<BoxItemType, ModelMatrices> boxModelMatrices = new MapList<>();

    public void clear() {
        boxes.clear();
        boxModelMatrices.clear();
    }

    public void addBox(SyncBoxItemSimpleDto syncBoxItem) {
        synchronized (boxes) {
            if (boxes.put(syncBoxItem.getId(), syncBoxItem) != null) {
                logger.warning("Box already exists: " + syncBoxItem);
            }
        }
        setupModelMatrices();
    }

    public void removeBox(int id) {
        synchronized (boxes) {
            SyncBoxItemSimpleDto box = boxes.remove(id);
            if (box == null) {
                throw new IllegalStateException("No box for id: " + id);
            }
            selectionHandler.boxItemRemove(box);
        }
        setupModelMatrices();
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

    public List<ModelMatrices> provideModelMatrices(BoxItemType boxItemType) {
        synchronized (boxModelMatrices) {
            return boxModelMatrices.get(boxItemType);
        }
    }

    private void setupModelMatrices() {
        synchronized (boxModelMatrices) {
            boxModelMatrices.clear();
            for (SyncBoxItemSimpleDto boxItemSimpleDto : boxes.values()) {
                BoxItemType boxItemType = itemTypeService.getBoxItemType(boxItemSimpleDto.getItemTypeId());
                boxModelMatrices.put(boxItemType, new ModelMatrices(boxItemSimpleDto.getModel(), nativeMatrixFactory));
            }
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
        return new SyncItemState(boxItemSimpleDto, null, itemTypeService.getBoxItemType(boxItemSimpleDto.getItemTypeId()).getRadius(), null).createSyncItemMonitor();
    }
}
