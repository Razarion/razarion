package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.fractal.FractalDialog;
import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeModeler;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.renderer.task.slope.SlopeRenderTask;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import elemental.client.Browser;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("SlopeConfigPropertyPanel.html#slope")
public class SlopeConfigPropertyPanel extends AbstractPropertyPanel<SlopeConfig> implements SelectedCornerListener {
    // private Logger logger = Logger.getLogger(SlopeConfigPropertyPanel.class.getName());
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
    @AutoBound
    private DataBinder<SlopeConfig> slopeConfigDataBinder;
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
    private LightWidget lightConfig;
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
    @Bound(property = "slopeSkeletonConfig.verticalSpace")
    @DataField
    private NumberInput verticalSpace;
    @Inject
    @DataField
    private Button fractalFieldButton;
    @Inject
    @Bound(property = "slopeSkeletonConfig.slopeOriented")
    @DataField
    private CheckBox slopeOriented;
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
    private CommaDoubleBox selectedYPos;
    @Inject
    @DataField
    private CommaDoubleBox selectedSlopeFactor;
    @Inject
    @DataField
    private Button deleteSelected;
    @Inject
    @DataField
    private Button sculpt;
    private FractalFieldConfig fractalFieldConfig;

    @Override
    public void init(SlopeConfig slopeConfig) {
        slopeConfigDataBinder.setModel(slopeConfig);
        terrainUiService.enableEditMode(slopeConfig.getSlopeSkeletonConfig());
        textureId.setImageId(slopeConfig.getSlopeSkeletonConfig().getTextureId(), imageId -> slopeConfig.getSlopeSkeletonConfig().setTextureId(imageId));
        bmId.setImageId(slopeConfig.getSlopeSkeletonConfig().getBmId(), imageId -> slopeConfig.getSlopeSkeletonConfig().setBmId(imageId));
        lightConfig.setModel(slopeConfig.getSlopeSkeletonConfig().getLightConfig());
        shapeEditor.init(svgElement, slopeConfig, this, 10.0);
    }

    @Override
    public SlopeConfig getConfigObject() {
        return slopeConfigDataBinder.getModel();
    }

    @EventHandler("fractalFieldButton")
    private void fractalFieldButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = slopeConfigDataBinder.getModel();
        if (fractalFieldConfig == null) {
            fractalFieldConfig = slopeConfig.toFractalFiledConfig();
        }
        modalDialogManager.show("Fractal Dialog", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, FractalDialog.class, fractalFieldConfig, (button, fractalFieldConfig1) -> {
            if (button == DialogButton.Button.APPLY) {
                SlopeConfig slopeConfig1 = slopeConfigDataBinder.getModel();
                slopeConfig1.fromFractalFiledConfig(fractalFieldConfig1);
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
            selectedYPos.setValue(corner.getSlopeShape().getPosition().getY());
            selectedSlopeFactor.setValue((double) corner.getSlopeShape().getSlopeFactor());
        } else {
            selectedXPos.setValue(null);
            selectedXPos.setReadOnly(true);
            selectedYPos.setValue(null);
            selectedYPos.setReadOnly(true);
            selectedSlopeFactor.setValue(null);
            selectedSlopeFactor.setReadOnly(true);
        }
    }

    @EventHandler("selectedXPos")
    public void selectedXPosChanged(ChangeEvent e) {
        shapeEditor.moveSelected(new DecimalPosition(selectedXPos.getValue(), selectedYPos.getValue()));
    }

    @EventHandler("selectedYPos")
    public void selectedYPosChanged(ChangeEvent e) {
        shapeEditor.moveSelected(new DecimalPosition(selectedXPos.getValue(), selectedYPos.getValue()));
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
        // TODO This method has not been touched for a long time
        SlopeConfig slopeConfig = getConfigObject();
        FractalFieldConfig fractalFieldConfig = this.fractalFieldConfig;
        if (fractalFieldConfig == null) {
            fractalFieldConfig = slopeConfig.toFractalFiledConfig();
        }
        SlopeModeler.sculpt(slopeConfig, fractalFieldConfig);
        terrainTypeService.overrideSlopeSkeletonConfig(slopeConfig.getSlopeSkeletonConfig());
        renderService.fillBuffers();
    }
}
