package com.btxtech.client.menu;

import com.btxtech.client.LeftSideBar;
import com.btxtech.client.slopeeditor.SlopeConfigPanelContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHyperlink;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("Menu.html#menu-template")
public class Menu extends Composite {
    // private Logger logger = Logger.getLogger(Menu.class.getName());
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
    @DataField("menu-slope")
    private InlineHyperlink slopeMenu;
    @Inject
    @DataField("menu-water")
    private WaterMenu waterMenu;
    @Inject
    @DataField("menu-unit")
    private UnitMenu unitMenu;
    @Inject
    private LeftSideBar leftSideBar;
    @Inject
    private Instance<SlopeConfigPanelContainer> panelContainerInstance;

    @EventHandler("menu-slope")
    private void slopeMenuClick(ClickEvent event) {
        leftSideBar.show(panelContainerInstance.get());
    }

}
