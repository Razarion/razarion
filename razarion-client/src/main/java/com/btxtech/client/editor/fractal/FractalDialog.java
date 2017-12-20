package com.btxtech.client.editor.fractal;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.editor.widgets.FileButton;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.utils.CanvasUtil;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.uiservice.utils.FractalFieldGenerator;
import com.google.gwt.dom.client.Element;
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
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2016.
 */
@Templated("FractalDialog.html#fractal-dialog")
public class FractalDialog extends Composite implements ModalDialogContent<FractalFieldConfig> {
    private Logger logger = Logger.getLogger(FractalDialog.class.getName());
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
    private CommaDoubleBox fractalMin;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox fractalMax;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox fractalRoughness;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox clampMin;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox clampMax;
    @Inject
    @DataField
    private DoubleBox fillValue;
    @Inject
    @DataField
    private Button fillButton;
    @Inject
    @DataField
    private Button generateButton;
    @Inject
    @DataField
    private FileButton loadImageButton;
    @Inject
    @DataField
    private CommaDoubleBox loadedMin;
    @Inject
    @DataField
    private CommaDoubleBox loadedMax;
    @DataField
    private Element canvasElement = (Element) Browser.getDocument().createCanvasElement();
    private FractalDisplay fractalDisplay;

    @Override
    public void init(FractalFieldConfig fractalFieldConfig) {
        fractalConfigDataBinder.setModel(fractalFieldConfig);
        fractalDisplay = new FractalDisplay(canvasElement);
        fractalDisplay.display(fractalFieldConfig);
        fillValue.setValue(0.0);
        loadedMin.setValue(-1.0);
        loadedMax.setValue(1.0);
        loadImageButton.init("Select", fileList -> ControlUtils.readFirstAsDataURL(fileList, (dataUrl, file) -> {
            CanvasUtil.getImageData(dataUrl, imageData -> {
                double[][] fractalField = new double[fractalFieldConfig.getXCount()][fractalFieldConfig.getYCount()];
                for (int x = 0; x < fractalFieldConfig.getXCount(); x++) {
                    for (int y = 0; y < fractalFieldConfig.getYCount(); y++) {
                        if (x < imageData.getWidth() && y < imageData.getHeight()) {
                            int red = imageData.getData().intAt(4 * (x + y * imageData.getWidth()));
                            double factor = (double) red / 255.0;
                            fractalField[x][y] = InterpolationUtils.mix(loadedMin.getValue(), loadedMax.getValue(), factor);

                        } else {
                            fractalField[x][y] = 0;
                        }
                    }
                }
                fractalFieldConfig.setFractalField(fractalField);
                fractalDisplay.display(fractalFieldConfig);
            });
        }));
    }

    @Override
    public void customize(ModalDialogPanel<FractalFieldConfig> modalDialogPanel) {
        // Ignore
    }

    @EventHandler("fillButton")
    private void fillButtonClick(ClickEvent event) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        FractalFieldGenerator.createFlatField(fractalFieldConfig, fillValue.getValue());
        fractalDisplay.display(fractalFieldConfig);
    }

    @EventHandler("generateButton")
    private void generateButtonClick(ClickEvent event) {
        FractalFieldConfig fractalFieldConfig = fractalConfigDataBinder.getModel();
        FractalFieldGenerator.createSaveFractalField(fractalFieldConfig);
        fractalFieldConfig.clampGeneration();
        fractalDisplay.display(fractalFieldConfig);
    }

    @Override
    public void onClose() {
    }
}
