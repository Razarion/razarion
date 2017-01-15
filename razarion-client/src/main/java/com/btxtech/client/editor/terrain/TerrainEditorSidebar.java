package com.btxtech.client.editor.terrain;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.Camera;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("TerrainEditorSidebar.html#terrainEditor")
public class TerrainEditorSidebar extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(TerrainEditorSidebar.class.getName());
    @Inject
    private TerrainEditorImpl terrainEditor;
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    private Camera camera;
    @Inject
    @DataField
    private DoubleBox cursorRadius;
    @Inject
    @DataField
    private IntegerBox cursorCorners;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> slopeSelection;
    @Inject
    @DataField
    private Button topViewButton;
    @Inject
    @DataField
    private Button sculptButton;

    @PostConstruct
    public void init() {
        terrainEditor.activate();
        cursorRadius.setValue(terrainEditor.getCursorRadius());
        cursorCorners.setValue(terrainEditor.getCursorCorners());
        slopeSelection.addValueChangeHandler(event -> terrainEditor.setSlope4New(slopeSelection.getValue()));
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                slopeSelection.setAcceptableValues(objectNameIds);
                slopeSelection.setValue(objectNameId);
                terrainEditor.setSlope4New(objectNameId);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getSlopeNameIds failed: " + message, throwable);
            return false;
        }).getSlopeNameIds();

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

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        camera.setTop();
    }

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        terrainEditor.sculpt();
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(terrainEditor::save);
        enableSaveButton(true);
    }
}
