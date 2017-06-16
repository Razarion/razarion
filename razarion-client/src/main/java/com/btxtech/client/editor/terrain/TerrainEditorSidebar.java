package com.btxtech.client.editor.terrain;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.Span;
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
public class TerrainEditorSidebar extends LeftSideBarContent implements ViewService.ViewFieldListener {
    private Logger logger = Logger.getLogger(TerrainEditorSidebar.class.getName());
    @Inject
    private TerrainEditorImpl terrainEditor;
    @Inject
    private Caller<TerrainElementEditorProvider> elementEditorProvider;
    @Inject
    private Camera camera;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ViewService viewService;
    @Inject
    @DataField
    private Span planetId;
    @Inject
    @DataField
    private Span groundMeshDimension;
    @Inject
    @DataField
    private Span playGround;
    @Inject
    @DataField
    private Span terrainPositionLabel;
    @Inject
    @DataField
    private DecimalPositionBox viewFiledCenter;
    @Inject
    @DataField
    private Span viewFiledTop;
    @Inject
    @DataField
    private Span viewFiledBottom;
    @Inject
    @DataField
    private Span viewFiledHeight;
    @Inject
    @DataField
    private Button creationModeButton;
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
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @Inject
    @DataField
    private DoubleBox terrainObjectRandomZRotation;
    @Inject
    @DataField
    private DoubleBox terrainObjectRandomScale;
    @Inject
    @DataField
    private Button topViewButton;
    @Inject
    @DataField
    private Button sculptButton;

    @PostConstruct
    public void init() {
        terrainEditor.activate();
        planetId.setTextContent(Integer.toString(terrainEditor.getPlanetConfig().getPlanetId()));
        groundMeshDimension.setTextContent(DisplayUtils.handleRectangle2D(TerrainUtil.toAbsoluteNodeRectangle(terrainEditor.getPlanetConfig().getGroundMeshDimension())));
        playGround.setTextContent(DisplayUtils.handleRectangle2D(terrainEditor.getPlanetConfig().getPlayGround()));
        creationModeButton.setText(terrainEditor.getCreationModeText());
        cursorRadius.setValue(terrainEditor.getCursorRadius());
        cursorCorners.setValue(terrainEditor.getCursorCorners());
        terrainObjectRandomZRotation.setValue(terrainEditor.getTerrainObjectRandomZRotation());
        terrainObjectRandomScale.setValue(terrainEditor.getTerrainObjectRandomScale());
        slopeSelection.addValueChangeHandler(event -> terrainEditor.setSlope4New(slopeSelection.getValue()));
        elementEditorProvider.call(new RemoteCallback<Collection<ObjectNameId>>() {
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
        terrainObjectSelection.addValueChangeHandler(event -> terrainEditor.setTerrainObject4New(terrainObjectSelection.getValue()));
        elementEditorProvider.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                terrainObjectSelection.setAcceptableValues(objectNameIds);
                terrainObjectSelection.setValue(objectNameId);
                terrainEditor.setTerrainObject4New(objectNameId);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getTerrainObjectNameIds failed: " + message, throwable);
            return false;
        }).getTerrainObjectNameIds();
        terrainEditor.setTerrainPositionListener(vertex -> terrainPositionLabel.setTextContent(DisplayUtils.formatVertex(vertex)));
        viewService.addViewFieldListeners(this);
    }

    @EventHandler("creationModeButton")
    private void creationModeButtonClick(ClickEvent event) {
        terrainEditor.toggleCreationMode();
        creationModeButton.setText(terrainEditor.getCreationModeText());
    }

    @Override
    public void onClose() {
        viewService.removeViewFieldListeners(this);
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

    @EventHandler("terrainObjectRandomZRotation")
    public void terrainObjectRandomZRotationChanged(ChangeEvent e) {
        terrainEditor.setTerrainObjectRandomZRotation(terrainObjectRandomZRotation.getValue());
    }

    @EventHandler("terrainObjectRandomScale")
    public void terrainObjectRandomScaleChanged(ChangeEvent e) {
        terrainEditor.setTerrainObjectRandomScale(terrainObjectRandomScale.getValue());
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        projectionTransformation.disableFovYConstrain();
        terrainScrollHandler.setPlayGround(null);
        terrainScrollHandler.setScrollDisabled(false, null);
        camera.setTop();
        terrainScrollHandler.update();
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

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        viewFiledCenter.setValue(viewField.calculateCenter());
        viewFiledTop.setTextContent(DisplayUtils.handleDouble2(viewField.getTopLeft().getDistance(viewField.getTopRight())));
        viewFiledBottom.setTextContent(DisplayUtils.handleDouble2(viewField.getBottomLeft().getDistance(viewField.getBottomRight())));
        viewFiledHeight.setTextContent(DisplayUtils.handleDouble2(viewField.getBottomLeft().getDistance(viewField.getTopLeft())));
    }

    @EventHandler("viewFiledCenter")
    public void yFieldChanged(ChangeEvent e) {
        camera.setTranslateXY(viewFiledCenter.getValue().getX(), viewFiledCenter.getValue().getY());
        terrainScrollHandler.update();
    }
}
