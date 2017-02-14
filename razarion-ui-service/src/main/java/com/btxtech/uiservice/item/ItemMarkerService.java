package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.SingleHolder;
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
    private List<ModelMatrices> selectedHealthModelMatrices = new ArrayList<>();
    private List<ModelMatrices> selectedConstructingModelMatrices = new ArrayList<>();
    // Hover
    private ItemMarkerModelMatrices hoverMarkerModelMatrices;
    private StatusBarModelMatrices hoverHealthModelMatrices;
    private StatusBarModelMatrices hoverConstructingModelMatrices;

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
            hoverConstructingModelMatrices = null;
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
        selectedHealthModelMatrices.clear();
        selectedConstructingModelMatrices.clear();
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
        // Status bars
        StatusBarModelMatrices healthModelMatrices = null;
        final SingleHolder<StatusBarModelMatrices> constructingModelMatrices = new SingleHolder<>();
        if (syncItemSimpleDto instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItemSimpleDto;
            healthModelMatrices = new StatusBarModelMatrices(setupHealthBarMatrix(syncItemMonitor), Colors.HEALTH_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), syncBaseItem.getHealth());
            selectedHealthModelMatrices.add(healthModelMatrices);
            if (syncBaseItem.checkConstructing()) {
                constructingModelMatrices.setO(new StatusBarModelMatrices(setupConstructingBarMatrix(syncItemMonitor), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), syncBaseItem.getConstructing()));
                selectedConstructingModelMatrices.add(constructingModelMatrices.getO());
            }
        }
        final StatusBarModelMatrices healthModelMatricesFinal = healthModelMatrices;
        // Update listener
        syncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            selectionModelMatrices.setModel(setupMarkerMatrix(changedSyncItemMonitor));
            selectionModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            if (healthModelMatricesFinal != null) {
                healthModelMatricesFinal.setModel(setupHealthBarMatrix(changedSyncItemMonitor));
                healthModelMatricesFinal.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
            if (!constructingModelMatrices.isEmpty()) {
                constructingModelMatrices.getO().setModel(setupConstructingBarMatrix(changedSyncItemMonitor));
                constructingModelMatrices.getO().setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });
        if (healthModelMatricesFinal != null) {
            ((SyncBaseItemMonitor) syncItemMonitor).setHealthChangeListener(syncItemMonitor1 -> healthModelMatricesFinal.setProgress(((SyncBaseItemMonitor) syncItemMonitor1).getHealth()));
        }
        if (syncItemSimpleDto instanceof SyncBaseItemSimpleDto) {
            ((SyncBaseItemMonitor) syncItemMonitor).setConstructingChangeListener(syncItemMonitor1 -> {
                if (((SyncBaseItemMonitor) syncItemMonitor1).checkConstructing()) {
                    if (constructingModelMatrices.isEmpty()) {
                        constructingModelMatrices.setO(new StatusBarModelMatrices(setupConstructingBarMatrix(syncItemMonitor1), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), ((SyncBaseItemMonitor) syncItemMonitor1).getConstructing()));
                        constructingModelMatrices.getO().setInterpolatableVelocity(syncItemMonitor1.getInterpolatableVelocity());
                        selectedConstructingModelMatrices.add(constructingModelMatrices.getO());
                        setupAllStatusBarModelMatrices();
                    } else {
                        constructingModelMatrices.getO().setProgress(((SyncBaseItemMonitor) syncItemMonitor1).getConstructing());
                    }
                } else {
                    selectedConstructingModelMatrices.remove(constructingModelMatrices.getO());
                    constructingModelMatrices.setO(null);
                    setupAllStatusBarModelMatrices();
                }
            });
        }
    }

    private void setupHoverModelMatrices(SyncItemSimpleDto syncItem) {
        // Marker
        hoverMarkerModelMatrices = new ItemMarkerModelMatrices(setupMarkerMatrix(hoverSyncItemMonitor), itemUiService.color4SyncItem(syncItem).fromAlpha(Colors.HOVER_ALPHA), hoverSyncItemMonitor.getRadius());
        // Status bars
        if (syncItem instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItem;
            hoverHealthModelMatrices = new StatusBarModelMatrices(setupHealthBarMatrix(hoverSyncItemMonitor), Colors.HEALTH_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), syncBaseItem.getHealth());
            if (syncBaseItem.checkConstructing()) {
                hoverConstructingModelMatrices = new StatusBarModelMatrices(setupConstructingBarMatrix(hoverSyncItemMonitor), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), syncBaseItem.getConstructing());
            }
        }
        // Update listener
        hoverSyncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            hoverMarkerModelMatrices.setModel(setupMarkerMatrix(changedSyncItemMonitor));
            hoverMarkerModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            if (hoverHealthModelMatrices != null) {
                hoverHealthModelMatrices.setModel(setupHealthBarMatrix(changedSyncItemMonitor));
                hoverHealthModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
            if (hoverConstructingModelMatrices != null) {
                hoverConstructingModelMatrices.setModel(setupConstructingBarMatrix(changedSyncItemMonitor));
                hoverConstructingModelMatrices.setInterpolatableVelocity(changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });
        if (hoverSyncItemMonitor instanceof SyncBaseItemMonitor) {
            ((SyncBaseItemMonitor) hoverSyncItemMonitor).setHealthChangeListener(syncItemMonitor1 -> hoverHealthModelMatrices.setProgress(((SyncBaseItemMonitor) syncItemMonitor1).getHealth()));
            ((SyncBaseItemMonitor) hoverSyncItemMonitor).setConstructingChangeListener(syncItemMonitor1 -> {
                if (((SyncBaseItemMonitor) syncItemMonitor1).checkConstructing()) {
                    if (hoverConstructingModelMatrices == null) {
                        hoverConstructingModelMatrices = new StatusBarModelMatrices(setupConstructingBarMatrix(hoverSyncItemMonitor), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), ((SyncBaseItemMonitor) syncItemMonitor1).getConstructing());
                        hoverConstructingModelMatrices.setInterpolatableVelocity(syncItemMonitor1.getInterpolatableVelocity());
                        setupAllStatusBarModelMatrices();
                    } else {
                        hoverConstructingModelMatrices.setProgress(((SyncBaseItemMonitor) syncItemMonitor1).getConstructing());
                    }
                } else {
                    hoverConstructingModelMatrices = null;
                    setupAllStatusBarModelMatrices();
                }
            });
        }

        setupAllMarkerModelMatrices();
        setupAllStatusBarModelMatrices();
    }

    private Matrix4 setupMarkerMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d()).multiply(Matrix4.createScale(syncItemMonitor.getRadius() * FACTOR));
    }

    private Matrix4 setupHealthBarMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d().getX(), syncItemMonitor.getPosition3d().getY() - syncItemMonitor.getRadius(), syncItemMonitor.getPosition3d().getZ()).multiply(Matrix4.createScale(2.0 * syncItemMonitor.getRadius(), 1, 1));
    }

    private Matrix4 setupConstructingBarMatrix(SyncItemMonitor syncItemMonitor) {
        return Matrix4.createTranslation(syncItemMonitor.getPosition3d().getX(), syncItemMonitor.getPosition3d().getY(), syncItemMonitor.getPosition3d().getZ() + syncItemMonitor.getRadius()).multiply(Matrix4.createScale(2.0 * syncItemMonitor.getRadius(), 1, 1));
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
        if (hoverConstructingModelMatrices != null) {
            allStatusBarModelMatrices.add(hoverConstructingModelMatrices);
        }
        allStatusBarModelMatrices.addAll(selectedHealthModelMatrices);
        allStatusBarModelMatrices.addAll(selectedConstructingModelMatrices);
    }

}
