package com.btxtech.client.editor.menu;

import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.client.editor.sidebar.colladaeditor.ColladaEditorSidebar;
import com.btxtech.client.editor.sidebar.slope.SlopeSidebar;
import com.btxtech.client.editor.sidebar.slopeeditor.SlopeConfigSidebar;
import com.btxtech.client.editor.sidebar.terraineditor.TerrainEditorSidebar;
import com.btxtech.client.editor.sidebar.terrainobjecteditor.TerrainObjectEditorSidebar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHyperlink;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("Menu.html#menu-template")
@Deprecated
public class Menu extends Composite {
    // private Logger logger = Logger.getLogger(Menu.class.getName());
    @Inject
    @DataField("menu-shadow")
    private ShadowMenu shadowMenu;
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
    private LeftSideBarManager leftSideBarManager;

    @EventHandler("menu-terrain")
    private void terrainMenuClick(ClickEvent event) {
        leftSideBarManager.show(SlopeSidebar.class);
    }

    @EventHandler("menu-slope")
    private void slopeMenuClick(ClickEvent event) {
        leftSideBarManager.show(SlopeConfigSidebar.class);
    }

    @EventHandler("menu-editor")
    private void editorMenuClick(ClickEvent event) {
        leftSideBarManager.show(TerrainEditorSidebar.class);
    }

    @EventHandler("menu-object-editor")
    private void objectEditorMenuClick(ClickEvent event) {
        leftSideBarManager.show(TerrainObjectEditorSidebar.class);
    }

    @EventHandler("menu-collada")
    private void colladaMenuClick(ClickEvent event) {
        leftSideBarManager.show(ColladaEditorSidebar.class);
    }

}
