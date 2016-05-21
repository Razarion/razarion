package com.btxtech.client.sidebar.slopeeditor;

import com.btxtech.client.dialog.ModalDialog;
import com.btxtech.client.dialog.content.fractal.FractalDialog;
import com.btxtech.client.terrain.FractalFieldConfig;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.slope.SlopeModeler;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.SlopeConfig;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import elemental.client.Browser;
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
@Templated("SlopeConfigPanel.html#slope")
public class SlopeConfigPanel extends Composite implements SelectedCornerListener {
    // private Logger logger = Logger.getLogger(SlopePanel.class.getName());
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ModalDialog modalDialog;
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
    @Bound(property = "slopeSkeleton.slopeGroundSplattingBumpDepth")
    @DataField
    private DoubleBox slopeGroundSplattingBumpDepth;
    @Inject
    @Bound(property = "slopeSkeleton.slopeFactorDistance")
    @DataField
    private DoubleBox slopeFactorDistance;
    @Inject
    @Bound(property = "slopeSkeleton.bumpMapDepth")
    @DataField
    private DoubleBox bumpMapDepth;
    @Inject
    @Bound(property = "slopeSkeleton.specularIntensity")
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @Bound(property = "slopeSkeleton.specularHardness")
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @Bound(property = "slopeSkeleton.verticalSpace")
    @DataField
    private IntegerBox verticalSpace;
    @Inject
    @DataField
    private Button fractalFieldButton;
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
    private DoubleBox helperLine;
    @Inject
    @DataField
    private IntegerBox selectedXPos;
    @Inject
    @DataField
    private IntegerBox selectedYPos;
    @Inject
    @DataField
    private DoubleBox selectedSlopeFactor;
    @Inject
    @DataField
    private Button deleteSelected;
    @Inject
    @DataField
    private Button sculpt;
    @Inject
    @DataField
    private Button update;
    private FractalFieldConfig fractalFieldConfig;

    public void init(SlopeConfig slopeConfig, Double zoom) {
        slopeConfigDataBinder.setModel(slopeConfig);
        shapeEditor.init(svgElement, slopeConfig, this, zoom);
    }

    @EventHandler("fractalFieldButton")
    private void fractalFieldButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = slopeConfigDataBinder.getModel();
        if (fractalFieldConfig == null) {
            fractalFieldConfig = slopeConfig.toFractalFiledConfig();
        }
        modalDialog.show("Fractal Dialog", FractalDialog.class, fractalFieldConfig, new Runnable() {
            @Override
            public void run() {
                SlopeConfig slopeConfig = slopeConfigDataBinder.getModel();
                slopeConfig.fromFractalFiledConfig(fractalFieldConfig);
            }
        });
    }

    public double getZoom() {
        return shapeEditor.getScale();
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

    public SlopeConfig getSlopeConfig() {
        return slopeConfigDataBinder.getModel();
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
        shapeEditor.moveSelected(new Index(selectedXPos.getValue(), selectedYPos.getValue()));
    }

    @EventHandler("selectedYPos")
    public void selectedYPosChanged(ChangeEvent e) {
        shapeEditor.moveSelected(new Index(selectedXPos.getValue(), selectedYPos.getValue()));
    }

    @EventHandler("selectedSlopeFactor")
    public void selectedSlopeFactorChanged(ChangeEvent e) {
        shapeEditor.setSlopeFactorSelected(selectedSlopeFactor.getValue());
    }

    @EventHandler("deleteSelected")
    public void deleteSelectedButtonClick(ClickEvent e) {
        shapeEditor.deleteSelectedCorner();
    }

    @EventHandler("update")
    private void updateButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = getSlopeConfig();
        terrainSurface.setSlopeSkeleton(slopeConfig.getSlopeSkeleton());
    }

    @EventHandler("sculpt")
    private void sculptButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = getSlopeConfig();
        FractalFieldConfig fractalFieldConfig = this.fractalFieldConfig;
        if (fractalFieldConfig == null) {
            fractalFieldConfig = slopeConfig.toFractalFiledConfig();
        }
        SlopeModeler.sculpt(slopeConfig, fractalFieldConfig);
        terrainSurface.setSlopeSkeleton(slopeConfig.getSlopeSkeleton());
        terrainSurface.fillBuffers();
    }
}
