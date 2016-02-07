package com.btxtech.client.menu;

import com.btxtech.client.slopeeditor.PanelContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("Menu.html#menu-template")
public class Menu extends Composite {
    @Inject
    @DataField("menu-debug")
    private DebugMenu debugMenu;
    @Inject
    @DataField("menu-light")
    private LightMenu lightMenu;
    @Inject
    @DataField("menu-camera")
    private CameraMenu cameraMenu;
    @Inject
    @DataField("menu-terrain")
    private TerrainMenu terrainMenu;
    @Inject
    @DataField
    private Button plateauEditor;
    @Inject
    @DataField("menu-beach")
    private BeachMenu beachMenu;
    @Inject
    @DataField("menu-water")
    private WaterMenu waterMenu;
    @Inject
    @DataField("menu-unit")
    private UnitMenu unitMenu;
    private PanelContainer panelContainer;

    public void setEditorPanelContainer(PanelContainer panelContainer) {
        this.panelContainer = panelContainer;
    }

    @EventHandler("plateauEditor")
    private void plateauEditorButtonClick(ClickEvent event) {
        panelContainer.showSlopeEditor();
    }

}
