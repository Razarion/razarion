package com.btxtech.client.editor.terrain;

import com.btxtech.client.cockpit.radar.RadarPanel;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.common.DisplayUtils;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.common.client.dom.RadioInput;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("TerrainEditor.html#terrainEditor")
public class TerrainEditor extends AbstractEditor implements ViewService.ViewFieldListener {
    // private Logger logger = Logger.getLogger(TerrainEditorSidebar.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private TerrainEditorService terrainEditorService;
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
    private Instance<IsElement> elementInstance;
    @Inject
    @DataField
    private Span id;
    @Inject
    @DataField
    private Span terrainSize;
    @Inject
    @DataField
    private RadarPanel radarPanel;
    @Inject
    @DataField
    private HTMLDivElement terrainPositionLabel;
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
    private HTMLDivElement controlPanel;
    @Inject
    @DataField
    private Button showMiniMapButton;
    @Inject
    @DataField
    private Button restartPlanetButton;

    @PostConstruct
    public void init() {
        terrainEditorService.activate();
        id.setTextContent(Integer.toString(terrainEditorService.getPlanetConfig().getId()));
        terrainSize.setTextContent(DisplayUtils.handleDecimalPosition(terrainEditorService.getPlanetConfig().getSize()));

        slopeRadio.setChecked(terrainEditorService.getSlopeMode());
        terrainObjectRadio.setChecked(!terrainEditorService.getSlopeMode());
        setSlopeMode(terrainEditorService.getSlopeMode());

        terrainEditorService.setTerrainPositionListener(vertex -> terrainPositionLabel.textContent = DisplayUtils.formatVertex(vertex));
        viewService.addViewFieldListeners(this);
        radarPanel.show();
    }

    @EventHandler("slopeRadio")
    private void slopeRadioClick(ClickEvent event) {
        setSlopeMode(true);
    }

    @EventHandler("terrainObjectRadio")
    private void terrainObjectRadioClick(ClickEvent event) {
        setSlopeMode(false);
    }

    @Override
    public void onClose() {
        viewService.removeViewFieldListeners(this);
        terrainEditorService.deactivate();
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        projectionTransformation.disableFovYConstrain();
        terrainScrollHandler.setScrollDisabled(false, null);
        camera.setTop();
    }

    @EventHandler("restartPlanetButton")
    private void restartPlanetButtonClick(ClickEvent event) {
        terrainEditorService.restartPlanetButton();
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(terrainEditorService::save);
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

    private void setSlopeMode(boolean slopeMode) {
        IsElement controlPanel;
        if (slopeMode) {
            controlPanel = elementInstance.select(SlopeControlPanel.class).get();
        } else {
            controlPanel = elementInstance.select(TerrainObjectControlPanel.class).get();
        }
        Elemental2Utils.removeAllChildren(this.controlPanel);
        this.controlPanel.appendChild(controlPanel.getElement());
        terrainEditorService.setSlopeMode(slopeMode);
    }
}
