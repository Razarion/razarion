package com.btxtech.client.editor.terrain;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
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
    private ClientRenderServiceImpl renderService;
    @Inject
    private TerrainEditor terrainEditor;
    @Inject
    private Caller<TerrainElementEditorProvider> terrainEditorService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    @DataField
    private IntegerBox cursorRadius;
    @Inject
    @DataField
    private IntegerBox cursorCorners;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> slopeSelection;
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
        slopeSelection.addValueChangeHandler(new ValueChangeHandler<ObjectNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<ObjectNameId> event) {
                terrainEditor.setSlope4New(slopeSelection.getValue());
            }
        });
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                slopeSelection.setAcceptableValues(objectNameIds);
                slopeSelection.setValue(objectNameId);
                terrainEditor.setSlope4New(objectNameId);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getSlopeNameIds failed: " + message, throwable);
                return false;
            }
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

    @EventHandler("sculptButton")
    private void sculptButtonClick(ClickEvent event) {
        terrainEditor.updateTerrainSurface();
        // TODO terrainUiService.setup();
        renderService.setup();
        renderService.fillBuffers();
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        terrainEditor.save();
    }
}
