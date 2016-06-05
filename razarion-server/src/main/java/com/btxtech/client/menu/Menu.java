package com.btxtech.client.menu;

import com.btxtech.client.sidebar.ColladaEditorSidebar;
import com.btxtech.client.sidebar.LeftSideBar;
import com.btxtech.client.sidebar.LeftSideBarContent;
import com.btxtech.client.sidebar.TerrainEditorSidebar;
import com.btxtech.client.sidebar.TerrainObjectEditorSidebar;
import com.btxtech.client.sidebar.TerrainSidebar;
import com.btxtech.client.sidebar.slopeeditor.SlopeConfigSidebar;
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
    @DataField("menu-shadow")
    private ShadowMenu shadowMenu;
    @Inject
    @DataField("menu-camera")
    private CameraMenu cameraMenu;
    @Inject
    @DataField("menu-terrain")
    private InlineHyperlink terrainMenu;
    @Inject
    @DataField("menu-slope")
    private InlineHyperlink slopeMenu;
    @Inject
    @DataField("menu-water")
    private WaterMenu waterMenu;
    @Inject
    @DataField("menu-item")
    private ItemMenu itemMenu;
    @Inject
    @DataField("menu-editor")
    private InlineHyperlink editorMenu;
    @Inject
    @DataField("menu-object-editor")
    private InlineHyperlink objectEditorMenu;
    @Inject
    @DataField("menu-collada")
    private InlineHyperlink colladaMenu;

    @Inject
    private LeftSideBar leftSideBar;
    @Inject
    private Instance<LeftSideBarContent> leftSideBarContentInstance;

    @EventHandler("menu-terrain")
    private void terrainMenuClick(ClickEvent event) {
        leftSideBar.show(leftSideBarContentInstance.select(TerrainSidebar.class).get());
    }

    @EventHandler("menu-slope")
    private void slopeMenuClick(ClickEvent event) {
        leftSideBar.show(leftSideBarContentInstance.select(SlopeConfigSidebar.class).get());
    }

    @EventHandler("menu-editor")
    private void editorMenuClick(ClickEvent event) {
        leftSideBar.show(leftSideBarContentInstance.select(TerrainEditorSidebar.class).get());
    }

    @EventHandler("menu-object-editor")
    private void objectEditorMenuClick(ClickEvent event) {
        leftSideBar.show(leftSideBarContentInstance.select(TerrainObjectEditorSidebar.class).get());
    }

    @EventHandler("menu-collada")
    private void colladaMenuClick(ClickEvent event) {
        leftSideBar.show(leftSideBarContentInstance.select(ColladaEditorSidebar.class).get());
    }

}
