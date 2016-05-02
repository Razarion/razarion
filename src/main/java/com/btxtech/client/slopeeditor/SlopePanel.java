package com.btxtech.client.slopeeditor;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.SlopeConfigEntity;
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
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("SlopePanel.html#slope")
public class SlopePanel extends Composite implements SelectedCornerListener {
    // private Logger logger = Logger.getLogger(SlopePanel.class.getName());
    @Inject
    @AutoBound
    private DataBinder<SlopeConfigEntity> plateauConfigEntityDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @Bound
    @DataField
    private DoubleBox slopeGroundSplattingBumpDepth;
    @Inject
    @Bound
    @DataField
    private DoubleBox slopeFactorDistance;
    @Inject
    @Bound
    @DataField
    private DoubleBox bumpMapDepth;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalShift;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalRoughness;
    @Inject
    @Bound
    @DataField
    private IntegerBox verticalSpace;
    @Inject
    @Bound
    @DataField
    private IntegerBox segments;
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

    public void init(SlopeConfigEntity slopeConfigEntity, Double zoom) {
        plateauConfigEntityDataBinder.setModel(slopeConfigEntity);
        shapeEditor.init(svgElement, slopeConfigEntity, this, zoom);
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

    public SlopeConfigEntity getSlopeConfigEntity() {
        return plateauConfigEntityDataBinder.getModel();
    }

    @Override
    public void onSelectionChanged(Corner corner) {
        if (corner != null) {
            selectedXPos.setValue(corner.getSlopeShapeEntity().getPosition().getX());
            selectedYPos.setValue(corner.getSlopeShapeEntity().getPosition().getY());
            selectedSlopeFactor.setValue((double) corner.getSlopeShapeEntity().getSlopeFactor());
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

}
