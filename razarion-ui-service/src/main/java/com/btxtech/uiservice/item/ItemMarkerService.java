package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

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
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
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
    private ModelMatrices hoverMarkerModelMatrices;
    private ModelMatrices hoverHealthModelMatrices;
    private ModelMatrices hoverConstructingModelMatrices;

    public void clear() {
        selectionMonitors.clear();
        hoverSyncItemMonitor = null;
        // ModelMatrices for renderer
        allMarkerModelMatrices.clear();
        allStatusBarModelMatrices.clear();
        // Selection
        selectedMarkerModelMatrices.clear();
        selectedHealthModelMatrices.clear();
        selectedConstructingModelMatrices.clear();
        // Hover
        hoverMarkerModelMatrices = null;
        hoverHealthModelMatrices = null;
        hoverConstructingModelMatrices = null;
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
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
        ModelMatrices selectionModelMatrices = ModelMatrices.create4Marker(syncItemMonitor.getPosition3d(), syncItemMonitor.getRadius() * FACTOR, syncItemMonitor.getInterpolatableVelocity(), itemUiService.color4SyncItem(syncItemSimpleDto).fromAlpha(Colors.SELECTION_ALPHA), syncItemMonitor.getRadius(), nativeMatrixFactory);
        selectedMarkerModelMatrices.add(selectionModelMatrices);
        // Status bars
        ModelMatrices healthModelMatrices = null;
        final SingleHolder<ModelMatrices> constructingModelMatrices = new SingleHolder<>();
        if (syncItemSimpleDto instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItemSimpleDto;
            healthModelMatrices = ModelMatrices.create4Status(setupHealthBarPosition(syncItemMonitor), 2.0 * syncItemMonitor.getRadius(), syncItemMonitor.getInterpolatableVelocity(), Colors.HEALTH_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), syncBaseItem.getHealth(), nativeMatrixFactory);
            selectedHealthModelMatrices.add(healthModelMatrices);
            if (syncBaseItem.checkConstructing()) {
                constructingModelMatrices.setO(ModelMatrices.create4Status(setupConstructingBarPosition(syncItemMonitor), 2.0 * syncItemMonitor.getRadius(), syncItemMonitor.getInterpolatableVelocity(), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), syncBaseItem.getConstructing(), nativeMatrixFactory));
                selectedConstructingModelMatrices.add(constructingModelMatrices.getO());
            }
        }
        final ModelMatrices healthModelMatricesFinal = healthModelMatrices;
        // Update listener
        syncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            if(changedSyncItemMonitor.getPosition2d() == null) {
                return;
            }
            selectionModelMatrices.updatePositionScale(changedSyncItemMonitor.getPosition3d(), changedSyncItemMonitor.getRadius() * FACTOR, changedSyncItemMonitor.getInterpolatableVelocity());
            if (healthModelMatricesFinal != null) {
                healthModelMatricesFinal.updatePositionScaleX(setupHealthBarPosition(changedSyncItemMonitor), 2.0 * changedSyncItemMonitor.getRadius(), changedSyncItemMonitor.getInterpolatableVelocity());
            }
            if (!constructingModelMatrices.isEmpty()) {
                constructingModelMatrices.getO().updatePositionScaleX(setupConstructingBarPosition(changedSyncItemMonitor), 2.0 * changedSyncItemMonitor.getRadius(), changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });
        if (healthModelMatricesFinal != null) {
            ((SyncBaseItemMonitor) syncItemMonitor).setHealthChangeListener(syncItemMonitor1 -> healthModelMatricesFinal.updateProgress(((SyncBaseItemMonitor) syncItemMonitor1).getHealth()));
        }
        if (syncItemSimpleDto instanceof SyncBaseItemSimpleDto) {
            ((SyncBaseItemMonitor) syncItemMonitor).setConstructingChangeListener(syncItemMonitor1 -> {
                if (((SyncBaseItemMonitor) syncItemMonitor1).checkConstructing()) {
                    if (constructingModelMatrices.isEmpty()) {
                        constructingModelMatrices.setO(ModelMatrices.create4Status(setupConstructingBarPosition(syncItemMonitor1), 2.0 * syncItemMonitor1.getRadius(), syncItemMonitor1.getInterpolatableVelocity(), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.SELECTION_ALPHA), Colors.BAR_BG.fromAlpha(Colors.SELECTION_ALPHA), ((SyncBaseItemMonitor) syncItemMonitor1).getConstructing(), nativeMatrixFactory));
                        selectedConstructingModelMatrices.add(constructingModelMatrices.getO());
                        setupAllStatusBarModelMatrices();
                    } else {
                        constructingModelMatrices.getO().updateProgress(((SyncBaseItemMonitor) syncItemMonitor1).getConstructing());
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
        hoverMarkerModelMatrices = ModelMatrices.create4Marker(hoverSyncItemMonitor.getPosition3d(), hoverSyncItemMonitor.getRadius() * FACTOR, hoverSyncItemMonitor.getInterpolatableVelocity(), itemUiService.color4SyncItem(syncItem).fromAlpha(Colors.HOVER_ALPHA), hoverSyncItemMonitor.getRadius(), nativeMatrixFactory);
        // Status bars
        if (syncItem instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) syncItem;
            hoverHealthModelMatrices = ModelMatrices.create4Status(setupHealthBarPosition(hoverSyncItemMonitor), 2.0 * hoverSyncItemMonitor.getRadius(), hoverSyncItemMonitor.getInterpolatableVelocity(), Colors.HEALTH_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), syncBaseItem.getHealth(), nativeMatrixFactory);
            if (syncBaseItem.checkConstructing()) {
                hoverConstructingModelMatrices = ModelMatrices.create4Status(setupConstructingBarPosition(hoverSyncItemMonitor), 2.0 * hoverSyncItemMonitor.getRadius(), hoverSyncItemMonitor.getInterpolatableVelocity(), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), syncBaseItem.getConstructing(), nativeMatrixFactory);
            }
        }
        // Update listener
        hoverSyncItemMonitor.setPositionChangeListener(changedSyncItemMonitor -> {
            hoverMarkerModelMatrices.updatePositionScale(changedSyncItemMonitor.getPosition3d(), changedSyncItemMonitor.getRadius() * FACTOR, changedSyncItemMonitor.getInterpolatableVelocity());
            if (hoverHealthModelMatrices != null) {
                hoverHealthModelMatrices.updatePositionScaleX(setupHealthBarPosition(changedSyncItemMonitor), 2.0 * changedSyncItemMonitor.getRadius(), changedSyncItemMonitor.getInterpolatableVelocity());
            }
            if (hoverConstructingModelMatrices != null) {
                hoverConstructingModelMatrices.updatePositionScaleX(setupConstructingBarPosition(changedSyncItemMonitor), 2.0 * changedSyncItemMonitor.getRadius(), changedSyncItemMonitor.getInterpolatableVelocity());
            }
        });
        if (hoverSyncItemMonitor instanceof SyncBaseItemMonitor) {
            ((SyncBaseItemMonitor) hoverSyncItemMonitor).setHealthChangeListener(syncItemMonitor1 -> hoverHealthModelMatrices.updateProgress(((SyncBaseItemMonitor) syncItemMonitor1).getHealth()));
            ((SyncBaseItemMonitor) hoverSyncItemMonitor).setConstructingChangeListener(syncItemMonitor1 -> {
                if (((SyncBaseItemMonitor) syncItemMonitor1).checkConstructing()) {
                    if (hoverConstructingModelMatrices == null) {
                        hoverConstructingModelMatrices = ModelMatrices.create4Status(setupConstructingBarPosition(syncItemMonitor1), 2.0 * syncItemMonitor1.getRadius(), syncItemMonitor1.getInterpolatableVelocity(), Colors.CONSTRUCTING_BAR.fromAlpha(Colors.HOVER_ALPHA), Colors.BAR_BG.fromAlpha(Colors.HOVER_ALPHA), ((SyncBaseItemMonitor) syncItemMonitor1).getConstructing(), nativeMatrixFactory);
                        setupAllStatusBarModelMatrices();
                    } else {
                        hoverConstructingModelMatrices.updateProgress(((SyncBaseItemMonitor) syncItemMonitor1).getConstructing());
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

    private Vertex setupHealthBarPosition(SyncItemMonitor syncItemMonitor) {
        return new Vertex(syncItemMonitor.getPosition3d().getX(), syncItemMonitor.getPosition3d().getY() - syncItemMonitor.getRadius(), syncItemMonitor.getPosition3d().getZ());
    }

    private Vertex setupConstructingBarPosition(SyncItemMonitor syncItemMonitor) {
        return new Vertex(syncItemMonitor.getPosition3d().getX(), syncItemMonitor.getPosition3d().getY(), syncItemMonitor.getPosition3d().getZ() + syncItemMonitor.getRadius());
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
