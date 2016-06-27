package com.btxtech.client.dialog.content.fractal;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.uiservice.utils.FractalField;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
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
 * 20.05.2016.
 */
@Templated("FractalDialog.html#fractal-dialog")
public class FractalDialog extends Composite implements ModalDialogContent<FractalFieldConfig> {
    @Inject
    @AutoBound
    private DataBinder<FractalFieldConfig> fractalConfigDataBinder;
    @Inject
    @Bound
    @DataField
    private IntegerBox xCount;
    @Inject
    @Bound
    @DataField
    private IntegerBox yCount;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalMin;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalMax;
    @Inject
    @Bound
    @DataField
    private DoubleBox fractalRoughness;
    @Inject
    @DataField
    private DoubleBox clampMin;
    @Inject
    @DataField
    private DoubleBox clampMax;
    @Inject
    @DataField
    private DoubleBox fillValue;
    @Inject
    @DataField
    private Button fillButton;
    @Inject
    @DataField
    private Button generateButton;
    @DataField
    private Element canvasElement = (Element) Browser.getDocument().createCanvasElement();
    private FractalDisplay fractalDisplay;

    @Override
    public void init(FractalFieldConfig fractalFieldConfig) {
        clampMin.setValue(fractalFieldConfig.getClampMin());
        clampMax.setValue(fractalFieldConfig.getClampMax());
        fractalConfigDataBinder.setModel(fractalFieldConfig);
        fractalDisplay = new FractalDisplay(canvasElement);
        fractalDisplay.display(fractalFieldConfig);
        fillValue.setValue((fractalFieldConfig.getFractalMax() + fractalFieldConfig.getFractalMin()) / 2.0);
    }

    @Override
    public void customize(ModalDialogManager modalDialogManager) {
        // Ignore
    }

    @EventHandler("fillButton")
    private void fillButtonClick(ClickEvent event) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        FractalField.createFlatField(fractalFieldConfig, fillValue.getValue());
        fractalFieldConfig.clamp();
        fractalDisplay.display(fractalFieldConfig);
    }

    @EventHandler("generateButton")
    private void generateButtonClick(ClickEvent event) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        FractalField.createSaveFractalField(fractalFieldConfig);
        fractalFieldConfig.clamp();
        fractalDisplay.display(fractalFieldConfig);
    }

    @EventHandler("clampMin")
    public void clampMinChanged(ChangeEvent e) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        fractalFieldConfig.setClampMin(clampMin.getValue());
        clamp();
    }

    @EventHandler("clampMax")
    public void clampMaxChanged(ChangeEvent e) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        fractalFieldConfig.setClampMax(clampMax.getValue());
        clamp();
    }

    private void clamp() {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        fractalFieldConfig.clamp();
        fractalDisplay.display(fractalFieldConfig);
    }

    @Override
    public void onClose() {
    }
}
