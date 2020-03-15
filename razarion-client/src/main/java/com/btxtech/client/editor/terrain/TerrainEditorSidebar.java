package com.btxtech.client.editor.terrain;

import com.btxtech.client.cockpit.radar.RadarPanel;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.common.DisplayUtils;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.dialog.DialogButton;
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
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.RadioInput;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("TerrainEditorSidebar.html#terrainEditor")
public class TerrainEditorSidebar extends AbstractEditor implements ViewService.ViewFieldListener {
    // private Logger logger = Logger.getLogger(TerrainEditorSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
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
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Span planetId;
    @Inject
    @DataField
    private Span terrainTileDimension;
    @Inject
    @DataField
    private Span terrainTiles;
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
    private Button topViewButton;
    @Inject
    @DataField
    private RadioInput slopeRadio;
    @Inject
    @DataField
    private RadioInput terrainObjectRadio;
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
    private CheckboxInput slopeInverted;
    @Inject
    @DataField
    private CheckboxInput drivewayMode;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> drivewaySelection;
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
    private Button showMiniMapButton;
    @Inject
    @DataField
    private RadarPanel radarPanel;
    @Inject
    @DataField
    private Button restartPlanetButton;

    @PostConstruct
    public void init() {
        terrainEditor.activate();
        planetId.setTextContent(Integer.toString(terrainEditor.getPlanetConfig().getId()));
        terrainTiles.setTextContent(DisplayUtils.handleRectangle(terrainEditor.getPlanetConfig().getTerrainTileDimension()));
        terrainTileDimension.setTextContent(DisplayUtils.handleRectangle2D(TerrainUtil.toTileAbsolute(terrainEditor.getPlanetConfig().getTerrainTileDimension())));
        playGround.setTextContent(DisplayUtils.handleRectangle2D(terrainEditor.getPlanetConfig().getPlayGround()));
        slopeRadio.setChecked(terrainEditor.getCreationMode());
        terrainObjectRadio.setChecked(!terrainEditor.getCreationMode());
        cursorRadius.setValue(terrainEditor.getCursorRadius());
        slopeInverted.setChecked(terrainEditor.isInvertedSlope());
        cursorCorners.setValue(terrainEditor.getCursorCorners());
        terrainObjectRandomZRotation.setValue(terrainEditor.getTerrainObjectRandomZRotation());
        terrainObjectRandomScale.setValue(terrainEditor.getTerrainObjectRandomScale());
        slopeSelection.addValueChangeHandler(event -> terrainEditor.setSlope4New(slopeSelection.getValue()));
        elementEditorProvider.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            slopeSelection.setAcceptableValues(objectNameIds);
            slopeSelection.setValue(objectNameId);
            terrainEditor.setSlope4New(objectNameId);
        }, exceptionHandler.restErrorHandler("getSlopeNameIds failed: ")).getSlopeNameIds();
        drivewayMode.setChecked(terrainEditor.isDrivewayMode());
        drivewaySelection.addValueChangeHandler(event -> terrainEditor.setDriveway4New(drivewaySelection.getValue()));
        elementEditorProvider.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            drivewaySelection.setAcceptableValues(objectNameIds);
            drivewaySelection.setValue(objectNameId);
            terrainEditor.setDriveway4New(objectNameId);
        }, exceptionHandler.restErrorHandler("readDrivewayObjectNameIds failed: ")).readDrivewayObjectNameIds();
        terrainObjectSelection.addValueChangeHandler(event -> terrainEditor.setTerrainObject4New(terrainObjectSelection.getValue()));
        elementEditorProvider.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            terrainObjectSelection.setAcceptableValues(objectNameIds);
            terrainObjectSelection.setValue(objectNameId);
            terrainEditor.setTerrainObject4New(objectNameId);
        }, exceptionHandler.restErrorHandler("getTerrainObjectNameIds failed: ")).getTerrainObjectNameIds();
        terrainEditor.setTerrainPositionListener(vertex -> terrainPositionLabel.setTextContent(DisplayUtils.formatVertex(vertex)));
        viewService.addViewFieldListeners(this);
        radarPanel.show();
    }

    @EventHandler("slopeRadio")
    private void slopeRadioClick(ClickEvent event) {
        terrainEditor.setSlopeMode(true);
    }

    @EventHandler("terrainObjectRadio")
    private void terrainObjectRadioClick(ClickEvent event) {
        terrainEditor.setSlopeMode(false);
    }

    @EventHandler("drivewayMode")
    public void drivewayModeChanged(ChangeEvent e) {
        terrainEditor.setDrivewayModeChanged(drivewayMode.getChecked());
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

    @EventHandler("slopeInverted")
    public void slopeInvertedCheckboxChanged(ChangeEvent e) {
        terrainEditor.setInvertedSlope(slopeInverted.getChecked());
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
    }

    @EventHandler("restartPlanetButton")
    private void restartPlanetButtonClick(ClickEvent event) {
        terrainEditor.restartPlanetButton();
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
    }

    @EventHandler("showMiniMapButton")
    private void showMiniMapButtonClicked(ClickEvent event) {
        modalDialogManager.show("Mini map generator", ClientModalDialogManagerImpl.Type.STACK_ABLE, MiniMapDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }
}
