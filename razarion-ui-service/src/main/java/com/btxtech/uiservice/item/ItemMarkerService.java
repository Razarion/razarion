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
    // Monitors
    private Collection<SyncItemMonitor> selectionMonitors = new ArrayList<>();
    private SyncItemMonitor hoverSyncItemMonitor;
    // ModelMatrices for renderer
    private List<ModelMatrices> allMarkerModelMatrices = new ArrayList<>();
    private List<ModelMatrices> allStatusBarModelMatrices = new ArrayList<>();
    // Selection
    private List<ModelMatrices> selectedMarkerModelMatrices = new ArrayList<>();
    private List<ModelMatrices> selectedHealthBarModelMatrices = new ArrayList<>();
    // Hover
    private ItemMarkerModelMatrices hoverMarkerModelMatrices;
    private StatusBarModelMatrices hoverHealthModelMatrices;

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        clearSelection();

        if (selectionEvent.getType() == SelectionEvent.Type.OWN) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup().getItems()) {
                setupSelectionModelMatrices(syncBaseItemSimpleDto, baseItemUiService.monitorSyncItem(syncBaseItemSimpleDto));
            }
        } else if (selectionEvent.getType() == SelectionEvent.Type.OTHER) {
            setupSelectionModelMatrices(selectionEvent.getSelectedOther(), itemUiService.monitorSyncItem(selectionEvent.getSelectedOther()));
        }

        setupAllMarkerModelMatrices();
        setupAllStatusBarModelMatrices();
    }

    public void onHover(SyncItemSimpleDto syncItem) {
        if (hoverSyncItemMonitor == null && syncItem != null) {
            hoverSyncItemMonitor = itemUiService.monitorSyncItem(syncItem);
            setupHoverModelMatrices(syncItem);
        } else if (hoverSyncItemMonitor != null && syncItem == null) {
            hoverSyncItemMonitor.release();
            hoverSyncItemMonitor = null;
            hoverMarkerModelMatrices = null;
            hoverHealthModelMatrices = null;
            setupAllMarkerModelMatrices();
            setupAllStatusBarModelMatrices();
        } else if (hoverSyncItemMonitor != null && hoverSyncItemMonitor.getSyncItemId() != syncItem.getId()) {
            hoverSyncItemMonitor.release();
            hoverSyncItemMonitor = itemUiService.monitorSyncItem(syncItem);
            setupHoverModelMatrices(syncItem);
        }
    }

    private void clearSelection() {
        for (SyncItemMonitor syncItemMonitor : selectionMonitors) {
            syncItemMonitor.release();
        }
        selectionMonitors.clear();
        selectedMarkerModelMatrices.clear();
        selectedHealthBarModelMatrices.clear();
    }

    public boolean hasMarkedItems() {
        return !allMarkerModelMatrices.isEmpty() || !allStatusBarModelMatrices.isEmpty();
    }

    public List<ModelMatrices> provideSelectedModelMatrices() {
        return allMarkerModelMatrices;
    }

    public List<ModelMatrices> provideStatusBarModelMatrices() {
        return allStatusBarModelMatrices;
    }

    private void setupSelectionModelMatrices(SyncItemSimpleDto syncItemSimpleDto, SyncItemMonitor syncItemMonitor) {
        selectionMonitors.add(syncItemMonitor);
        // Marker
        ItemMarkerModelMatrices selectionModelMatrices = new ItemMarkerModelMatrices(setupMarkerMatrix(syncItemMonitor), itemUiService.color4SyncItem(syncItemSimpleDto).fromAlpha(Colors.SELECTION_ALPHA), syncItemMonitor.getRadius());
        selectionModelMatrices.setInterpolatableVelocity(syncItemMonitor.getInterpolatableVelocity());
        selectedMarkerModelMatrices.add(selectionModelMatrices);
        // Health status bar
        StatusBarModelMatrices statusBarModelMatrices = null;
        if (syncItemSimpleDto instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItemSimpleDto;
            statusBarModelMatrices = new StatusBarModelMatrices(setupHealthBarMatrix(syncItemMonitor), Colors.HEALTH_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), syncBaseItem.getHealth());
            selectedHealthBarModelMatrices.add(statusBarModelMatrices);
        }
        final StatusBarModelMatrices statusBarModelMatricesFinal = statusBarModelMatrices;
        // Update listener
        syncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            selectionModelMatrices.setModel(setupMarkerMatrix(changedSyncItemMonitor));
            selectionModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            if (statusBarModelMatricesFinal != null) {
                statusBarModelMatricesFinal.setModel(setupHealthBarMatrix(changedSyncItemMonitor));
                statusBarModelMatricesFinal.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });
    }

    private void setupHoverModelMatrices(SyncItemSimpleDto syncItem) {
        // Marker
        hoverMarkerModelMatrices = new ItemMarkerModelMatrices(setupMarkerMatrix(hoverSyncItemMonitor), itemUiService.color4SyncItem(syncItem).fromAlpha(Colors.HOVER_ALPHA), hoverSyncItemMonitor.getRadius());
        // Health status bar
        if (syncItem instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItem;
            hoverHealthModelMatrices = new StatusBarModelMatrices(setupHealthBarMatrix(hoverSyncItemMonitor), Colors.HEALTH_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), syncBaseItem.getHealth());
        }
        // Update listener
        hoverSyncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            hoverMarkerModelMatrices.setModel(setupMarkerMatrix(changedSyncItemMonitor));
            hoverMarkerModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            if (hoverHealthModelMatrices != null) {
                hoverHealthModelMatrices.setModel(setupHealthBarMatrix(changedSyncItemMonitor));
                hoverHealthModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });

        setupAllMarkerModelMatrices();
        setupAllStatusBarModelMatrices();
    }

    private Matrix4 setupMarkerMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d()).multiply(Matrix4.createScale(syncItemMonitor.getRadius() * FACTOR));
    }

    private Matrix4 setupHealthBarMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d().getX(), syncItemMonitor.getPosition3d().getY() - syncItemMonitor.getRadius(), syncItemMonitor.getPosition3d().getZ()).multiply(Matrix4.createScale(2.0 * syncItemMonitor.getRadius(), 1, 1));
    }

    private void setupAllMarkerModelMatrices() {
        allMarkerModelMatrices.clear();
        if (hoverMarkerModelMatrices != null) {
            allMarkerModelMatrices.add(hoverMarkerModelMatrices);
        }
        allMarkerModelMatrices.addAll(selectedMarkerModelMatrices);
    }

    private void setupAllStatusBarModelMatrices() {
        allStatusBarModelMatrices.clear();
        if (hoverHealthModelMatrices != null) {
            allStatusBarModelMatrices.add(hoverHealthModelMatrices);
        }
        allStatusBarModelMatrices.addAll(selectedHealthBarModelMatrices);
    }

}
