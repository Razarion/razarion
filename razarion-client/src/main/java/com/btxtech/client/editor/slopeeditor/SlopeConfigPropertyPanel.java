package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.fractal.FractalDialogDto;
import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.SpecularLightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.utils.BooleanNullConverter;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig_OLD;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeModeler;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.renderer.task.slope.SlopeRenderTask;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import elemental.client.Browser;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("SlopeConfigPropertyPanel.html#slope")
public class SlopeConfigPropertyPanel extends AbstractPropertyPanel<SlopeConfig_OLD> implements SelectedCornerListener {
    // private Logger logger = Logger.getLogger(SlopeConfigPropertyPanel.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private SlopeRenderTask slopeRenderTask;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Caller<PlanetEditorProvider> planetEditorServiceCaller;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @AutoBound
    private DataBinder<SlopeConfig_OLD> slopeConfigDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private ValueListBox<SlopeConfig.Type> type;
    @Inject
    @DataField
    private SpecularLightWidget specularLightConfig;
    @Inject
    @DataField
    private ImageItemWidget textureId;
    @Inject
    @Bound(property = "slopeSkeletonConfig.textureScale")
    @DataField
    private NumberInput textureScale;
    @Inject
    @DataField
    private ImageItemWidget bmId;
    @Inject
    @Bound(property = "slopeSkeletonConfig.bmScale")
    @DataField
    private NumberInput bmScale;
    @Inject
    @Bound(property = "slopeSkeletonConfig.bmDepth")
    @DataField
    private NumberInput bmDepth;
    @Inject
    @DataField
    private ImageItemWidget slopeWaterSplattingId;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeWaterSplattingScale")
    @DataField
    private NumberInput slopeWaterSplattingScale;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeWaterSplattingFactor")
    @DataField
    private NumberInput slopeWaterSplattingFactor;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeWaterSplattingFadeThreshold")
    @DataField
    private NumberInput slopeWaterSplattingFadeThreshold;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeWaterSplattingHeight")
    @DataField
    private NumberInput slopeWaterSplattingHeight;
    @Inject
    @Bound(property = "slopeSkeletonConfig.verticalSpace")
    @DataField
    private NumberInput verticalSpace;
    @Inject
    @DataField
    private Button fractalFieldButton;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeOriented", converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput slopeOriented;
    @DataField
    private Element svgElement = (Element) Browser.getDocument().createSVGElement();
    @Inject
    @DataField
    private Button zoomIn;
    @Inject
    @DataField
    private Button zoomOut;
    @Inject
    private ShapeEditor shapeEditor;
    @Inject
    @DataField
    private CommaDoubleBox helperLine;
    @Inject
    @DataField
    private CommaDoubleBox selectedXPos;
    @Inject
    @DataField
    private CommaDoubleBox selectedZPos;
    @Inject
    @DataField
    private CommaDoubleBox selectedSlopeFactor;
    @Inject
    @DataField
    private Button deleteSelected;
    @Inject
    @Bound(property = "slopeSkeletonConfig.outerLineGameEngine")
    @DataField
    private NumberInput outerLineGameEngine;
    @Inject
    @Bound(property = "slopeSkeletonConfig.coastDelimiterLineGameEngine")
    @DataField
    private NumberInput coastDelimiterLineGameEngine;
    @Inject
    @Bound(property = "slopeSkeletonConfig.innerLineGameEngine")
    @DataField
    private NumberInput innerLineGameEngine;
    @Inject
    @DataField
    private Button sculpt;
    @Inject
    @DataField
    private Button restartPlanetButton;
    private FractalDialogDto fractalDialogDto;

    @Override
    public void init(SlopeConfig_OLD slopeConfigOLD) {
        slopeConfigDataBinder.setModel(slopeConfigOLD);
        if (slopeConfigOLD.getSlopeConfig().getType() != null) {
            type.setValue(slopeConfigOLD.getSlopeConfig().getType());
        }
        type.setAcceptableValues(Arrays.asList(SlopeConfig.Type.values()));
        type.addValueChangeHandler(event -> slopeConfigOLD.getSlopeConfig().setType(event.getValue()));
        terrainUiService.enableEditMode(slopeConfigOLD.getSlopeConfig());
        textureId.setImageId(slopeConfigOLD.getSlopeConfig().getSlopeTextureId(), imageId -> {
            slopeConfigOLD.getSlopeConfig().setSlopeTextureId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
        bmId.setImageId(slopeConfigOLD.getSlopeConfig().getSlopeBumpMapId(), imageId -> {
            slopeConfigOLD.getSlopeConfig().setSlopeBumpMapId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
        specularLightConfig.setModel(slopeConfigOLD.getSlopeConfig().getSpecularLightConfig());
        slopeWaterSplattingId.setImageId(slopeConfigOLD.getSlopeConfig().getSlopeWaterSplattingId(), imageId -> {
            slopeConfigOLD.getSlopeConfig().setSlopeWaterSplattingId(imageId);
            terrainUiService.onEditorTerrainChanged();
        });
        shapeEditor.init(svgElement, slopeConfigOLD, this, 10.0);
    }

    @Override
    public SlopeConfig_OLD getConfigObject() {
        return slopeConfigDataBinder.getModel();
    }

    @EventHandler("fractalFieldButton")
    private void fractalFieldButtonClick(ClickEvent event) {
        SlopeConfig_OLD slopeConfigOLD = slopeConfigDataBinder.getModel();
        if (fractalDialogDto == null) {
            fractalDialogDto = new FractalDialogDto();
            fractalDialogDto.setFractalFieldConfig(slopeConfigOLD.toFractalFiledConfig());
        }
        modalDialogManager.show("Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalDialogDto, (button, fractalDialogDto1) -> {
            if (button == DialogButton.Button.APPLY) {
                SlopeConfig_OLD slopeConfigOLD1 = slopeConfigDataBinder.getModel();
                slopeConfigOLD1.fromFractalFiledConfig(fractalDialogDto1.getFractalFieldConfig());
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    @EventHandler("zoomIn")
    private void zoomInButtonClick(ClickEvent event) {
        shapeEditor.zoomIn();
    }

    @EventHandler("zoomOut")
    private void zoomOutButtonClick(ClickEvent event) {
        shapeEditor.zoomOut();
    }

    @EventHandler("helperLine")
    public void groundChanged(ChangeEvent e) {
        shapeEditor.setHelperLine(helperLine.getValue());
    }

    @Override
    public void onSelectionChanged(Corner corner) {
        if (corner != null) {
            selectedXPos.setValue(corner.getSlopeShape().getPosition().getX());
            selectedZPos.setValue(corner.getSlopeShape().getPosition().getY());
            selectedSlopeFactor.setValue((double) corner.getSlopeShape().getSlopeFactor());
        } else {
            selectedXPos.setValue(null);
            selectedXPos.setReadOnly(true);
            selectedZPos.setValue(null);
            selectedZPos.setReadOnly(true);
            selectedSlopeFactor.setValue(null);
            selectedSlopeFactor.setReadOnly(true);
        }
    }

    @EventHandler("selectedXPos")
    public void selectedXPosChanged(ChangeEvent e) {
        shapeEditor.moveSelected(new DecimalPosition(selectedXPos.getValue(), selectedZPos.getValue()));
    }

    @EventHandler("selectedZPos")
    public void selectedYPosChanged(ChangeEvent e) {
        shapeEditor.moveSelected(new DecimalPosition(selectedXPos.getValue(), selectedZPos.getValue()));
    }

    @EventHandler("selectedSlopeFactor")
    public void selectedSlopeFactorChanged(ChangeEvent e) {
        shapeEditor.setSlopeFactorSelected(selectedSlopeFactor.getValue());
    }

    @EventHandler("deleteSelected")
    public void deleteSelectedButtonClick(ClickEvent e) {
        shapeEditor.deleteSelectedCorner();
    }

    @EventHandler("sculpt")
    private void sculptButtonClick(ClickEvent event) {
        SlopeConfig_OLD slopeConfigOLD = getConfigObject();
        if (fractalDialogDto == null) {
            return;
        }
        SlopeModeler.sculpt(slopeConfigOLD.getSlopeConfig(), fractalDialogDto.getFractalField());
        renderService.fillBuffers();  // TODO May not working
    }

    @EventHandler("restartPlanetButton")
    private void restartPlanetButtonClicked(ClickEvent event) {
        modalDialogManager.showQuestionDialog("Restart planet", "Really restart the planet? Close all current connections.", () -> planetEditorServiceCaller.call(ignore -> {
        }, exceptionHandler.restErrorHandler("PlanetEditorProvider.restartPlanetWarm() failed: ")).restartPlanetCold(gameUiControl.getPlanetConfig().getPlanetId()), () -> {
        });
    }
}
