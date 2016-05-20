package com.btxtech.client.dialog.content.fractal;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.terrain.FractalField;
import com.btxtech.client.terrain.FractalFiledConfig;
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
public class FractalDialog extends Composite implements ModalDialogContent<FractalFiledConfig> {
    @Inject
    @AutoBound
    private DataBinder<FractalFiledConfig> fractalConfigDataBinder;
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
    private Button generateButton;
    @DataField
    private Element canvasElement = (Element) Browser.getDocument().createCanvasElement();
    private FractalDisplay fractalDisplay;

    @Override
    public void init(FractalFiledConfig fractalFiledConfig) {
        clampMin.setValue(fractalFiledConfig.getClampMin());
        clampMax.setValue(fractalFiledConfig.getClampMax());
        fractalConfigDataBinder.setModel(fractalFiledConfig);
        fractalDisplay = new FractalDisplay(canvasElement);
        fractalDisplay.display(fractalFiledConfig);
    }

    @EventHandler("generateButton")
    private void generateButtonClick(ClickEvent event) {
        FractalFiledConfig fractalFiledConfig = fractalConfigDataBinder.getModel();
        FractalField.createSaveFractalField(fractalFiledConfig);
        fractalFiledConfig.clamp();
        fractalDisplay.display(fractalFiledConfig);
    }

    @EventHandler("clampMin")
    public void clampMinChanged(ChangeEvent e) {
        FractalFiledConfig fractalFiledConfig = fractalConfigDataBinder.getModel();
        fractalFiledConfig.setClampMin(clampMin.getValue());
        clamp();
    }

    @EventHandler("clampMax")
    public void clampMaxChanged(ChangeEvent e) {
        FractalFiledConfig fractalFiledConfig = fractalConfigDataBinder.getModel();
        fractalFiledConfig.setClampMax(clampMax.getValue());
        clamp();
    }

    private void clamp() {
        FractalFiledConfig fractalFiledConfig = fractalConfigDataBinder.getModel();
        fractalFiledConfig.clamp();
        fractalDisplay.display(fractalFiledConfig);
    }

    @Override
    public void onClose() {
    }
}
