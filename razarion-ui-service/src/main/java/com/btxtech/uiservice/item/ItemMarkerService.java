package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
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
    private Collection<SyncItemMonitor> monitors = new ArrayList<>();
    private List<ModelMatrices> selectedModelMatrices = new ArrayList<>();

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        clear();

        if (selectionEvent.getType() == SelectionEvent.Type.OWN) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup().getItems()) {
                createModelMatrices(baseItemUiService.monitorSyncItem(syncBaseItemSimpleDto));
            }
        } else if (selectionEvent.getType() == SelectionEvent.Type.OTHER) {
            if (selectionEvent.getSelectedOther() instanceof SyncBoxItemSimpleDto) {
                createModelMatrices(boxUiService.monitorSyncBoxItem((SyncBoxItemSimpleDto) selectionEvent.getSelectedOther()));
            } else if (selectionEvent.getSelectedOther() instanceof SyncBaseItemSimpleDto) {
                createModelMatrices(baseItemUiService.monitorSyncItem((SyncBaseItemSimpleDto) selectionEvent.getSelectedOther()));
            } else if (selectionEvent.getSelectedOther() instanceof SyncResourceItemSimpleDto) {
                createModelMatrices(resourceUiService.monitorSyncResourceItem((SyncResourceItemSimpleDto) selectionEvent.getSelectedOther()));
            } else {
                throw new IllegalArgumentException("Don't know how to handle: " + selectionEvent.getSelectedOther());
            }
        }
    }

    private void clear() {
        for (SyncItemMonitor syncItemMonitor : monitors) {
            syncItemMonitor.release();
        }
        monitors.clear();
        selectedModelMatrices.clear();
    }

    public List<ModelMatrices> provideSelectedModelMatrices() {
        return selectedModelMatrices;
    }

    public boolean haSelection() {
        return !selectedModelMatrices.isEmpty();
    }

    private void createModelMatrices(SyncItemMonitor syncItemMonitor) {
        monitors.add(syncItemMonitor);
        ModelMatrices modelMatrices = new ModelMatrices(setupMatrix(syncItemMonitor));
        modelMatrices.setRadius(syncItemMonitor.getRadius());
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
}
