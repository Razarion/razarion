package com.btxtech.client.sidebar;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("TerrainEditorSidebar.html#terrainEditor")
public class TerrainEditorSidebar extends Composite implements LeftSideBarContent {
    // private Logger logger = Logger.getLogger(TerrainEditorSidebar.class.getName());
    @Inject
    private TerrainEditor terrainEditor;
    @Inject
    @DataField
    private IntegerBox cursorRadius;
    @Inject
    @DataField
    private IntegerBox cursorCorners;
    @Inject
    @DataField
    private Button sculptButton;
    @Inject
    @DataField
    private Button saveButton;

    @PostConstruct
    public void init() {
        terrainEditor.activate();
        cursorRadius.setValue(terrainEditor.getCursorRadius());
        cursorCorners.setValue(terrainEditor.getCursorCorners());
    }

    @Override
    public void onClose() {
        terrainEditor.deactivate();
    }

    @EventHandler("cursorRadius")
    public void cursorRadiusChanged(ChangeEvent e) {
        terrainEditor.setCursorRadius(cursorRadius.getValue());
    }

    @EventHandler("cursorCorners")
    public void cursorCornersChanged(ChangeEvent e) {
        terrainEditor.setCursorCorners(cursorCorners.getValue());
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
    }
}
