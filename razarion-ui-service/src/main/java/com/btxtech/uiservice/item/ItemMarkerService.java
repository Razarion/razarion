package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.SelectionEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2017.
 */
@ApplicationScoped
public class ItemMarkerService {
    private static final double FACTOR = 1.1;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private ItemUiService itemUiService;
    private Collection<SyncItemMonitor> monitors = new ArrayList<>();
    private List<ModelMatrices> allModelMatrices = new ArrayList<>();
    private List<ModelMatrices> selectedModelMatrices = new ArrayList<>();
    private SyncItemMonitor hoverSyncItemMonitor;
    private ItemMarkerModelMatrices hoverModelMatrices;

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        clear();

        if (selectionEvent.getType() == SelectionEvent.Type.OWN) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup().getItems()) {
                setupSelectionModelMatrices(syncBaseItemSimpleDto, baseItemUiService.monitorSyncItem(syncBaseItemSimpleDto));
            }
        } else if (selectionEvent.getType() == SelectionEvent.Type.OTHER) {
            setupSelectionModelMatrices(selectionEvent.getSelectedOther(), itemUiService.monitorSyncItem(selectionEvent.getSelectedOther()));
        }

        setupAllModelMatrices();
    }

    public void onHover(SyncItemSimpleDto syncItem) {
        if (hoverSyncItemMonitor == null && syncItem != null) {
            hoverSyncItemMonitor = itemUiService.monitorSyncItem(syncItem);
            setupHoverModelMatrices(syncItem);
        } else if (hoverSyncItemMonitor != null && syncItem == null) {
            hoverSyncItemMonitor.release();
            hoverSyncItemMonitor = null;
            hoverModelMatrices = null;
            setupAllModelMatrices();
        } else if (hoverSyncItemMonitor != null && hoverSyncItemMonitor.getSyncItemId() != syncItem.getId()) {
            hoverSyncItemMonitor.release();
            hoverSyncItemMonitor = itemUiService.monitorSyncItem(syncItem);
            setupHoverModelMatrices(syncItem);
        }
    }

    private void setupHoverModelMatrices(SyncItemSimpleDto syncItem) {
        hoverModelMatrices = new ItemMarkerModelMatrices(setupMatrix(hoverSyncItemMonitor), itemUiService.color4SyncItem(syncItem).fromAlpha(Colors.HOVER_ALPHA), hoverSyncItemMonitor.getRadius());
        hoverSyncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            hoverModelMatrices.setModel(setupMatrix(changedSyncItemMonitor));
            hoverModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
        });

        setupAllModelMatrices();
    }

    private void clear() {
        for (SyncItemMonitor syncItemMonitor : monitors) {
            syncItemMonitor.release();
        }
        monitors.clear();
        selectedModelMatrices.clear();
    }

    public List<ModelMatrices> provideSelectedModelMatrices() {
        return allModelMatrices;
    }

    public boolean hasMarkedItems() {
        return !allModelMatrices.isEmpty();
    }

    private void setupSelectionModelMatrices(SyncItemSimpleDto syncItemSimpleDto, SyncItemMonitor syncItemMonitor) {
        monitors.add(syncItemMonitor);
        ItemMarkerModelMatrices modelMatrices = new ItemMarkerModelMatrices(setupMatrix(syncItemMonitor), itemUiService.color4SyncItem(syncItemSimpleDto).fromAlpha(Colors.SELECTION_ALPHA), syncItemMonitor.getRadius());
        modelMatrices.setInterpolatableVelocity(syncItemMonitor.getInterpolatableVelocity());
        selectedModelMatrices.add(modelMatrices);
        syncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            modelMatrices.setModel(setupMatrix(changedSyncItemMonitor));
            modelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
        });
    }

    private Matrix4 setupMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d()).multiply(Matrix4.createScale(syncItemMonitor.getRadius() * FACTOR));
    }


    private void setupAllModelMatrices() {
        allModelMatrices.clear();
        if (hoverModelMatrices != null) {
            allModelMatrices.add(hoverModelMatrices);
        }
        allModelMatrices.addAll(selectedModelMatrices);
    }

}
