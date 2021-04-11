package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.cockpit.CockpitMode;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 05.12.2017.
 */
@Deprecated
public abstract class ItemContainerPanel {
    @Inject
    private CockpitMode cockpitMode;

    protected abstract void updateGui(boolean enabled, int count);

    public void display(SyncBaseItemSimpleDto syncBaseItem) {
        updateGui(syncBaseItem.getContainingItemCount() > 0, syncBaseItem.getContainingItemCount());
    }

    protected void onUnloadPressed() {
        if (cockpitMode.getMode() != CockpitMode.Mode.UNLOAD) {
            cockpitMode.setCockpitMode(CockpitMode.Mode.UNLOAD);
        } else if (cockpitMode.getMode() == CockpitMode.Mode.UNLOAD) {
            cockpitMode.setCockpitMode(null);
        }
    }
}
