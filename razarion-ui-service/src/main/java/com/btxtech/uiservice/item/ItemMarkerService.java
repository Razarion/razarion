package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.SelectionEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2017.
 */
@ApplicationScoped
public class ItemMarkerService {
    private List<ModelMatrices> selectedModelMatrices = new ArrayList<>();

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        selectedModelMatrices.clear();

        if (selectionEvent.getType() == SelectionEvent.Type.OWN) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup().getItems()) {
                selectedModelMatrices.add(new ModelMatrices(syncBaseItemSimpleDto.getModel()));
            }
        } else if (selectionEvent.getType() == SelectionEvent.Type.OTHER) {
            selectedModelMatrices.add(new ModelMatrices(selectionEvent.getSelectedOther().getModel()));
        }
    }

    public List<ModelMatrices> provideSelectedModelMatrices() {
        return selectedModelMatrices;
    }

    public boolean haSelection() {
        return !selectedModelMatrices.isEmpty();
    }
}
