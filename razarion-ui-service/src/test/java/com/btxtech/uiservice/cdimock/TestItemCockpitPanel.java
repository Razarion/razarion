package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import com.btxtech.uiservice.cockpit.item.ItemCockpitPanel;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestItemCockpitPanel implements ItemCockpitPanel {
    @Override
    public void cleanPanels() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInfoPanel(Object infoPanel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBuildupItemPanel(BuildupItemPanel buildupItemPanel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void maximizeMinButton() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPanel(boolean visible) {
        throw new UnsupportedOperationException();
    }
}
